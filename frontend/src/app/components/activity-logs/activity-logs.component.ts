import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ActivityLogService, ActivityLog, ActivityLogPage } from '../../services/activity-log.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-activity-logs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './activity-logs.component.html',
  styleUrls: ['./activity-logs.component.css']
})
export class ActivityLogsComponent implements OnInit {
  activeSection = 'activity-logs';
  userName: string = '';
  logs: ActivityLog[] = [];
  loading = false;
  error = '';

  // Pagination
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 50;

  // Filters
  filterUserEmail = '';
  filterActionType = '';
  filterEntityType = '';
  filterStartDate = '';
  filterEndDate = '';

  // Filter options
  actionTypes: string[] = [];
  entityTypes: string[] = [];

  // Search mode
  isFiltering = false;

  constructor(
    private activityLogService: ActivityLogService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.userName = user.firstName || 'Admin';
    this.loadLogs();
    this.loadFilterOptions();
  }

  /**
   * Load activity logs.
   */
  loadLogs(): void {
    this.loading = true;
    this.error = '';

    if (this.isFiltering) {
      this.applyFilters();
    } else {
      this.activityLogService.getAllLogs(this.currentPage, this.pageSize).subscribe({
        next: (response: ActivityLogPage) => {
          this.logs = response.content;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load activity logs';
          console.error('Error loading logs:', err);
          this.loading = false;
        }
      });
    }
  }

  /**
   * Load filter options.
   */
  loadFilterOptions(): void {
    this.activityLogService.getActionTypes().subscribe({
      next: (types) => (this.actionTypes = types),
      error: (err) => console.error('Error loading action types:', err)
    });

    this.activityLogService.getEntityTypes().subscribe({
      next: (types) => (this.entityTypes = types),
      error: (err) => console.error('Error loading entity types:', err)
    });
  }

  /**
   * Apply filters.
   */
  applyFilters(): void {
    this.loading = true;
    this.error = '';
    this.isFiltering = true;
    this.currentPage = 0;

    this.activityLogService.searchLogs(
      this.filterUserEmail || undefined,
      this.filterActionType || undefined,
      this.filterEntityType || undefined,
      this.filterStartDate || undefined,
      this.filterEndDate || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (response: ActivityLogPage) => {
        this.logs = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to filter activity logs';
        console.error('Error filtering logs:', err);
        this.loading = false;
      }
    });
  }

  /**
   * Clear filters.
   */
  clearFilters(): void {
    this.filterUserEmail = '';
    this.filterActionType = '';
    this.filterEntityType = '';
    this.filterStartDate = '';
    this.filterEndDate = '';
    this.isFiltering = false;
    this.currentPage = 0;
    this.loadLogs();
  }

  /**
   * Go to next page.
   */
  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadLogs();
    }
  }

  /**
   * Go to previous page.
   */
  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadLogs();
    }
  }

  /**
   * Go to specific page.
   */
  goToPage(page: number): void {
    this.currentPage = page;
    this.loadLogs();
  }

  /**
   * Get icon for action type.
   */
  getActionIcon(actionType: string): string {
    const iconMap: { [key: string]: string } = {
      'USER_LOGIN': '🔑',
      'USER_LOGOUT': '🚪',
      'USER_REGISTER': '👤',
      'PASSWORD_CHANGE': '🔒',
      'INTERNSHIP_CREATE': '➕',
      'INTERNSHIP_UPDATE': '✏️',
      'INTERNSHIP_DELETE': '🗑️',
      'INTERNSHIP_SUBMIT': '📤',
      'INTERNSHIP_CLAIM': '✋',
      'INTERNSHIP_VALIDATE': '✅',
      'INTERNSHIP_REFUSE': '❌',
      'COMMENT_ADD': '💬',
      'COMMENT_UPDATE': '✏️',
      'COMMENT_DELETE': '🗑️',
      'USER_CREATE': '👥',
      'USER_UPDATE': '✏️',
      'USER_DELETE': '🗑️'
    };
    return iconMap[actionType] || '📋';
  }

  /**
   * Get color class for action type.
   */
  getActionColorClass(actionType: string): string {
    if (actionType.includes('LOGIN') || actionType.includes('REGISTER')) {
      return 'action-success';
    }
    if (actionType.includes('DELETE') || actionType.includes('REFUSE')) {
      return 'action-danger';
    }
    if (actionType.includes('UPDATE') || actionType.includes('EDIT')) {
      return 'action-warning';
    }
    if (actionType.includes('VALIDATE') || actionType.includes('CLAIM')) {
      return 'action-success';
    }
    return 'action-info';
  }

  /**
   * Format action type for display.
   */
  formatActionType(actionType: string): string {
    return actionType.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase());
  }

  /**
   * Format date for display.
   */
  getRelativeTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffSec < 60) return 'Just now';
    if (diffMin < 60) return `${diffMin} minute${diffMin > 1 ? 's' : ''} ago`;
    if (diffHour < 24) return `${diffHour} hour${diffHour > 1 ? 's' : ''} ago`;
    if (diffDay < 7) return `${diffDay} day${diffDay > 1 ? 's' : ''} ago`;

    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }

  /**
   * Get page numbers for pagination.
   */
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

  /**
   * Navigate to a different section in admin dashboard.
   */
  navigateToSection(section: string): void {
    this.router.navigate(['/admin-dashboard'], { queryParams: { section } });
  }

  /**
   * Logout the current user.
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
