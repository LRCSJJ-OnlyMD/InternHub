import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ErrorHandlerService } from '../services/error-handler.service';

/**
 * Global HTTP Error Interceptor
 * Catches all HTTP errors and handles them gracefully without crashing the app
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const errorHandler = inject(ErrorHandlerService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occurred';
      let shouldNotify = true;

      // Don't show error for cancelled requests
      if (error.status === 0 && error.error instanceof ProgressEvent) {
        errorMessage = 'Network error. Please check your internet connection.';
      } 
      // Handle specific HTTP status codes
      else if (error.status === 401) {
        errorMessage = 'Your session has expired. Please log in again.';
        // Clear token and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        router.navigate(['/login']);
      } 
      else if (error.status === 403) {
        errorMessage = 'You do not have permission to perform this action.';
      } 
      else if (error.status === 404) {
        errorMessage = 'The requested resource was not found.';
        // Don't notify for 404 on some endpoints (like checking if report exists)
        if (req.url.includes('/report') || req.url.includes('/download')) {
          shouldNotify = false;
        }
      } 
      else if (error.status === 413) {
        errorMessage = 'File is too large. Maximum size is 10MB.';
      } 
      else if (error.status === 415) {
        errorMessage = 'File type not supported. Please upload a PDF file.';
      } 
      else if (error.status === 422) {
        errorMessage = error.error?.message || 'Invalid data submitted.';
      } 
      else if (error.status === 500) {
        errorMessage = 'Server error. Please try again later.';
      } 
      else if (error.status === 502 || error.status === 503 || error.status === 504) {
        errorMessage = 'Service temporarily unavailable. Please try again later.';
      }
      // Extract error message from response if available
      else if (error.error) {
        if (typeof error.error === 'string') {
          errorMessage = error.error;
        } else if (error.error.message) {
          errorMessage = error.error.message;
        } else if (error.error.error) {
          errorMessage = error.error.error;
        }
      }

      // Log error for debugging (in development)
      console.error('HTTP Error:', {
        status: error.status,
        statusText: error.statusText,
        url: req.url,
        message: errorMessage
      });

      // Show user-friendly notification (unless suppressed)
      if (shouldNotify) {
        errorHandler.handleError(errorMessage, error.status);
      }

      // Re-throw the error so components can also handle it if needed
      return throwError(() => ({
        status: error.status,
        message: errorMessage,
        originalError: error
      }));
    })
  );
};
