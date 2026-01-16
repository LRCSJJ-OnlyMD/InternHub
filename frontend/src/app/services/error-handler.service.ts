import { Injectable, NgZone } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BehaviorSubject } from 'rxjs';

export interface AppError {
  message: string;
  status?: number;
  timestamp: Date;
  type: 'error' | 'warning' | 'info';
}

/**
 * Global Error Handler Service
 * Provides user-friendly error messages without crashing the application
 */
@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {
  private errorsSubject = new BehaviorSubject<AppError[]>([]);
  public errors$ = this.errorsSubject.asObservable();

  // Track recent errors to prevent duplicate notifications
  private recentErrors = new Set<string>();

  constructor(
    private snackBar: MatSnackBar,
    private ngZone: NgZone
  ) {}

  /**
   * Handle an error and show user-friendly message
   */
  handleError(message: string, status?: number): void {
    const errorKey = `${status}-${message}`;
    
    // Prevent duplicate error messages within 3 seconds
    if (this.recentErrors.has(errorKey)) {
      return;
    }
    
    this.recentErrors.add(errorKey);
    setTimeout(() => this.recentErrors.delete(errorKey), 3000);

    const error: AppError = {
      message,
      status,
      timestamp: new Date(),
      type: this.getErrorType(status)
    };

    // Add to error history
    const currentErrors = this.errorsSubject.value;
    this.errorsSubject.next([error, ...currentErrors.slice(0, 9)]); // Keep last 10

    // Show snackbar notification
    this.showNotification(message, error.type);
  }

  /**
   * Handle WebSocket connection errors
   */
  handleWebSocketError(error: any): void {
    console.error('WebSocket Error:', error);
    
    let message = 'Chat connection lost. Reconnecting...';
    
    if (error?.headers?.message) {
      message = `Chat error: ${error.headers.message}`;
    }
    
    this.showNotification(message, 'warning');
  }

  /**
   * Handle file upload errors
   */
  handleUploadError(error: any): void {
    let message = 'Failed to upload file. Please try again.';
    
    if (error?.status === 413) {
      message = 'File is too large. Maximum size is 10MB.';
    } else if (error?.status === 415) {
      message = 'Invalid file type. Please upload a PDF file.';
    } else if (error?.message) {
      message = error.message;
    }
    
    this.showNotification(message, 'error');
  }

  /**
   * Handle validation errors
   */
  handleValidationError(errors: { [key: string]: string }): void {
    const messages = Object.values(errors);
    if (messages.length > 0) {
      this.showNotification(messages[0], 'warning');
    }
  }

  /**
   * Show success message
   */
  showSuccess(message: string): void {
    this.showNotification(message, 'info');
  }

  /**
   * Clear all errors
   */
  clearErrors(): void {
    this.errorsSubject.next([]);
  }

  /**
   * Determine error type based on status code
   */
  private getErrorType(status?: number): 'error' | 'warning' | 'info' {
    if (!status) return 'error';
    if (status >= 500) return 'error';
    if (status >= 400) return 'warning';
    return 'info';
  }

  /**
   * Show snackbar notification
   */
  private showNotification(message: string, type: 'error' | 'warning' | 'info'): void {
    // Use NgZone to ensure change detection works properly
    this.ngZone.run(() => {
      const panelClass = this.getPanelClass(type);
      const duration = type === 'error' ? 5000 : 3000;
      
      this.snackBar.open(message, 'Close', {
        duration,
        horizontalPosition: 'end',
        verticalPosition: 'top',
        panelClass: [panelClass]
      });
    });
  }

  /**
   * Get snackbar panel class based on error type
   */
  private getPanelClass(type: 'error' | 'warning' | 'info'): string {
    switch (type) {
      case 'error': return 'error-snackbar';
      case 'warning': return 'warning-snackbar';
      case 'info': return 'success-snackbar';
      default: return 'info-snackbar';
    }
  }
}
