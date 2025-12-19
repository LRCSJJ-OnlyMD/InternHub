import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { InternshipService } from '../../services/internship.service';
import { AuthService } from '../../services/auth.service';
import { InternshipResponse, RefusalRequest, InternshipStatus } from '../../models/internship.model';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { ValidateInternshipDialogComponent } from '../validate-internship-dialog/validate-internship-dialog.component';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';

/**
 * Instructor dashboard component for validating/refusing internships.
 * Instructors can only see internships in their assigned sectors.
 */
@Component({
  selector: 'app-instructor-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MatDialogModule, MatSnackBarModule, NotificationBellComponent],
  templateUrl: './instructor-dashboard.component.html',
  styleUrls: ['./instructor-dashboard.component.css']
})
export class InstructorDashboardComponent implements OnInit {
  pendingInternships: InternshipResponse[] = [];
  availableInternships: InternshipResponse[] = [];
  validatedInternships: InternshipResponse[] = [];
  refusedInternships: InternshipResponse[] = [];
  selectedInternship: InternshipResponse | null = null;
  refusalForm: FormGroup;
  showRefusalDialog = false;
  InternshipStatus = InternshipStatus;
  
  // Navigation
  activeSection: string = 'overview';
  userName: string = '';
  
  // Settings / 2FA Management
  userEmail: string = '';
  twoFactorEnabled: boolean = false;
  settingsLoading: boolean = false;
  settingsSuccessMessage: string = '';
  settingsErrorMessage: string = '';
  showQRCode: boolean = false;
  qrCodeUrl: string = '';
  twoFactorSecret: string = '';
  confirmationCode: string = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private internshipService: InternshipService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.refusalForm = this.fb.group({
      refusalComment: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    this.userName = currentUser?.firstName && currentUser?.lastName 
      ? `${currentUser.firstName} ${currentUser.lastName}` 
      : currentUser?.email || 'User';
    this.userEmail = currentUser?.email || '';
    this.loadUserInfo();
    this.loadAvailableInternships();
    this.loadPendingInternships();
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    if (section === 'validated') {
      this.loadValidatedInternships();
    } else if (section === 'available') {
      this.loadAvailableInternships();
    }
  }

  getSectionTitle(): string {
    const titles: { [key: string]: string } = {
      'overview': 'Dashboard Overview',
      'available': `Available Internships (${this.availableInternships.length})`,
      'pending': `My Internships (${this.pendingInternships.length})`,
      'validated': `Validated Internships (${this.validatedInternships.length})`,
      'refused': 'Refused Internships',
      'settings': 'Settings'
    };
    return titles[this.activeSection] || 'Instructor Dashboard';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  loadAvailableInternships(): void {
    this.internshipService.getAvailableInternships().subscribe({
      next: (data) => this.availableInternships = data,
      error: (err) => console.error('Error loading available internships', err)
    });
  }

  loadPendingInternships(): void {
    this.internshipService.getPendingInternships().subscribe({
      next: (data) => this.pendingInternships = data,
      error: (err) => console.error('Error loading pending internships', err)
    });
  }

  claimInternship(id: number): void {
    this.internshipService.claimInternship(id).subscribe({
      next: () => {
        this.snackBar.open('Internship claimed successfully!', 'Close', { duration: 3000 });
        this.loadAvailableInternships();
        this.loadPendingInternships();
      },
      error: (err) => {
        this.snackBar.open(err.error?.message || 'Failed to claim internship', 'Close', { duration: 3000 });
      }
    });
  }

  loadValidatedInternships(): void {
    this.internshipService.getMyValidatedInternships().subscribe({
      next: (data) => this.validatedInternships = data,
      error: (err) => console.error('Error loading validated internships', err)
    });
  }

  downloadReport(internshipId: number, title: string): void {
    this.internshipService.downloadReportAsInstructor(internshipId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${title.replace(/\s+/g, '_')}_report.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Error downloading report', err);
        this.dialog.open(ConfirmationDialogComponent, {
          data: {
            title: 'Download Failed',
            message: 'Failed to download report',
            confirmText: 'OK',
            type: 'error'
          },
          width: '400px'
        });
      }
    });
  }

