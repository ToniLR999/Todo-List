/**
 * Authentication guard for protecting routes that require user authentication.
 * Checks if the user is authenticated before allowing access to protected routes.
 * Redirects unauthenticated users to the login page.
 */
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard {
  constructor(private authService: AuthService, private router: Router) {}

  /**
   * Determines if a route can be activated based on authentication status.
   * @returns true if user is authenticated, false otherwise
   */
  canActivate(): boolean {
    if (this.authService.isAuthenticated()) {
      return true;
    }
    this.router.navigate(['/login']);
    return false;
  }
}
