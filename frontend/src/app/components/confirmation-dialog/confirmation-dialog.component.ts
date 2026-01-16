import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { trigger, transition, style, animate, keyframes } from '@angular/animations';

export interface DialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'info' | 'warning' | 'error' | 'success';
}

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  animations: [
    trigger('iconPulse', [
      transition(':enter', [
        animate('0.6s ease-out', keyframes([
          style({ transform: 'scale(0)', opacity: 0, offset: 0 }),
          style({ transform: 'scale(1.2)', opacity: 1, offset: 0.6 }),
          style({ transform: 'scale(1)', opacity: 1, offset: 1 })
        ]))
      ])
    ]),
    trigger('slideIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-10px)' }),
        animate('0.3s 0.1s ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ])
    ]),
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('0.3s 0.2s ease-out', style({ opacity: 1 }))
      ])
    ])
  ],
  template: `
    <div class="dialog-container" [class]="'dialog-' + (data.type || 'info')">
      <!-- Decorative Background Pattern -->
      <div class="dialog-pattern"></div>
      
      <!-- Icon Section -->
      <div class="icon-container" [@iconPulse]>
        <div class="icon-bg">
          <div class="icon-inner">
            <!-- Success Icon -->
            <svg *ngIf="data.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
            </svg>
            <!-- Warning Icon -->
            <svg *ngIf="data.type === 'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <!-- Error Icon -->
            <svg *ngIf="data.type === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
            <!-- Info/Default Icon -->
            <svg *ngIf="!data.type || data.type === 'info'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        </div>
      </div>

      <!-- Content Section -->
      <div class="content-section" [@slideIn]>
        <h2 class="dialog-title">{{ data.title }}</h2>
        <p class="dialog-message">{{ data.message }}</p>
      </div>

      <!-- Actions Section -->
      <div class="actions-section" [@fadeIn]>
        <button 
          *ngIf="data.cancelText" 
          class="btn-cancel" 
          (click)="onCancel()">
          {{ data.cancelText }}
        </button>
        <button 
          class="btn-confirm" 
          [class]="'btn-' + (data.type || 'info')"
          (click)="onConfirm()">
          <span class="btn-text">{{ data.confirmText || 'OK' }}</span>
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13 7l5 5m0 0l-5 5m5-5H6" />
          </svg>
        </button>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }

    .dialog-container {
      position: relative;
      padding: 2rem 2.5rem;
      min-width: 380px;
      max-width: 450px;
      background: linear-gradient(145deg, #ffffff 0%, #f8fafc 100%);
      border-radius: 20px;
      overflow: hidden;
      box-shadow: 
        0 25px 50px -12px rgba(0, 0, 0, 0.15),
        0 0 0 1px rgba(0, 0, 0, 0.05);
    }

    /* Decorative Pattern */
    .dialog-pattern {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 120px;
      background: linear-gradient(135deg, var(--accent-light) 0%, var(--accent-lighter) 100%);
      opacity: 0.1;
      clip-path: ellipse(80% 100% at 50% 0%);
    }

    /* Type-based accent colors */
    .dialog-info { --accent: #3b82f6; --accent-light: #60a5fa; --accent-lighter: #93c5fd; --accent-bg: #eff6ff; }
    .dialog-success { --accent: #10b981; --accent-light: #34d399; --accent-lighter: #6ee7b7; --accent-bg: #ecfdf5; }
    .dialog-warning { --accent: #f59e0b; --accent-light: #fbbf24; --accent-lighter: #fcd34d; --accent-bg: #fffbeb; }
    .dialog-error { --accent: #ef4444; --accent-light: #f87171; --accent-lighter: #fca5a5; --accent-bg: #fef2f2; }

    /* Icon Container */
    .icon-container {
      display: flex;
      justify-content: center;
      margin-bottom: 1.5rem;
      position: relative;
      z-index: 1;
    }

    .icon-bg {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: var(--accent-bg);
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      box-shadow: 
        0 10px 25px -5px rgba(var(--accent), 0.2),
        inset 0 -2px 10px rgba(0, 0, 0, 0.02);
    }

    .icon-bg::before {
      content: '';
      position: absolute;
      inset: -4px;
      border-radius: 50%;
      border: 2px dashed var(--accent-lighter);
      opacity: 0.5;
      animation: spin 20s linear infinite;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    .icon-inner {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--accent) 0%, var(--accent-light) 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    }

    .icon-inner svg {
      width: 28px;
      height: 28px;
      color: white;
    }

    /* Content Section */
    .content-section {
      text-align: center;
      margin-bottom: 2rem;
      position: relative;
      z-index: 1;
    }

    .dialog-title {
      font-size: 1.5rem;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 0.75rem 0;
      letter-spacing: -0.025em;
    }

    .dialog-message {
      font-size: 1rem;
      color: #64748b;
      margin: 0;
      line-height: 1.6;
      max-width: 320px;
      margin: 0 auto;
    }

    /* Actions Section */
    .actions-section {
      display: flex;
      gap: 1rem;
      justify-content: center;
      position: relative;
      z-index: 1;
    }

    .btn-cancel, .btn-confirm {
      padding: 0.875rem 1.75rem;
      border-radius: 12px;
      font-size: 0.95rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      border: none;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .btn-cancel {
      background: #f1f5f9;
      color: #64748b;
      border: 1px solid #e2e8f0;
    }

    .btn-cancel:hover {
      background: #e2e8f0;
      color: #475569;
      transform: translateY(-1px);
    }

    .btn-confirm {
      background: linear-gradient(135deg, var(--accent) 0%, var(--accent-light) 100%);
      color: white;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
      position: relative;
      overflow: hidden;
    }

    .btn-confirm::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
      transition: left 0.5s;
    }

    .btn-confirm:hover::before {
      left: 100%;
    }

    .btn-confirm:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
    }

    .btn-confirm:active {
      transform: translateY(0);
    }

    .btn-icon {
      width: 18px;
      height: 18px;
      transition: transform 0.2s;
    }

    .btn-confirm:hover .btn-icon {
      transform: translateX(3px);
    }

    /* Responsive */
    @media (max-width: 480px) {
      .dialog-container {
        min-width: 300px;
        padding: 1.5rem;
      }

      .icon-bg {
        width: 70px;
        height: 70px;
      }

      .icon-inner {
        width: 42px;
        height: 42px;
      }

      .icon-inner svg {
        width: 24px;
        height: 24px;
      }

      .dialog-title {
        font-size: 1.25rem;
      }

      .actions-section {
        flex-direction: column-reverse;
      }

      .btn-cancel, .btn-confirm {
        width: 100%;
        justify-content: center;
      }
    }
  `]
})
export class ConfirmationDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  getButtonColor(): 'primary' | 'accent' | 'warn' {
    switch (this.data.type) {
      case 'error':
      case 'warning':
        return 'warn';
      case 'success':
        return 'primary';
      default:
        return 'primary';
    }
  }
}
