import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InternshipSearchService, InternshipSearchParams, PageResponse } from '../../services/internship-search.service';
import { SectorService } from '../../services/sector.service';
import { InternshipResponse, InternshipStatus, Sector } from '../../models/internship.model';

@Component({
  selector: 'app-internship-search',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './internship-search.component.html',
  styleUrls: ['./internship-search.component.css']
})
export class InternshipSearchComponent implements OnInit {
  searchForm: FormGroup;
  internships: InternshipResponse[] = [];
  sectors: Sector[] = [];
  loading = false;
  error = '';

  // Pagination
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 20;

  // Enum for template
  InternshipStatus = InternshipStatus;
  statusOptions = Object.values(InternshipStatus);

  constructor(
    private fb: FormBuilder,
    private searchService: InternshipSearchService,
    private sectorService: SectorService,
    private router: Router
  ) {
    this.searchForm = this.fb.group({
      sectorId: [null],
      status: [null],
      companyName: [''],
      title: [''],
      studentName: [''],
      instructorName: [''],
      startDateFrom: [''],
      startDateTo: [''],
      endDateFrom: [''],
      endDateTo: [''],
      sortBy: ['createdAt'],
      sortDirection: ['DESC']
    });
  }

  ngOnInit(): void {
    this.loadSectors();
    this.search(); // Load initial results
  }

  loadSectors(): void {
    this.sectorService.getAllSectors().subscribe({
      next: (sectors) => (this.sectors = sectors),
      error: (err) => console.error('Error loading sectors:', err)
    });
  }

  search(): void {
    this.loading = true;
    this.error = '';
    this.currentPage = 0;

    const searchParams: InternshipSearchParams = {
      ...this.searchForm.value,
      page: this.currentPage,
      size: this.pageSize
    };

    // Remove empty strings
    Object.keys(searchParams).forEach(key => {
      const value = searchParams[key as keyof InternshipSearchParams];
      if (value === '' || value === null) {
        delete searchParams[key as keyof InternshipSearchParams];
      }
    });

    this.searchService.searchInternships(searchParams).subscribe({
      next: (response: PageResponse<InternshipResponse>) => {
        this.internships = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to search internships';
        console.error('Search error:', err);
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.currentPage = page;
    this.loading = true;

    const searchParams: InternshipSearchParams = {
      ...this.searchForm.value,
      page: this.currentPage,
      size: this.pageSize
    };

    // Remove empty strings
    Object.keys(searchParams).forEach(key => {
      const value = searchParams[key as keyof InternshipSearchParams];
      if (value === '' || value === null) {
        delete searchParams[key as keyof InternshipSearchParams];
      }
    });

    this.searchService.searchInternships(searchParams).subscribe({
      next: (response: PageResponse<InternshipResponse>) => {
        this.internships = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load page';
        console.error('Pagination error:', err);
        this.loading = false;
      }
    });
  }

  clearFilters(): void {
    this.searchForm.reset({
      sortBy: 'createdAt',
      sortDirection: 'DESC'
    });
    this.search();
  }

  viewDetails(internship: InternshipResponse): void {
    // Navigate to details based on user role or use a unified view
    this.router.navigate(['/student-dashboard'], { 
      queryParams: { internshipId: internship.id } 
    });
  }

  getStatusLabel(status: InternshipStatus): string {
    const labels: { [key in InternshipStatus]: string } = {
      [InternshipStatus.DRAFT]: 'Draft',
      [InternshipStatus.PENDING_VALIDATION]: 'Pending Validation',
      [InternshipStatus.VALIDATED]: 'Validated',
      [InternshipStatus.REFUSED]: 'Refused'
    };
    return labels[status];
  }

  getStatusClass(status: InternshipStatus): string {
    const classes: { [key in InternshipStatus]: string } = {
      [InternshipStatus.DRAFT]: 'status-draft',
      [InternshipStatus.PENDING_VALIDATION]: 'status-pending',
      [InternshipStatus.VALIDATED]: 'status-validated',
      [InternshipStatus.REFUSED]: 'status-refused'
    };
    return classes[status];
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible);

    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }

    for (let i = start; i < end; i++) {
      pages.push(i);
    }

    return pages;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.loadPage(this.currentPage + 1);
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.loadPage(this.currentPage - 1);
    }
  }
}
