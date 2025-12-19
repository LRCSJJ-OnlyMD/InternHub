import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { InternshipResponse } from '../../models/internship.model';

export interface ValidateDialogData {
  internship: InternshipResponse;
}

@Component({
  selector: 'app-validate-internship-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="validate-dialog">
      <h2 mat-dialog-title>
        <mat-icon>check_circle</mat-icon>
        Validate Internship
      </h2>
      
      <mat-dialog-content>
        <div class="dialog-info-section">
          <p class="validation-message">
            You are about to validate this internship and become the official supervisor.
          </p>
          
          <div class="internship-summary">
            <div class="summary-header">
              <mat-icon>work</mat-icon>
              <h3>{{ data.internship.title }}</h3>
            </div>
            
            <div class="summary-grid">
              <div class="summary-item">
                <mat-icon class="item-icon">person</mat-icon>
                <div class="item-content">
                  <span class="item-label">Student</span>
                  <span class="item-value">{{ data.internship.studentName }}</span>
                  <span class="item-subtext">{{ data.internship.studentEmail }}</span>
                </div>
              </div>
              
              <div class="summary-item">
                <mat-icon class="item-icon">business</mat-icon>
                <div class="item-content">
                  <span class="item-label">Company</span>
                  <span class="item-value">{{ data.internship.companyName }}</span>
                  <span class="item-subtext" *ngIf="data.internship.companyAddress">{{ data.internship.companyAddress }}</span>
                </div>
              </div>
              
              <div class="summary-item">
                <mat-icon class="item-icon">category</mat-icon>
                <div class="item-content">
                  <span class="item-label">Sector</span>
                  <span class="item-value">{{ data.internship.sectorName }}</span>
                </div>
              </div>
              
              <div class="summary-item">
                <mat-icon class="item-icon">date_range</mat-icon>
                <div class="item-content">
                  <span class="item-label">Duration</span>
                  <span class="item-value">{{ data.internship.startDate }} to {{ data.internship.endDate }}</span>
                  <span class="item-subtext">{{ getDuration() }} days</span>
                </div>
              </div>
            </div>
            
            <div class="description-box" *ngIf="data.internship.description">
              <mat-icon>description</mat-icon>
              <div>
                <span class="desc-label">Description</span>
                <p class="desc-text">{{ data.internship.description }}</p>
              </div>
            </div>
          </div>
          
          <div class="responsibilities-note">
            <mat-icon>info</mat-icon>
            <div>
              <strong>As supervisor, you will:</strong>
              <ul>
                <li>Monitor the student's progress during the internship</li>
                <li>Review and evaluate the final internship report</li>
                <li>Provide guidance and feedback to the student</li>
              </ul>
            </div>
          </div>
        </div>
      </mat-dialog-content>
      
      <mat-dialog-actions align="end">
        <button mat-button (click)="onCancel()">
          <mat-icon>close</mat-icon>
          Cancel
        </button>
        <button mat-raised-button color="primary" (click)="onConfirm()">
          <mat-icon>check_circle</mat-icon>
          Validate & Become Supervisor
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .validate-dialog {
      max-width: 700px;
    }
    
    h2[mat-dialog-title] {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin: 0;
      padding: 1.5rem 1.5rem 1rem;
      font-size: 1.5rem;
      font-weight: 600;
      color: #1e293b;
      border-bottom: 2px solid #e2e8f0;
    }
    
    h2[mat-dialog-title] mat-icon {
      color: #10b981;
      font-size: 28px;
      width: 28px;
      height: 28px;
    }
    
    mat-dialog-content {
      padding: 1.5rem;
      max-height: 70vh;
      overflow-y: auto;
    }
    
    .dialog-info-section {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }
    
    .validation-message {
      font-size: 1rem;
      color: #475569;
      line-height: 1.6;
      margin: 0;
      padding: 1rem;
      background: #f0f9ff;
      border-left: 4px solid #3b82f6;
      border-radius: 8px;
    }
    
    .internship-summary {
      background: #f8fafc;
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid #e2e8f0;
    }
    
    .summary-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 1.5rem;
      padding-bottom: 1rem;
      border-bottom: 2px solid #e2e8f0;
    }
    
    .summary-header mat-icon {
      color: #3b82f6;
      font-size: 24px;
      width: 24px;
      height: 24px;
    }
    
    .summary-header h3 {
      margin: 0;
      font-size: 1.25rem;
      font-weight: 600;
      color: #1e293b;
    }
    
    .summary-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1.5rem;
    }
    
    .summary-item {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
    }
    
    .item-icon {
      color: #64748b;
      font-size: 20px;
      width: 20px;
      height: 20px;
      margin-top: 2px;
      flex-shrink: 0;
    }
    
    .item-content {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      flex: 1;
    }
    
    .item-label {
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: #64748b;
      font-weight: 600;
    }
    
    .item-value {
      font-size: 0.9375rem;
      color: #1e293b;
      font-weight: 500;
    }
    
    .item-subtext {
      font-size: 0.8125rem;
      color: #64748b;
    }
    
    .description-box {
      display: flex;
      gap: 1rem;
      margin-top: 1.5rem;
      padding: 1rem;
      background: white;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
    }
    
    .description-box mat-icon {
      color: #3b82f6;
      font-size: 20px;
      width: 20px;
      height: 20px;
      flex-shrink: 0;
    }
    
    .desc-label {
      display: block;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: #64748b;
      font-weight: 600;
      margin-bottom: 0.5rem;
    }
    
    .desc-text {
      margin: 0;
      font-size: 0.9375rem;
      color: #475569;
      line-height: 1.6;
    }
    
    .responsibilities-note {
      display: flex;
      gap: 1rem;
      padding: 1rem;
      background: #fef3c7;
      border-left: 4px solid #f59e0b;
      border-radius: 8px;
    }
    
    .responsibilities-note mat-icon {
      color: #f59e0b;
      font-size: 20px;
      width: 20px;
      height: 20px;
      flex-shrink: 0;
    }
    
    .responsibilities-note strong {
      display: block;
      margin-bottom: 0.5rem;
      color: #78350f;
    }
    
    .responsibilities-note ul {
      margin: 0;
      padding-left: 1.25rem;
      color: #78350f;
    }
    
    .responsibilities-note li {
      margin-bottom: 0.25rem;
      font-size: 0.875rem;
      line-height: 1.5;
    }
    
    mat-dialog-actions {
      padding: 1rem 1.5rem;
      border-top: 2px solid #e2e8f0;
      gap: 0.75rem;
    }
    
    mat-dialog-actions button {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.625rem 1.5rem;
      font-weight: 500;
    }
    
    mat-dialog-actions button mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }
    
    @media (max-width: 768px) {
      .summary-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ValidateInternshipDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ValidateInternshipDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ValidateDialogData
  ) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  getDuration(): number {
    const start = new Date(this.data.internship.startDate);
    const end = new Date(this.data.internship.endDate);
    const diffTime = Math.abs(end.getTime() - start.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }
}
