import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { InternshipService } from '../../services/internship.service';
import { StatisticsService } from '../../services/statistics.service';
import { SectorService } from '../../services/sector.service';
import { AuthService } from '../../services/auth.service';
import { ExportService } from '../../services/export.service';
import { InternshipResponse, InternshipStatus, StatisticsResponse, Sector } from '../../models/internship.model';
import { environment } from '../../../environments/environment';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';

interface UserDTO {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  sectorNames?: string[];
}

Chart.register(...registerables);

/**
 * Admin dashboard with statistics visualization and full internship management.
 */
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, MatDialogModule, MatSnackBarModule, NotificationBellComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, AfterViewInit {
  internships: InternshipResponse[] = [];
  sectors: Sector[] = [];
  searchForm: FormGroup;
  InternshipStatus = InternshipStatus;
  
  // Navigation
  activeSection: string = 'overview';
  userName: string = '';
  
  statusChart: Chart | null = null;
  sectorChart: Chart | null = null;
  
  statisticsByStatus: StatisticsResponse[] = [];
  statisticsBySector: StatisticsResponse[] = [];
  
  // Instructor Management
  instructors: UserDTO[] = [];
  instructorForm: FormGroup;
  isCreatingInstructor: boolean = false;
  alertMessage: string = '';
  alertType: 'success' | 'error' = 'success';
  
  // Sector Management
  sectorForm: FormGroup;
  editingSectorId: number | null = null;
  isSavingSector: boolean = false;
  sectorAlertMessage: string = '';
  sectorAlertType: 'success' | 'error' = 'success';
  
  // Reports Management
  reportFilterSector: string = '';
  reportFilterStatus: string = '';
  
  // Export Management
  exportFromDate: string = '';
  exportToDate: string = '';
  
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
  
  private apiUrl = `${environment.apiUrl}/admin/users`;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private authService: AuthService,
    private internshipService: InternshipService,
    private statisticsService: StatisticsService,
    private sectorService: SectorService,
    private exportService: ExportService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.searchForm = this.fb.group({
      sectorId: [''],
      status: [''],
      companyName: [''],
      studentId: [''],
      startDateFrom: [''],
      startDateTo: ['']
    });
    
    this.instructorForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      sectorIds: [[], Validators.required]
    });
    
    this.sectorForm = this.fb.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    this.userName = currentUser?.firstName && currentUser?.lastName 
      ? `${currentUser.firstName} ${currentUser.lastName}` 
      : currentUser?.email || 'User';
    this.userEmail = currentUser?.email || '';
    
    // Check for section query parameter
    this.route.queryParams.subscribe(params => {
      const section = params['section'];
      if (section) {
        this.activeSection = section;
        if (section === 'overview') {
          setTimeout(() => this.renderCharts(), 100);
        }
      }
    });
    
    this.loadUserInfo();
    this.loadStatistics();
    this.loadAllInternships();
    this.loadSectors();
    this.loadInstructors();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.renderCharts();
    }, 500);
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    if (section === 'overview') {
      setTimeout(() => this.renderCharts(), 100);
    }
  }

  getSectionTitle(): string {
    const titles: { [key: string]: string } = {
      'overview': 'Statistics Overview',
      'internships': 'Internship Management',
      'users': 'User Management',
      'sectors': 'Sectors Management',
      'reports': 'Reports Overview',
      'settings': 'Settings'
    };
    return titles[this.activeSection] || 'Admin Dashboard';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  navigateToActivityLogs(): void {
    this.router.navigate(['/activity-logs']);
  }

  loadStatistics(): void {
    this.statisticsService.getInternshipsByStatus().subscribe({
      next: (data) => {
        this.statisticsByStatus = data;
        if (this.statusChart) this.updateStatusChart();
      },
      error: (err) => console.error('Error loading status statistics', err)
    });

    this.statisticsService.getInternshipsBySector().subscribe({
      next: (data) => {
        this.statisticsBySector = data;
        if (this.sectorChart) this.updateSectorChart();
      },
      error: (err) => console.error('Error loading sector statistics', err)
    });
  }

  loadAllInternships(): void {
    this.internshipService.getAllInternships().subscribe({
      next: (data) => this.internships = data,
      error: (err) => console.error('Error loading internships', err)
    });
  }

  loadSectors(): void {
    this.sectorService.getAllSectors().subscribe({
      next: (data) => this.sectors = data,
      error: (err) => console.error('Error loading sectors', err)
    });
  }

  searchInternships(): void {
    const filters = this.searchForm.value;
    this.internshipService.searchInternships(filters).subscribe({
      next: (data) => this.internships = data,
      error: (err) => console.error('Error searching internships', err)
    });
  }

  resetSearch(): void {
    this.searchForm.reset();
    this.loadAllInternships();
  }

  deleteInternship(id: number): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Confirm Deletion',
        message: 'Delete this internship permanently? This action cannot be undone.',
        confirmText: 'Delete',
        cancelText: 'Cancel',
        type: 'error'
      },
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.internshipService.deleteInternship(id).subscribe({
          next: () => {
            this.snackBar.open('Internship deleted', 'Close', {
              duration: 3000,
              horizontalPosition: 'end',
              verticalPosition: 'top',
              panelClass: ['success-snackbar']
            });
            this.loadAllInternships();
            this.loadStatistics();
          },
          error: (err) => console.error('Error deleting internship', err)
        });
      }
    });
  }

  reassignInstructor(internshipId: number): void {
    const newInstructorId = prompt('Enter new instructor ID:');
    if (newInstructorId) {
      this.internshipService.reassignInstructor(internshipId, Number(newInstructorId)).subscribe({
        next: () => {
          this.snackBar.open('Instructor reassigned', 'Close', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });
          this.loadAllInternships();
        },
        error: (err) => console.error('Error reassigning instructor', err)
      });
    }
  }

  renderCharts(): void {
    this.renderStatusChart();
    this.renderSectorChart();
  }

  renderStatusChart(): void {
    const canvas = document.getElementById('statusChart') as HTMLCanvasElement;
    if (!canvas || this.statisticsByStatus.length === 0) return;

    if (this.statusChart) {
      this.statusChart.destroy();
    }

    const colors = {
      'DRAFT': '#fbbf24',
      'PENDING_VALIDATION': '#60a5fa',
      'VALIDATED': '#34d399',
      'REFUSED': '#f87171'
    };

    this.statusChart = new Chart(canvas, {
      type: 'pie',
      data: {
        labels: this.statisticsByStatus.map(s => s.label),
        datasets: [{
          data: this.statisticsByStatus.map(s => s.count),
          backgroundColor: this.statisticsByStatus.map(s => colors[s.label as keyof typeof colors] || '#9ca3af')
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: false
          },
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: {
                size: 12
              }
            }
          }
        }
      }
    });
  }

  renderSectorChart(): void {
    const canvas = document.getElementById('sectorChart') as HTMLCanvasElement;
    if (!canvas || this.statisticsBySector.length === 0) return;

    if (this.sectorChart) {
      this.sectorChart.destroy();
    }

    this.sectorChart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: this.statisticsBySector.map(s => s.label),
        datasets: [{
          label: 'Number of Internships',
          data: this.statisticsBySector.map(s => s.count),
          backgroundColor: '#667eea',
          borderRadius: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: false
          },
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1
            }
          }
        }
      }
    });
  }

  updateStatusChart(): void {
    if (!this.statusChart) return;
    this.statusChart.data.labels = this.statisticsByStatus.map(s => s.label);
    this.statusChart.data.datasets[0].data = this.statisticsByStatus.map(s => s.count);
    this.statusChart.update();
  }

  updateSectorChart(): void {
    if (!this.sectorChart) return;
    this.sectorChart.data.labels = this.statisticsBySector.map(s => s.label);
    this.sectorChart.data.datasets[0].data = this.statisticsBySector.map(s => s.count);
    this.sectorChart.update();
  }

  getStatusClass(status: InternshipStatus): string {
    const classes = {
      [InternshipStatus.DRAFT]: 'status-draft',
      [InternshipStatus.PENDING_VALIDATION]: 'status-pending',
      [InternshipStatus.VALIDATED]: 'status-validated',
      [InternshipStatus.REFUSED]: 'status-refused'
    };
    return classes[status];
  }

  getStatusCount(status: string): number {
    return this.internships.filter(i => i.status === status).length;
  }

  downloadReport(id: number): void {
    this.internshipService.downloadReport(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `internship-${id}-report.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Error downloading report', err)
    });
  }

  // Instructor Management Methods
  loadInstructors(): void {
    this.http.get<UserDTO[]>(`${this.apiUrl}/instructors`).subscribe({
      next: (data) => {
        this.instructors = data;
      },
      error: (err) => {
        console.error('Error loading instructors', err);
        this.showAlert('Failed to load instructors', 'error');
      }
    });
  }

  createInstructor(): void {
    if (this.instructorForm.invalid) {
      return;
    }

    this.isCreatingInstructor = true;
    const formValue = this.instructorForm.value;
    
    // Convert sectorIds from form to array of numbers
    const payload = {
      email: formValue.email,
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      sectorIds: Array.isArray(formValue.sectorIds) 
        ? formValue.sectorIds.map((id: string) => Number(id))
        : [Number(formValue.sectorIds)]
    };

    this.http.post(`${this.apiUrl}/instructors`, payload).subscribe({
      next: () => {
        this.showAlert('Instructor created successfully! Credentials have been sent via email.', 'success');
        this.instructorForm.reset();
        this.loadInstructors();
        this.isCreatingInstructor = false;
      },
      error: (err) => {
        console.error('Error creating instructor', err);
        this.showAlert(err.error?.message || 'Failed to create instructor', 'error');
        this.isCreatingInstructor = false;
      }
    });
  }

  deleteInstructor(id: number): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Confirm Deletion',
        message: 'Delete this instructor? This action cannot be undone.',
        confirmText: 'Delete',
        cancelText: 'Cancel',
        type: 'error'
      },
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.http.delete(`${this.apiUrl}/${id}`).subscribe({
          next: () => {
            this.showAlert('Instructor deleted successfully', 'success');
            this.loadInstructors();
          },
          error: (err) => {
            console.error('Error deleting instructor', err);
            this.showAlert('Failed to delete instructor', 'error');
          }
        });
      }
    });
  }

  showAlert(message: string, type: 'success' | 'error'): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [type === 'success' ? 'success-snackbar' : 'error-snackbar']
    });
  }

  // Sector Management Methods
  saveSector(): void {
    if (this.sectorForm.invalid) {
      return;
    }

    this.isSavingSector = true;
    const sectorData = this.sectorForm.value;

    if (this.editingSectorId) {
      // Update existing sector
      this.sectorService.updateSector(this.editingSectorId, sectorData).subscribe({
        next: () => {
          this.showSectorAlert('Sector updated successfully', 'success');
          this.sectorForm.reset();
          this.editingSectorId = null;
          this.loadSectors();
          this.isSavingSector = false;
        },
        error: (err) => {
          console.error('Error updating sector', err);
          this.showSectorAlert(err.error?.message || 'Failed to update sector', 'error');
          this.isSavingSector = false;
        }
      });
    } else {
      // Create new sector
      this.sectorService.createSector(sectorData).subscribe({
        next: () => {
          this.showSectorAlert('Sector created successfully', 'success');
          this.sectorForm.reset();
          this.loadSectors();
          this.loadStatistics(); // Refresh statistics
          this.isSavingSector = false;
        },
        error: (err) => {
          console.error('Error creating sector', err);
          this.showSectorAlert(err.error?.message || 'Failed to create sector', 'error');
          this.isSavingSector = false;
        }
      });
    }
  }

  editSector(sector: Sector): void {
    this.editingSectorId = sector.id;
    this.sectorForm.patchValue({
      name: sector.name,
      code: sector.code,
      description: sector.description
    });
    // Scroll to form
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelEditSector(): void {
    this.editingSectorId = null;
    this.sectorForm.reset();
  }

  deleteSector(id: number): void {
    const sector = this.sectors.find(s => s.id === id);
    const internshipsCount = this.getSectorInternshipsCount(id);
    const instructorsCount = this.getSectorInstructorsCount(id);

    let confirmMessage = `Delete sector "${sector?.name}"?`;
    if (internshipsCount > 0 || instructorsCount > 0) {
      confirmMessage += `\n\nWarning: This sector has ${internshipsCount} internship(s) and ${instructorsCount} instructor(s) assigned.`;
    }
    confirmMessage += '\n\nThis action cannot be undone.';

    if (confirm(confirmMessage)) {
      this.sectorService.deleteSector(id).subscribe({
        next: () => {
          this.showSectorAlert('Sector deleted successfully', 'success');
          this.loadSectors();
          this.loadStatistics(); // Refresh statistics
          this.loadInstructors(); // Refresh instructors list
        },
        error: (err) => {
          console.error('Error deleting sector', err);
          this.showSectorAlert(err.error?.message || 'Failed to delete sector. It may be in use.', 'error');
        }
      });
    }
  }

  getSectorInternshipsCount(sectorId: number): number {
    return this.internships.filter(i => i.sectorId === sectorId).length;
  }

  getSectorInstructorsCount(sectorId: number): number {
    return this.instructors.filter(instructor => 
      instructor.sectorNames?.some(sName => {
        const sector = this.sectors.find(s => s.name === sName);
        return sector?.id === sectorId;
      })
    ).length;
  }

  showSectorAlert(message: string, type: 'success' | 'error'): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [type === 'success' ? 'success-snackbar' : 'error-snackbar']
    });
    setTimeout(() => {
      this.sectorAlertMessage = '';
    }, 5000);
  }

  // Reports Management Methods
  getReportsCount(type: 'total' | 'validated' | 'pending' | 'missing'): number {
    switch (type) {
      case 'total':
        return this.internships.filter(i => i.hasReport).length;
      case 'validated':
        return this.internships.filter(i => i.status === InternshipStatus.VALIDATED && i.hasReport).length;
      case 'pending':
        return this.internships.filter(i => i.status === InternshipStatus.PENDING_VALIDATION).length;
      case 'missing':
        return this.internships.filter(i => !i.hasReport && i.status !== InternshipStatus.DRAFT).length;
      default:
        return 0;
    }
  }

  filterReports(): void {
    // Filtering is handled by getFilteredReportsInternships()
  }

  getFilteredReportsInternships(): InternshipResponse[] {
    let filtered = this.internships.filter(i => i.status !== InternshipStatus.DRAFT);

    if (this.reportFilterSector) {
      filtered = filtered.filter(i => i.sectorId === Number(this.reportFilterSector));
    }

    if (this.reportFilterStatus === 'with-report') {
      filtered = filtered.filter(i => i.hasReport);
    } else if (this.reportFilterStatus === 'without-report') {
      filtered = filtered.filter(i => !i.hasReport);
    }

    return filtered;
  }

  exportToCSV(): void {
    const filtered = this.getFilteredReportsInternships();
    this.internshipService.exportToCSV(filtered);
  }

  exportToExcel(): void {
    const filtered = this.getFilteredReportsInternships();
    this.internshipService.exportToExcel(filtered);
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

  // Export Methods

  /**
   * Export internships to Excel format.
   */
  exportInternships(format: 'xlsx' | 'csv'): void {
    const fromDate = this.exportFromDate || undefined;
    const toDate = this.exportToDate || undefined;

    this.exportService.exportInternships(format, fromDate, toDate).subscribe({
      next: (blob) => {
        const filename = this.exportService.getFilename('internships', format);
        this.exportService.downloadFile(blob, filename);
        this.snackBar.open(`Internships exported successfully to ${format.toUpperCase()}`, 'Close', { duration: 3000 });
      },
      error: (error) => {
        console.error('Export failed:', error);
        this.snackBar.open('Failed to export internships', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Export users to Excel format.
   */
  exportUsers(format: 'xlsx' | 'csv'): void {
    this.exportService.exportUsers(format).subscribe({
      next: (blob) => {
        const filename = this.exportService.getFilename('users', format);
        this.exportService.downloadFile(blob, filename);
        this.snackBar.open(`Users exported successfully to ${format.toUpperCase()}`, 'Close', { duration: 3000 });
      },
      error: (error) => {
        console.error('Export failed:', error);
        this.snackBar.open('Failed to export users', 'Close', { duration: 3000 });
      }
    });
  }
}
