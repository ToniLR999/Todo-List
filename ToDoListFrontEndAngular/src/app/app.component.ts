import { Component, HostListener, OnInit, ViewChild, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, NgZone } from '@angular/core';
import { Router, NavigationEnd, Event } from '@angular/router';
import { filter, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavComponent } from './components/nav/nav.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { AuthService } from './services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
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
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {
    this.setupResizeOptimization();
    this.checkMaintenanceStatus();
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

    // 3. Optimización de resize
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

    // 4. Estado de mantenimiento
    this.http.get<any>(`${environment.apiUrl}/api/app-status/status`).pipe(
      takeUntil(this.destroy$)
    ).subscribe((response: any) => {
      const isActive = response.status === 'UP' && response.scheduleStatus === 'ACTIVO';
      if (!isActive && this.router.url !== '/maintenance') {
        this.router.navigate(['/maintenance']);
      }
    });

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

  private checkMaintenanceStatus() {
    this.http.get<any>(`${environment.apiUrl}/api/app-status/status`)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          const isActive = response.status === 'UP' && response.scheduleStatus === 'ACTIVO';
          if (!isActive && this.router.url !== '/maintenance') {
            this.router.navigate(['/maintenance']);
          }
        },
        error: () => {}
      });
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
