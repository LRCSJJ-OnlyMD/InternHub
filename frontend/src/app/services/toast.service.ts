import { Injectable, OnDestroy } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig, MatSnackBarRef, TextOnlySnackBar } from '@angular/material/snack-bar';
import { Subject, BehaviorSubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export interface ToastNotification {
  id: string;
  title: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
  timestamp: Date;
  action?: string;
  actionCallback?: () => void;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService implements OnDestroy {
  private destroy$ = new Subject<void>();
  private toastQueue: ToastNotification[] = [];
  private isShowingToast = false;
  private currentSnackBarRef: MatSnackBarRef<TextOnlySnackBar> | null = null;
  
  // Toast history for reference
  private toastHistorySubject = new BehaviorSubject<ToastNotification[]>([]);
  public toastHistory$ = this.toastHistorySubject.asObservable();
  
  // Sound notification flag
  private soundEnabled = true;

  constructor(private snackBar: MatSnackBar) {
    // Load sound preference from localStorage
    const savedPref = localStorage.getItem('toast_sound_enabled');
    this.soundEnabled = savedPref !== 'false';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Show a success toast notification
   */
  success(title: string, message: string, duration: number = 5000): void {
    this.show({ title, message, type: 'success', duration });
  }

  /**
   * Show an error toast notification
   */
  error(title: string, message: string, duration: number = 7000): void {
    this.show({ title, message, type: 'error', duration });
  }

  /**
   * Show a warning toast notification
   */
  warning(title: string, message: string, duration: number = 6000): void {
    this.show({ title, message, type: 'warning', duration });
  }

  /**
   * Show an info toast notification
   */
  info(title: string, message: string, duration: number = 5000): void {
    this.show({ title, message, type: 'info', duration });
  }

  /**
   * Show a toast notification with action button
   */
  showWithAction(
    title: string,
    message: string,
    type: 'success' | 'error' | 'warning' | 'info',
    action: string,
    actionCallback: () => void,
    duration: number = 8000
  ): void {
    this.show({ title, message, type, duration, action, actionCallback });
  }

  /**
   * Internal method to show a toast
   */
  private show(notification: Partial<ToastNotification>): void {
    const toast: ToastNotification = {
      id: this.generateId(),
      title: notification.title || '',
      message: notification.message || '',
      type: notification.type || 'info',
      duration: notification.duration || 5000,
      timestamp: new Date(),
      action: notification.action,
      actionCallback: notification.actionCallback
    };

    // Add to queue
    this.toastQueue.push(toast);
    
    // Add to history
    const history = this.toastHistorySubject.value;
    this.toastHistorySubject.next([toast, ...history].slice(0, 50));

    // Process queue
    this.processQueue();
  }

  /**
   * Process the toast queue
   */
  private processQueue(): void {
    if (this.isShowingToast || this.toastQueue.length === 0) {
      return;
    }

    const toast = this.toastQueue.shift();
    if (!toast) return;

    this.isShowingToast = true;

    // Play notification sound
    if (this.soundEnabled) {
      this.playNotificationSound(toast.type);
    }

    const config: MatSnackBarConfig = {
      duration: toast.duration,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: this.getPanelClass(toast.type)
    };

    const displayMessage = toast.title 
      ? `${this.getIcon(toast.type)} ${toast.title}: ${toast.message}`
      : `${this.getIcon(toast.type)} ${toast.message}`;

    this.currentSnackBarRef = this.snackBar.open(
      displayMessage,
      toast.action || 'Dismiss',
      config
    );

    // Handle action click
    if (toast.actionCallback) {
      this.currentSnackBarRef.onAction()
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          toast.actionCallback?.();
        });
    }

    // Process next toast after this one closes
    this.currentSnackBarRef.afterDismissed()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.isShowingToast = false;
        this.currentSnackBarRef = null;
        setTimeout(() => this.processQueue(), 300);
      });
  }

  /**
   * Get panel class based on toast type
   */
  private getPanelClass(type: string): string[] {
    const baseClasses = ['toast-notification'];
    switch (type) {
      case 'success':
        return [...baseClasses, 'toast-success'];
      case 'error':
        return [...baseClasses, 'toast-error'];
      case 'warning':
        return [...baseClasses, 'toast-warning'];
      case 'info':
      default:
        return [...baseClasses, 'toast-info'];
    }
  }

  /**
   * Get icon for toast type
   */
  private getIcon(type: string): string {
    switch (type) {
      case 'success':
        return '✅';
      case 'error':
        return '❌';
      case 'warning':
        return '⚠️';
      case 'info':
      default:
        return 'ℹ️';
    }
  }

  /**
   * Play notification sound
   */
  private playNotificationSound(type: string): void {
    try {
      // Create audio context for notification sound
      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);

      // Different frequencies for different types
      const frequencies: { [key: string]: number } = {
        success: 800,
        error: 300,
        warning: 500,
        info: 600
      };

      oscillator.frequency.value = frequencies[type] || 600;
      oscillator.type = 'sine';
      gainNode.gain.value = 0.1;

      oscillator.start();
      setTimeout(() => {
        oscillator.stop();
        audioContext.close();
      }, 150);
    } catch (e) {
      // Ignore audio errors (user may have blocked audio)
    }
  }

  /**
   * Generate unique ID for toast
   */
  private generateId(): string {
    return `toast_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Enable/disable notification sounds
   */
  setSoundEnabled(enabled: boolean): void {
    this.soundEnabled = enabled;
    localStorage.setItem('toast_sound_enabled', enabled.toString());
  }

  /**
   * Check if sounds are enabled
   */
  isSoundEnabled(): boolean {
    return this.soundEnabled;
  }

  /**
   * Dismiss current toast
   */
  dismissCurrent(): void {
    this.currentSnackBarRef?.dismiss();
  }

  /**
   * Clear all pending toasts
   */
  clearQueue(): void {
    this.toastQueue = [];
  }

  /**
   * Clear toast history
   */
  clearHistory(): void {
    this.toastHistorySubject.next([]);
  }
}
