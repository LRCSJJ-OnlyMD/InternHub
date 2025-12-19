import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

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
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()" *ngIf="data.cancelText">
        {{ data.cancelText }}
      </button>
      <button mat-raised-button [color]="getButtonColor()" (click)="onConfirm()">
        {{ data.confirmText || 'OK' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2 {
      margin: 0;
      font-size: 1.25rem;
      font-weight: 600;
    }
    
    mat-dialog-content {
      padding: 1.5rem 0;
      min-width: 300px;
    }
    
    mat-dialog-content p {
      margin: 0;
      color: #4b5563;
      line-height: 1.6;
    }
    
    mat-dialog-actions {
      padding: 0;
      margin-top: 1rem;
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
