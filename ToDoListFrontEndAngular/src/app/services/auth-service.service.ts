// src/app/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, throwError, map, of } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/api`;
  private authUrl = `${this.apiUrl}/auth`;
  private usersUrl = `${this.apiUrl}/users`;
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  private authMethod: 'jwt' | 'cookie' = 'jwt';

  constructor(private http: HttpClient, private router: Router) {
    this.isAuthenticatedSubject.next(this.isAuthenticated());
  }

  setAuthMethod(method: 'jwt' | 'cookie'): void {
    console.log('🔐 Cambiando método de autenticación a:', method);
    this.authMethod = method;
    this.isAuthenticatedSubject.next(this.isAuthenticated());
  }

  login(username: string, password: string): Observable<any> {
    console.log('🔑 Iniciando sesión con método:', this.authMethod);
    const options = this.authMethod === 'cookie' 
      ? { withCredentials: true }
      : {};

    return this.http.post(`${this.authUrl}/login`, { username, password }, options)
      .pipe(
        tap((response: any) => {
          if (this.authMethod === 'jwt' && response.token) {
            localStorage.setItem('token', response.token);
            console.log('📝 Token JWT guardado');
          } else if (this.authMethod === 'cookie') {
            console.log('🍪 Autenticación por cookie establecida');
          }
          this.isAuthenticatedSubject.next(true);
        })
      );
  }

  logout(): void {
    console.log('🚪 Cerrando sesión con método:', this.authMethod);
    if (this.authMethod === 'jwt') {
      localStorage.removeItem('token');
      console.log('🗑️ Token JWT eliminado');
    } else {
      console.log('🍪 Cookie de sesión eliminada');
    }
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    const token = window.localStorage.getItem('token');
    console.log('Verificando autenticación. Token:', token);
    return !!token;
  }

  getAuthStatus(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  register(username: string, email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/users/register`, { 
      username, 
      email, 
      password 
    });
  }

  checkAuthStatus(): void {
    const token = localStorage.getItem('token');
    console.log('Token actual:', token);
    console.log('¿Está autenticado?:', this.isAuthenticated());
  }

  setToken(token: string): void {
    console.log('Guardando token:', token); // Debug
    localStorage.setItem('token', token);
    this.isAuthenticatedSubject.next(true);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  checkToken(): void {
    const token = localStorage.getItem('token');
    console.log('Token actual:', token);
  }

  // Obtener perfil del usuario actual
  getCurrentUser(): Observable<any> {
    return this.http.get(`${this.usersUrl}/profile`);
  }

  // Actualizar perfil del usuario
  updateProfile(userDetails: any): Observable<any> {
    return this.http.put(`${this.usersUrl}/profile`, userDetails);
  }

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
}