import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { InternshipService } from '../../services/internship.service';
import { SectorService } from '../../services/sector.service';
import { AuthService } from '../../services/auth.service';
import { InternshipResponse, InternshipRequest, Sector, InternshipStatus } from '../../models/internship.model';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';

/**
 * Student dashboard component for managing internships.
 * Students can: create, edit, submit internships, upload reports.
 */
@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTableModule,
    MatMenuModule,
    MatDialogModule,
    MatSnackBarModule,
    NotificationBellComponent
  ],
  templateUrl: './student-dashboard.component.html',
  styleUrls: ['./student-dashboard.component.css']
})
export class StudentDashboardComponent implements OnInit {
  internships: InternshipResponse[] = [];
  sectors: Sector[] = [];
  sectorsLoading = false;
  sectorsError = '';
  internshipForm: FormGroup;
  selectedInternship: InternshipResponse | null = null;
  isEditing = false;
  isCreating = false;
  selectedFile: File | null = null;
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
  profileForm: FormGroup;
  passwordForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private internshipService: InternshipService,
    private sectorService: SectorService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.internshipForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      companyName: ['', Validators.required],
      companyAddress: [''],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      sectorId: ['', Validators.required]
    });

    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { mismatch: true };
  }

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    this.userName = currentUser?.firstName + ' ' + currentUser?.lastName || 'Student';
    this.userEmail = currentUser?.email || '';
    
    // Populate profile form with current user data
    this.profileForm.patchValue({
      firstName: currentUser?.firstName || '',
      lastName: currentUser?.lastName || '',
      email: currentUser?.email || ''
    });
    
    this.loadUserInfo();
    this.loadInternships();
    this.loadSectors();
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    if (section === 'create') {
      this.showCreateForm();
      this.activeSection = 'internships';
    }
  }

  getSectionTitle(): string {
    const titles: { [key: string]: string } = {
      'overview': 'Dashboard Overview',
      'internships': 'My Internships',
      'create': 'Create New Internship',
      'reports': 'My Reports',
      'settings': 'Settings'
    };
    return titles[this.activeSection] || 'Student Dashboard';
  }

  getStatusCount(status: string): number {
    return this.internships.filter(i => i.status === status).length;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  hasAnyReports(): boolean {
    return this.internships.some(i => i.hasReport);
  }

  loadInternships(): void {
    this.internshipService.getMyInternships().subscribe({
      next: (data) => this.internships = data,
      error: (err) => console.error('Error loading internships', err)
    });
  }

  loadSectors(): void {
    this.sectorsLoading = true;
    this.sectorsError = '';
    console.log('Loading sectors from API...');
    
    this.sectorService.getAllSectors().subscribe({
      next: (data) => {
        this.sectors = data;
        this.sectorsLoading = false;
        console.log('Sectors loaded successfully:', data);
        if (data.length === 0) {
          console.warn('No sectors returned from API');
        }
      },
      error: (err) => {
        this.sectorsLoading = false;
        this.sectorsError = 'Failed to load sectors. Please try again.';
        console.error('Error loading sectors:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Full error:', err);
      }
    });
  }

  showCreateForm(): void {
    this.isCreating = true;
    this.isEditing = false;
    this.selectedInternship = null;
    this.internshipForm.reset();
  }

  editInternship(internship: InternshipResponse): void {
    if (!this.canModify(internship)) return;
    
    this.isEditing = true;
    this.isCreating = false;
    this.selectedInternship = internship;
    this.internshipForm.patchValue({
      title: internship.title,
      description: internship.description,
      companyName: internship.companyName,
      companyAddress: internship.companyAddress,
      startDate: internship.startDate,
      endDate: internship.endDate,
      sectorId: internship.sectorId
    });
  }

  saveInternship(): void {
    if (this.internshipForm.invalid) return;

    const request: InternshipRequest = this.internshipForm.value;

    if (this.isEditing && this.selectedInternship) {
      this.internshipService.updateInternship(this.selectedInternship.id, request).subscribe({
        next: () => {
          this.loadInternships();
          this.cancelForm();
        },
        error: (err) => console.error('Error updating internship', err)
      });
    } else {
      this.internshipService.createInternship(request).subscribe({
        next: (createdInternship) => {
          // If a report file was selected, upload it
          if (this.selectedFile && createdInternship && createdInternship.id) {
            this.internshipService.uploadReport(createdInternship.id, this.selectedFile).subscribe({
              next: () => {
                this.snackBar.open('Internship created and report uploaded successfully', 'Close', {
                  duration: 4000,
                  horizontalPosition: 'end',
                  verticalPosition: 'top',
                  panelClass: ['success-snackbar']
                });
                this.loadInternships();
                this.cancelForm();
              },
              error: (err) => {
                console.error('Error uploading report', err);
                this.dialog.open(ConfirmationDialogComponent, {
                  data: {
                    title: 'Warning',
                    message: 'Internship created but report upload failed. You can upload it later.',
                    confirmText: 'OK',
                    type: 'warning'
                  },
                  width: '400px'
                });
                this.loadInternships();
                this.cancelForm();
              }
            });
          } else {
            this.loadInternships();
            this.cancelForm();
          }
        },
        error: (err) => console.error('Error creating internship', err)
      });
    }
  }

  submitInternship(id: number): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Confirm Submission',
        message: 'Submit this internship for validation?',
        confirmText: 'Submit',
        cancelText: 'Cancel',
        type: 'info'
      },
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.internshipService.submitInternship(id).subscribe({
          next: () => {
            this.snackBar.open('Internship submitted for validation', 'Close', {
              duration: 3000,
              horizontalPosition: 'end',
              verticalPosition: 'top',
              panelClass: ['success-snackbar']
            });
            this.loadInternships();
          },
          error: (err) => console.error('Error submitting internship', err)
        });
      }
    });
  }

  onReportFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.type === 'application/pdf') {
        if (file.size > 10 * 1024 * 1024) { // 10MB limit
          this.dialog.open(ConfirmationDialogComponent, {
            data: {
              title: 'File Too Large',
              message: 'File size must be less than 10MB',
              confirmText: 'OK',
              type: 'warning'
            },
            width: '400px'
          });
          this.selectedFile = null;
        } else {
          this.selectedFile = file;
        }
      } else {
        this.dialog.open(ConfirmationDialogComponent, {
          data: {
            title: 'Invalid File Type',
            message: 'Please select a PDF file',
            confirmText: 'OK',
            type: 'warning'
          },
          width: '400px'
        });
        this.selectedFile = null;
      }
    }
  }

  onFileSelected(event: any, internshipId: number): void {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
      this.internshipService.uploadReport(internshipId, file).subscribe({
        next: () => {
          this.snackBar.open('Report uploaded successfully', 'Close', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });
          this.loadInternships();
        },
        error: (err) => console.error('Error uploading report', err)
      });
    } else {
      this.dialog.open(ConfirmationDialogComponent, {
        data: {
          title: 'Invalid File Type',
          message: 'Please select a PDF file',
          confirmText: 'OK',
          type: 'warning'
        },
        width: '400px'
      });
    }
  }

  viewReport(internshipId: number): void {
    this.internshipService.viewReport(internshipId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
        // Clean up after a delay to allow browser to load the PDF
        setTimeout(() => window.URL.revokeObjectURL(url), 100);
      },
      error: (err) => {
        console.error('Error viewing report', err);
        this.dialog.open(ConfirmationDialogComponent, {
          data: {
            title: 'Error Viewing Report',
            message: 'Error viewing report. The file may not exist or you may not have permission to view it.',
            confirmText: 'OK',
            type: 'error'
          },
          width: '400px'
        });
      }
    });
  }

  downloadReport(internshipId: number, internshipTitle: string): void {
    this.internshipService.downloadReport(internshipId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${internshipTitle}_report.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Error downloading report', err);
        this.dialog.open(ConfirmationDialogComponent, {
          data: {
            title: 'Error Downloading Report',
            message: 'Error downloading report. The file may not exist or you may not have permission to download it.',
            confirmText: 'OK',
            type: 'error'
          },
          width: '400px'
        });
      }
    });
  }

  cancelForm(): void {
    this.isCreating = false;
    this.isEditing = false;
    this.selectedInternship = null;
    this.selectedFile = null;
    this.internshipForm.reset();
  }

  canModify(internship: InternshipResponse): boolean {
    return internship.status === InternshipStatus.DRAFT || 
           internship.status === InternshipStatus.REFUSED;
  }

  getStatusClass(status: InternshipStatus): string {
    switch (status) {
      case InternshipStatus.DRAFT: return 'status-draft';
      case InternshipStatus.PENDING_VALIDATION: return 'status-pending';
      case InternshipStatus.VALIDATED: return 'status-validated';
      case InternshipStatus.REFUSED: return 'status-refused';
      default: return '';
    }
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

  updateProfile(): void {
    if (this.profileForm.invalid) return;

    this.settingsLoading = true;
    this.settingsErrorMessage = '';
    this.settingsSuccessMessage = '';

    const request = this.profileForm.value;

    this.authService.updateProfile(request).subscribe({
      next: (response) => {
        this.settingsLoading = false;
        this.settingsSuccessMessage = 'Profile updated successfully';
        this.userName = response.firstName + ' ' + response.lastName;
        this.userEmail = response.email;
        // Reload user info after successful update
        setTimeout(() => {
          this.settingsSuccessMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.settingsLoading = false;
        this.settingsErrorMessage = error.error?.message || 'Failed to update profile';
        setTimeout(() => {
          this.settingsErrorMessage = '';
        }, 5000);
      }
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) return;

    this.settingsLoading = true;
    this.settingsErrorMessage = '';
    this.settingsSuccessMessage = '';

    const request = {
      currentPassword: this.passwordForm.value.currentPassword,
      newPassword: this.passwordForm.value.newPassword
    };

    this.authService.changePassword(request).subscribe({
      next: (response) => {
        this.settingsLoading = false;
        this.settingsSuccessMessage = response.message;
        this.passwordForm.reset();
        setTimeout(() => {
          this.settingsSuccessMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.settingsLoading = false;
        this.settingsErrorMessage = error.error?.message || 'Failed to change password';
        setTimeout(() => {
          this.settingsErrorMessage = '';
        }, 5000);
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
