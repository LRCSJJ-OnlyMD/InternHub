import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InternshipService } from '../../services/internship.service';
import { BulkOperationsService, BulkOperationResponse } from '../../services/bulk-operations.service';
import { InternshipResponse } from '../../models/internship.model';

@Component({
  selector: 'app-bulk-operations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bulk-operations.component.html',
  styleUrl: './bulk-operations.component.css'
})
export class BulkOperationsComponent implements OnInit {
  internships: InternshipResponse[] = [];
  selectedInternships: Set<number> = new Set();
  loading = false;
  error: string | null = null;
  
  // Operation settings
  selectedOperation: string = '';
  newStatus: string = '';
  newInstructorId: number | null = null;
  rejectionReason: string = '';
  
  // Operation result
  operationResult: BulkOperationResponse | null = null;
  showResultModal = false;
  
  statusOptions = [
    { value: 'PENDING', label: 'Pending' },
    { value: 'VALIDATED', label: 'Validated' },
    { value: 'REJECTED', label: 'Rejected' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETED', label: 'Completed' }
  ];

  constructor(
    private internshipService: InternshipService,
    private bulkOperationsService: BulkOperationsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadInternships();
  }

  loadInternships(): void {
    this.loading = true;
    this.error = null;

    this.internshipService.getAllInternships().subscribe({
      next: (data) => {
        this.internships = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load internships';
        this.loading = false;
        console.error('Error loading internships:', err);
      }
    });
  }

  toggleSelection(internshipId: number): void {
    if (this.selectedInternships.has(internshipId)) {
      this.selectedInternships.delete(internshipId);
    } else {
      this.selectedInternships.add(internshipId);
    }
  }

  toggleSelectAll(): void {
    if (this.selectedInternships.size === this.internships.length) {
      this.selectedInternships.clear();
    } else {
      this.internships.forEach(i => this.selectedInternships.add(i.id));
    }
  }

  isSelected(internshipId: number): boolean {
    return this.selectedInternships.has(internshipId);
  }

  get allSelected(): boolean {
    return this.internships.length > 0 && this.selectedInternships.size === this.internships.length;
  }

  performOperation(): void {
    if (this.selectedInternships.size === 0) {
      alert('Please select at least one internship');
      return;
    }

    if (!this.selectedOperation) {
      alert('Please select an operation');
      return;
    }

    this.loading = true;
    const internshipIds = Array.from(this.selectedInternships);

    let operation$;

    switch (this.selectedOperation) {
      case 'UPDATE_STATUS':
        if (!this.newStatus) {
          alert('Please select a status');
          this.loading = false;
          return;
        }
        operation$ = this.bulkOperationsService.bulkUpdateStatus(internshipIds, this.newStatus);
        break;

      case 'VALIDATE':
        operation$ = this.bulkOperationsService.bulkValidate(internshipIds);
        break;

      case 'REJECT':
        operation$ = this.bulkOperationsService.bulkReject(internshipIds, this.rejectionReason);
        break;

      case 'DELETE':
        if (!confirm(`Are you sure you want to delete ${internshipIds.length} internship(s)?`)) {
          this.loading = false;
          return;
        }
        operation$ = this.bulkOperationsService.bulkDelete(internshipIds);
        break;

      default:
        this.loading = false;
        alert('Invalid operation');
        return;
    }

    operation$.subscribe({
      next: (response) => {
        this.operationResult = response;
        this.showResultModal = true;
        this.loading = false;
        this.selectedInternships.clear();
        this.loadInternships();
      },
      error: (err) => {
        this.error = 'Operation failed: ' + (err.error?.message || err.message);
        this.loading = false;
        console.error('Operation error:', err);
      }
    });
  }

  closeResultModal(): void {
    this.showResultModal = false;
    this.operationResult = null;
  }

  getStatusLabel(status: string): string {
    const option = this.statusOptions.find(o => o.value === status);
    return option ? option.label : status;
  }

  getStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'status-pending',
      'VALIDATED': 'status-validated',
      'REJECTED': 'status-rejected',
      'IN_PROGRESS': 'status-in-progress',
      'COMPLETED': 'status-completed'
    };
    return statusMap[status] || '';
  }

  goBack(): void {
    this.router.navigate(['/admin-dashboard']);
  }
}
