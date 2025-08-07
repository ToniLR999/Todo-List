import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, throwError, map, of } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/api/auth`;
  private authUrl = `${this.apiUrl}/auth`;
  private usersUrl = `${this.apiUrl}/users`;
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private authMethod: 'jwt' | 'cookie' = 'jwt';

  constructor(private http: HttpClient, private router: Router) {
    this.isAuthenticatedSubject.next(this.isAuthenticated());
  }

  /**
   * Sets the authentication method (JWT or cookie-based).
   * @param method Authentication method to use
   */
  setAuthMethod(method: 'jwt' | 'cookie'): void {
    this.authMethod = method;
    this.isAuthenticatedSubject.next(this.isAuthenticated());
  }

  /**
   * Authenticates a user with username and password.
   * Stores JWT token in localStorage if using JWT authentication.
   * @param username User's username
   * @param password User's password
   * @returns Observable with authentication response
   */
  login(username: string, password: string): Observable<any> {
    const options = this.authMethod === 'cookie' 
      ? { withCredentials: true }
      : {};

    return this.http.post(`${this.apiUrl}/login`, { username, password }, options)
      .pipe(
        tap((response: any) => {
          if (this.authMethod === 'jwt' && response.token) {
            localStorage.setItem('token', response.token);
            localStorage.setItem('username', username);
          } else if (this.authMethod === 'cookie') {
          }
          this.isAuthenticatedSubject.next(true);
        })
      );
  }

  /**
   * Logs out the current user and navigates to login page.
   */
  logout(): void {
    localStorage.clear();
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  /**
   * Checks if the user is currently authenticated.
   * @returns true if user has a valid token, false otherwise
   */
  isAuthenticated(): boolean {
    const token = window.localStorage.getItem('token');
    return !!token;
  }

  /**
   * Gets the current authentication status as an observable.
   * @returns Observable of authentication status
   */
  getAuthStatus(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  /**
   * Registers a new user account.
   * @param username New user's username
   * @param email New user's email
   * @param password New user's password
   * @returns Observable with registration response
   */
  register(username: string, email: string, password: string, timezone: string): Observable<any> {
    //console.log('🚀 Enviando datos de registro:', { username, email, password, timezone });
    return this.http.post(`${this.apiUrl}/users/register`, { 
      username, 
      email, 
      password,
      timezone
    }).pipe(
      tap(response => console.log('✅ Registro exitoso:', response)),
      catchError(error => {
        console.error('❌ Error en registro:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Checks and logs the current authentication status.
   */
  checkAuthStatus(): void {
    const token = localStorage.getItem('token');
    // console.log('¿Está autenticado?:', this.isAuthenticated());
  }

  /**
   * Sets the JWT token in localStorage and updates authentication status.
   * @param token JWT token to store
   */
  setToken(token: string): void {
    localStorage.setItem('token', token);
    this.isAuthenticatedSubject.next(true);
  }

  /**
   * Retrieves the stored JWT token.
   * @returns JWT token or null if not found
   */
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  /**
   * Checks if a token exists in localStorage.
   */
  checkToken(): void {
    const token = localStorage.getItem('token');
  }

  /**
   * Retrieves the current user's profile information.
   * @returns Observable with user profile data
   */
  getCurrentUser(): Observable<any> {
    return this.http.get(`${this.usersUrl}/profile`);
  }

  /**
   * Updates the current user's profile information.
   * @param userDetails Updated user details
   * @returns Observable with update response
   */
  updateProfile(userDetails: any): Observable<any> {
    return this.http.put(`${this.usersUrl}/profile`, userDetails);
  }

  /**
   * Initiates password reset process by sending reset email.
   * @param email User's email address
   * @returns Observable with reset request response
   */
  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.authUrl}/forgot-password`, { email })
      .pipe(
        map(response => response),
        catchError(error => {
          if (error.status === 200) {
            // Si el status es 200 pero hay error de parsing, asumimos que fue exitoso
            return of({ message: 'Email de restablecimiento enviado' });
          }
          return throwError(() => error);
        })
      );
  }

  /**
   * Resets user password using reset token.
   * @param token Password reset token
   * @param newPassword New password
   * @returns Observable with reset response
   */
  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.authUrl}/reset-password`, { token, newPassword })
      .pipe(
        map(response => response),
        catchError(error => {
          if (error.status === 200) {
            // Si el status es 200 pero hay error de parsing, asumimos que fue exitoso
            return of({ message: 'Contraseña actualizada correctamente' });
          }
          return throwError(() => error);
        })
      );
  }

  /**
   * Retrieves the current user's username from localStorage.
   * @returns Username or null if not found
   */
  getUsername(): string | null {
    return localStorage.getItem('username');
  }
}