  validateInternship(internship: InternshipResponse): void {
    const dialogRef = this.dialog.open(ValidateInternshipDialogComponent, {
      data: { internship },
      width: '700px',
      maxWidth: '90vw'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.internshipService.validateInternship(internship.id).subscribe({
          next: () => {
            this.snackBar.open('Internship validated successfully', 'Close', {
              duration: 3000,
              horizontalPosition: 'end',
              verticalPosition: 'top',
              panelClass: ['success-snackbar']
            });
            this.loadPendingInternships();
          },
          error: (err) => console.error('Error validating internship', err)
        });
      }
    });
  }

  showRefuseDialog(internship: InternshipResponse): void {
    this.selectedInternship = internship;
    this.showRefusalDialog = true;
    this.refusalForm.reset();
  }

  refuseInternship(): void {
    if (this.refusalForm.invalid || !this.selectedInternship) return;

    const request: RefusalRequest = {
      refusalComment: this.refusalForm.value.refusalComment
    };

    this.internshipService.refuseInternship(this.selectedInternship.id, request).subscribe({
      next: () => {
        this.snackBar.open('Internship refused', 'Close', {
          duration: 3000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['success-snackbar']
        });
        this.showRefusalDialog = false;
        this.selectedInternship = null;
        this.loadPendingInternships();
      },
      error: (err) => console.error('Error refusing internship', err)
    });
  }

  cancelRefusal(): void {
    this.showRefusalDialog = false;
    this.selectedInternship = null;
    this.refusalForm.reset();
  }

  viewDetails(internship: InternshipResponse): void {
    this.selectedInternship = internship;
  }

  closeDetails(): void {
    this.selectedInternship = null;
  }

  // Settings / 2FA Methods
  loadUserInfo(): void {
    this.authService.getUserInfo().subscribe({
      next: (response) => {
        this.twoFactorEnabled = response.twoFactorEnabled || false;
      },
      error: (error) => {
        console.error('Failed to load user info', error);
      }
    });
  }

  enableTwoFactor(): void {
    this.settingsLoading = true;
    this.settingsErrorMessage = '';
    this.settingsSuccessMessage = '';

    this.authService.enableTwoFactor().subscribe({
      next: (response) => {
        this.settingsLoading = false;
        this.qrCodeUrl = response.qrCodeUrl;
        this.twoFactorSecret = response.secret;
        this.showQRCode = true;
        this.settingsSuccessMessage = response.message;
      },
      error: (error) => {
        this.settingsLoading = false;
        this.settingsErrorMessage = error.error?.message || 'Failed to enable 2FA';
      }
    });
  }

  confirm2FA(): void {
    if (!this.confirmationCode || this.confirmationCode.length !== 6) {
      this.settingsErrorMessage = 'Please enter a valid 6-digit code';
      return;
    }

    this.settingsLoading = true;
    this.settingsErrorMessage = '';

    this.authService.confirm2FA(this.confirmationCode).subscribe({
      next: (response) => {
        this.settingsLoading = false;
        this.settingsSuccessMessage = response.message;
        this.showQRCode = false;
        this.confirmationCode = '';
        this.loadUserInfo();
        this.hideSettingsAlertsAfterDelay();
      },
      error: (error) => {
        this.settingsLoading = false;
        this.settingsErrorMessage = error.error?.message || 'Failed to confirm 2FA';
      }
    });
  }

  disableTwoFactor(): void {
    if (!confirm('Are you sure you want to disable two-factor authentication? This will make your account less secure.')) {
      return;
    }

    this.settingsLoading = true;
    this.settingsErrorMessage = '';
    this.settingsSuccessMessage = '';

    this.authService.disableTwoFactor().subscribe({
      next: (response) => {
        this.settingsLoading = false;
        this.settingsSuccessMessage = response.message;
        this.showQRCode = false;
        this.confirmationCode = '';
        this.loadUserInfo();
        this.hideSettingsAlertsAfterDelay();
      },
      error: (error) => {
        this.settingsLoading = false;
        this.settingsErrorMessage = error.error?.message || 'Failed to disable 2FA';
      }
    });
  }

  cancelQRSetup(): void {
    this.showQRCode = false;
    this.qrCodeUrl = '';
    this.twoFactorSecret = '';
    this.confirmationCode = '';
    this.settingsSuccessMessage = '';
    this.settingsErrorMessage = '';
  }

  hideSettingsAlertsAfterDelay(): void {
    setTimeout(() => {
      this.settingsSuccessMessage = '';
      this.settingsErrorMessage = '';
    }, 5000);
  }
}
