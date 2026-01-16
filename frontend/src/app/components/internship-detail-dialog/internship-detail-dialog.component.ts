import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { InternshipResponse } from '../../models/internship.model';
import { ChatService } from '../../services/chat.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-internship-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="dialog-container">
      <div class="dialog-header">
        <h2>{{ data.internship.title }}</h2>
        <button mat-icon-button (click)="close()">
          <mat-icon>close</mat-icon>
        </button>
      </div>
      
      <mat-divider></mat-divider>
      
      <div class="dialog-content">
        <!-- Status Badge -->
        <div class="status-section">
          <span class="status-badge" [ngClass]="getStatusClass()">
            {{ data.internship.status }}
          </span>
        </div>
        
        <!-- Company Info -->
        <div class="info-section">
          <h3><mat-icon>business</mat-icon> Company Details</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">Company</span>
              <span class="value">{{ data.internship.companyName }}</span>
            </div>
            <div class="info-item" *ngIf="data.internship.companyAddress">
              <span class="label">Address</span>
              <span class="value">{{ data.internship.companyAddress }}</span>
            </div>
          </div>
        </div>
        
        <!-- Internship Info -->
        <div class="info-section">
          <h3><mat-icon>work</mat-icon> Internship Details</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">Sector</span>
              <span class="value">{{ data.internship.sectorName }}</span>
            </div>
            <div class="info-item">
              <span class="label">Period</span>
              <span class="value">{{ data.internship.startDate }} to {{ data.internship.endDate }}</span>
            </div>
          </div>
          <div class="description" *ngIf="data.internship.description">
            <span class="label">Description</span>
            <p>{{ data.internship.description }}</p>
          </div>
        </div>
        
        <!-- Instructor Info -->
        <div class="info-section" *ngIf="data.internship.instructorName">
          <h3><mat-icon>person</mat-icon> Assigned Instructor</h3>
          <div class="instructor-card">
            <div class="avatar">{{ getInstructorInitial() }}</div>
            <div class="instructor-info">
              <span class="name">{{ data.internship.instructorName }}</span>
              <span class="email">Instructor</span>
            </div>
            <button mat-raised-button color="primary" (click)="contactInstructor()" 
                    *ngIf="canContactInstructor()" [disabled]="isStartingChat">
              <mat-icon>chat</mat-icon>
              {{ isStartingChat ? 'Starting...' : 'Contact' }}
            </button>
          </div>
        </div>
        
        <!-- No Instructor Message -->
        <div class="info-section warning" *ngIf="!data.internship.instructorName">
          <mat-icon>info</mat-icon>
          <p>No instructor assigned yet. An instructor will be assigned after you submit your internship.</p>
        </div>
        
        <!-- Refusal Comment -->
        <div class="info-section error" *ngIf="data.internship.refusalComment">
          <h3><mat-icon>warning</mat-icon> Refusal Reason</h3>
          <p class="refusal-text">{{ data.internship.refusalComment }}</p>
        </div>
        
        <!-- Report Status -->
        <div class="info-section success" *ngIf="data.internship.hasReport">
          <mat-icon>description</mat-icon>
          <p>Report has been uploaded for this internship.</p>
        </div>
      </div>
      
      <mat-divider></mat-divider>
      
      <div class="dialog-actions">
        <button mat-button (click)="close()">Close</button>
        <button mat-raised-button color="primary" (click)="contactInstructor()" 
                *ngIf="data.internship.instructorName && canContactInstructor()" [disabled]="isStartingChat">
          <mat-icon>chat</mat-icon>
          Message Instructor
        </button>
      </div>
    </div>
  `,
  styles: [`
    .dialog-container {
      width: 500px;
      max-height: 80vh;
      display: flex;
      flex-direction: column;
    }
    
    .dialog-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
    }
    
    .dialog-header h2 {
      margin: 0;
      font-size: 1.25rem;
      font-weight: 600;
      color: #1e293b;
    }
    
    .dialog-content {
      padding: 24px;
      overflow-y: auto;
      flex: 1;
    }
    
    .status-section {
      margin-bottom: 24px;
    }
    
    .status-badge {
      display: inline-block;
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 600;
      text-transform: uppercase;
    }
    
    .status-badge.status-draft { background: #f1f5f9; color: #64748b; }
    .status-badge.status-pending { background: #fef3c7; color: #d97706; }
    .status-badge.status-validated { background: #dcfce7; color: #16a34a; }
    .status-badge.status-refused { background: #fee2e2; color: #dc2626; }
    
    .info-section {
      margin-bottom: 24px;
    }
    
    .info-section h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0 0 12px;
      font-size: 0.95rem;
      font-weight: 600;
      color: #475569;
    }
    
    .info-section h3 mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
      color: #6366f1;
    }
    
    .info-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    
    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    
    .info-item .label {
      font-size: 0.75rem;
      color: #94a3b8;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    
    .info-item .value {
      font-size: 0.9rem;
      color: #1e293b;
      font-weight: 500;
    }
    
    .description {
      margin-top: 16px;
    }
    
    .description .label {
      font-size: 0.75rem;
      color: #94a3b8;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      display: block;
      margin-bottom: 8px;
    }
    
    .description p {
      margin: 0;
      font-size: 0.9rem;
      color: #475569;
      line-height: 1.6;
    }
    
    .instructor-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: #f8fafc;
      border-radius: 12px;
    }
    
    .instructor-card .avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, #6366f1, #8b5cf6);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.2rem;
      font-weight: 600;
    }
    
    .instructor-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    
    .instructor-info .name {
      font-weight: 600;
      color: #1e293b;
    }
    
    .instructor-info .email {
      font-size: 0.85rem;
      color: #64748b;
    }
    
    .info-section.warning, .info-section.error, .info-section.success {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 16px;
      border-radius: 12px;
    }
    
    .info-section.warning {
      background: #fef3c7;
    }
    
    .info-section.warning mat-icon {
      color: #d97706;
    }
    
    .info-section.warning p {
      margin: 0;
      color: #92400e;
      font-size: 0.9rem;
    }
    
    .info-section.error {
      background: #fee2e2;
      flex-direction: column;
    }
    
    .info-section.error h3 mat-icon {
      color: #dc2626;
    }
    
    .refusal-text {
      margin: 0;
      color: #991b1b;
      font-size: 0.9rem;
    }
    
    .info-section.success {
      background: #dcfce7;
    }
    
    .info-section.success mat-icon {
      color: #16a34a;
    }
    
    .info-section.success p {
      margin: 0;
      color: #166534;
      font-size: 0.9rem;
    }
    
    .dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      padding: 16px 24px;
    }
    
    @media (max-width: 600px) {
      .dialog-container {
        width: 100%;
      }
      
      .info-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class InternshipDetailDialogComponent {
  isStartingChat = false;
  
  constructor(
    public dialogRef: MatDialogRef<InternshipDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { internship: InternshipResponse },
    private chatService: ChatService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}
  
  close(): void {
    this.dialogRef.close();
  }
  
  getStatusClass(): string {
    switch (this.data.internship.status) {
      case 'DRAFT': return 'status-draft';
      case 'PENDING_VALIDATION': return 'status-pending';
      case 'VALIDATED': return 'status-validated';
      case 'REFUSED': return 'status-refused';
      default: return '';
    }
  }
  
  getInstructorInitial(): string {
    return this.data.internship.instructorName?.charAt(0).toUpperCase() || '?';
  }
  
  canContactInstructor(): boolean {
    // Can contact if internship is pending or validated and has an instructor
    return this.data.internship.instructorId !== null && 
           this.data.internship.instructorId !== undefined &&
           ['PENDING_VALIDATION', 'VALIDATED'].includes(this.data.internship.status);
  }
  
  contactInstructor(): void {
    if (!this.data.internship.instructorId) {
      this.snackBar.open('No instructor assigned yet', 'Close', { duration: 3000 });
      return;
    }
    
    this.isStartingChat = true;
    
    this.chatService.startConversation({
      otherUserId: this.data.internship.instructorId,
      internshipId: this.data.internship.id
    }).subscribe({
      next: (conversation) => {
        this.isStartingChat = false;
        this.dialogRef.close();
        this.router.navigate(['/chat', conversation.id]);
      },
      error: (err) => {
        console.error('Error starting conversation:', err);
        this.isStartingChat = false;
        this.snackBar.open('Failed to start conversation', 'Close', { duration: 3000 });
      }
    });
  }
}
