import { Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { Router, NavigationEnd, Event } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavComponent } from './components/nav/nav.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { AuthService } from './services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

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
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  showSidebar = false;
  showNavbar = false;
  isMobile: boolean = window.innerWidth <= 768;
  isMaintenanceRoute = false;
  @ViewChild('sidebar') sidebar!: SidebarComponent;

  constructor(
    private router: Router,
    private authService: AuthService,
    private http: HttpClient
  ) {
    this.checkMaintenanceStatus();
    this.checkCurrentRoute();
  }

  ngOnInit() {
    this.router.events.pipe(
      filter((event: Event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      const currentRoute = event.urlAfterRedirects;
      this.showSidebar = this.shouldShowSidebar(currentRoute);
      this.showNavbar = this.shouldShowNavbar(currentRoute);
    });
  }

  private checkMaintenanceStatus() {
    this.http.get(`${environment.apiUrl}/api/app-status/status`)
      .subscribe({
        next: (response: any) => {
          const isActive = response.status === 'UP' && response.scheduleStatus === 'ACTIVO';
          
          if (!isActive && this.router.url !== '/maintenance') {
            // Si la aplicación no está activa y no estamos ya en mantenimiento, redirigir
            this.router.navigate(['/maintenance']);
          }
        },
        error: () => {
          // En caso de error, permitir acceso (fallback)
        }
      });
  }

  private checkCurrentRoute() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.isMaintenanceRoute = event.url === '/maintenance';
    });
  }

  private shouldShowSidebar(route: string): boolean {
    const publicRoutes = ['/login', '/register', '/profile', '/notification-preferences', '/admin'];
    return !publicRoutes.some(r => route.startsWith(r)) && this.authService.isAuthenticated();
  }

  private shouldShowNavbar(route: string): boolean {
    const publicRoutes = ['/login', '/register','/maintenance','/forgot-password'];
    return !publicRoutes.some(r => route.startsWith(r));
  }

  @HostListener('window:resize')
  onResize() {
    this.isMobile = window.innerWidth <= 768;
  }
}
