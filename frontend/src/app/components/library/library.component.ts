import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { LibraryService, LibraryDocument, LibraryStatistics, Page } from '../../services/library.service';
import { AuthService } from '../../services/auth.service';
import { SharedLayoutComponent } from '../shared-layout/shared-layout.component';

@Component({
  selector: 'app-library',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatTooltipModule,
    MatBadgeModule,
    MatMenuModule,
    MatDividerModule,
    MatDialogModule,
    MatSnackBarModule,
    SharedLayoutComponent
  ],
  templateUrl: './library.component.html',
  styleUrls: ['./library.component.css']
})
export class LibraryComponent implements OnInit {
  // Data
  documents: LibraryDocument[] = [];
  featuredDocuments: LibraryDocument[] = [];
  recentDocuments: LibraryDocument[] = [];
  popularDocuments: LibraryDocument[] = [];
  statistics: LibraryStatistics | null = null;
  
  // Filters
  sectors: string[] = [];
  academicYears: string[] = [];
  selectedSector = '';
  selectedYear = '';
  searchQuery = '';
  
  // Pagination
  totalElements = 0;
  pageSize = 12;
  pageIndex = 0;
  
  // State
  isLoading = false;
  activeTab = 0;
  viewMode: 'grid' | 'list' = 'grid';
  
  // User info
  isAdmin = false;
  currentUserId: number = 0;

  constructor(
    private libraryService: LibraryService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initializeUser();
    this.loadInitialData();
  }

  private initializeUser(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.isAdmin = user.role === 'ADMIN';
      this.currentUserId = user.userId || 0;
    }
  }

  private loadInitialData(): void {
    // Load filters - academic years from API
    this.libraryService.getAllAcademicYears().subscribe(years => this.academicYears = years);
    
    // Load statistics
    this.libraryService.getStatistics().subscribe(stats => this.statistics = stats);
    
    // Load featured and popular
    this.libraryService.getFeaturedDocuments().subscribe(docs => this.featuredDocuments = docs);
    this.libraryService.getPopularDocuments(10).subscribe(docs => this.popularDocuments = docs);
    
    // Load main documents
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.isLoading = true;
    
    let request;
    if (this.searchQuery) {
      request = this.libraryService.searchDocuments(this.searchQuery, this.pageIndex, this.pageSize);
    } else if (this.selectedYear) {
      request = this.libraryService.getDocumentsByYear(this.selectedYear, this.pageIndex, this.pageSize);
    } else {
      request = this.libraryService.browseDocuments(this.pageIndex, this.pageSize);
    }
    
    request.subscribe({
      next: (page) => {
        this.documents = page.content;
        this.totalElements = page.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading documents:', error);
        this.isLoading = false;
        this.snackBar.open('Error loading documents', 'Close', { duration: 3000 });
      }
    });
  }

  onSearch(): void {
    this.pageIndex = 0;
    this.selectedSector = '';
    this.selectedYear = '';
    this.loadDocuments();
  }

  onFilterChange(): void {
    this.pageIndex = 0;
    this.searchQuery = '';
    this.loadDocuments();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedSector = '';
    this.selectedYear = '';
    this.pageIndex = 0;
    this.loadDocuments();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadDocuments();
  }

  onTabChange(index: number): void {
    this.activeTab = index;
  }

  viewDocument(document: LibraryDocument): void {
    // Open in new tab
    window.open(document.cloudinaryUrl, '_blank');
  }

  downloadDocument(document: LibraryDocument): void {
    this.libraryService.downloadDocument(document);
  }

  getFileIcon(fileType: string): string {
    return this.libraryService.getFileIcon(fileType);
  }

  formatFileSize(bytes: number): string {
    return this.libraryService.formatFileSize(bytes);
  }

  getRelativeTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    const weeks = Math.floor(diff / 604800000);
    const months = Math.floor(diff / 2592000000);
    
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    if (weeks < 4) return `${weeks}w ago`;
    if (months < 12) return `${months}mo ago`;
    return date.toLocaleDateString();
  }

  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'grid' ? 'list' : 'grid';
  }

  // Admin functions
  toggleFeatured(document: LibraryDocument, event: Event): void {
    event.stopPropagation();
    if (!this.isAdmin) return;
    
    this.libraryService.toggleFeatured(document.id).subscribe({
      next: (updated) => {
        document.featured = updated.featured;
        this.snackBar.open(
          updated.featured ? 'Document featured' : 'Document unfeatured',
          'Close',
          { duration: 2000 }
        );
      },
      error: () => this.snackBar.open('Error updating document', 'Close', { duration: 3000 })
    });
  }

  deleteDocument(document: LibraryDocument, event: Event): void {
    event.stopPropagation();
    if (!this.isAdmin) return;
    
    if (confirm(`Are you sure you want to delete "${document.title}"?`)) {
      this.libraryService.deleteDocument(document.id).subscribe({
        next: () => {
          this.documents = this.documents.filter(d => d.id !== document.id);
          this.snackBar.open('Document deleted', 'Close', { duration: 2000 });
        },
        error: () => this.snackBar.open('Error deleting document', 'Close', { duration: 3000 })
      });
    }
  }

  trackByDocument(index: number, document: LibraryDocument): number {
    return document.id;
  }
}
