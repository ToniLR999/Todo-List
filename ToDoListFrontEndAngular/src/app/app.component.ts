import { Component, HostListener, OnInit, ViewChild, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, NgZone } from '@angular/core';
import { Router, NavigationEnd, Event } from '@angular/router';
import { filter, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavComponent } from './components/nav/nav.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { AuthService } from './services/auth.service';
import { ScheduleService } from './services/schedule.service';
import { NavigationGuardService } from './services/navigation-guard.service';
import { TaskListService } from './services/task-list.service';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NavComponent,
    SidebarComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent implements OnInit, OnDestroy {
  showSidebar = false;
  showNavbar = false;
  isMobile: boolean = window.innerWidth <= 768;
  isMaintenanceRoute = false;
  @ViewChild('sidebar') sidebar!: SidebarComponent;
  
  private destroy$ = new Subject<void>();
  private resizeSubject = new Subject<number>();

  constructor(
    private router: Router,
    private authService: AuthService,
    private scheduleService: ScheduleService,
    private navigationGuardService: NavigationGuardService,
    private taskListService: TaskListService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {
    this.setupResizeOptimization();
    this.checkCurrentRoute();
  }

  ngOnInit() {
    // 1. Navegación del router
    this.router.events.pipe(
      filter((event: Event): event is NavigationEnd => event instanceof NavigationEnd),
      debounceTime(100),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe((event: NavigationEnd) => {
      this.ngZone.runOutsideAngular(() => {
        const currentRoute = event.urlAfterRedirects;
        this.showSidebar = this.shouldShowSidebar(currentRoute);
        this.showNavbar = this.shouldShowNavbar(currentRoute);
        this.cdr.detectChanges();
      });
    });

    // 2. Optimización de resize
    this.resizeSubject.pipe(
      debounceTime(150),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe((width: number) => {
      this.ngZone.run(() => {
        this.isMobile = width <= 768;
        this.cdr.detectChanges();
      });
    });

    // 3. Verificar estado del horario y redirigir si es necesario
    this.checkScheduleAndRedirect();

    // 4. Precargar listas de tareas para mejorar rendimiento
    this.preloadTaskLists();

    // 5. Eventos de ruta
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((event: any) => {
      this.isMaintenanceRoute = event.url === '/maintenance';
    });
  }

  /**
   * Configura optimización para eventos de resize
   */
  private setupResizeOptimization(): void {
    this.resizeSubject.pipe(
      debounceTime(150),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe((width: number) => {
      this.ngZone.run(() => {
        this.isMobile = width <= 768;
        this.cdr.detectChanges();
      });
    });
  }

  /**
   * Verifica el estado del horario y redirige si es necesario
   */
  private checkScheduleAndRedirect(): void {
    // Verificar si la aplicación está activa según el horario hardcodeado
    if (!this.scheduleService.isApplicationActive() && this.router.url !== '/maintenance') {
      console.log('Aplicación fuera del horario de funcionamiento, redirigiendo a mantenimiento');
      this.router.navigate(['/maintenance']);
    }
  }

  /**
   * Optimiza la aplicación cuando el uso de memoria es alto
   */
  private optimizeForHighMemory(): void {
    // Reducir la frecuencia de actualizaciones
    this.ngZone.runOutsideAngular(() => {
      // Limpiar caché innecesario
      this.clearUnnecessaryCache();
      // Reducir la frecuencia de detección de cambios
      setTimeout(() => {
        this.cdr.detectChanges();
      }, 100);
    });
  }

  /**
   * Limpia caché innecesario
   */
  private clearUnnecessaryCache(): void {
    const images = document.querySelectorAll('img');
    images.forEach(img => {
      if (!this.isElementInViewport(img)) {
        img.src = '';
        img.removeAttribute('src');
      }
    });
  }

  /**
   * Verifica si un elemento está en el viewport
   */
  private isElementInViewport(el: Element): boolean {
    const rect = el.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
      rect.right <= (window.innerWidth || document.documentElement.clientWidth)
    );
  }

  /**
   * Precarga las listas de tareas para mejorar el rendimiento
   */
  private preloadTaskLists(): void {
    // Solo precargar si el usuario está autenticado
    if (this.authService.isAuthenticated()) {
      this.taskListService.preloadTaskLists().subscribe({
        error: (error) => {
          console.warn('No se pudieron precargar las listas de tareas:', error);
        }
      });
    }
  }

  private checkCurrentRoute() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((event: any) => {
      this.isMaintenanceRoute = event.url === '/maintenance';
    });
  }

  private shouldShowSidebar(route: string): boolean {
    const publicRoutes = ['/login', '/register', '/profile', '/notification-preferences', '/admin','/maintenance'];
    return !publicRoutes.some(r => route.startsWith(r)) && this.authService.isAuthenticated();
  }

  private shouldShowNavbar(route: string): boolean {
    const publicRoutes = ['/login', '/register','/maintenance','/forgot-password'];
    return !publicRoutes.some(r => route.startsWith(r));
  }

  @HostListener('window:resize')
  onResize() {
    this.resizeSubject.next(window.innerWidth);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
