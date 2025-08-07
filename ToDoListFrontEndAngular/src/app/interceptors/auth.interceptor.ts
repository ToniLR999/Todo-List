import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Authentication HTTP Interceptor
 * 
 * This interceptor automatically adds the JWT token to all HTTP requests
 * that require authentication. It intercepts outgoing HTTP requests and
 * attaches the Authorization header with the Bearer token if available.
 * 
 * Purpose:
 * - Automatically handles authentication headers for API calls
 * - Eliminates the need to manually add Authorization headers in each service
 * - Ensures all authenticated requests include the JWT token
 * - Provides centralized authentication logic
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Retrieve the JWT token from localStorage
  const token = localStorage.getItem('token');
  
  // If a token exists, clone the request and add the Authorization header
  if (token) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(cloned);
  }
  
  // If no token exists, proceed with the original request
  return next(req);
};
