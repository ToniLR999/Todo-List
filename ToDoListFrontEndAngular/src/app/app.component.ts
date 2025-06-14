import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd, Event } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavComponent } from './components/nav/nav.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NavComponent,
    SidebarComponent
  ],
  template: `
    <div class="app-container">
      <app-nav *ngIf="showNavbar"></app-nav>
      <div class="main-content">
        <app-sidebar *ngIf="showSidebar"></app-sidebar>
        <main class="content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .app-container {
      display: flex;
      flex-direction: column;
      height: 100vh;
    }

    .main-content {
      display: flex;
      flex: 1;
      overflow: hidden;
    }

    .content {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
    }
  `]
})
export class AppComponent implements OnInit {
  showSidebar = false;
  showNavbar = false;

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.router.events.pipe(
      filter((event: Event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      const currentRoute = event.urlAfterRedirects;
      this.showSidebar = this.shouldShowSidebar(currentRoute);
      this.showNavbar = this.shouldShowNavbar(currentRoute);
    });
  }

  private shouldShowSidebar(route: string): boolean {
    const publicRoutes = ['/login', '/register', '/profile', '/notification-preferences'];
    return !publicRoutes.some(r => route.startsWith(r)) && this.authService.isAuthenticated();
  }

  private shouldShowNavbar(route: string): boolean {
    const publicRoutes = ['/login', '/register'];
    return !publicRoutes.some(r => route.startsWith(r));
  }
}
