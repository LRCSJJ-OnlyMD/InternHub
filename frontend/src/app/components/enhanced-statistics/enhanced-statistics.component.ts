import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatisticsService, EnhancedStatistics } from '../../services/statistics.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-enhanced-statistics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './enhanced-statistics.component.html',
  styleUrl: './enhanced-statistics.component.css'
})
export class EnhancedStatisticsComponent implements OnInit {
  statistics: EnhancedStatistics | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private statisticsService: StatisticsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loading = true;
    this.error = null;

    this.statisticsService.getEnhancedStatistics().subscribe({
      next: (data) => {
        this.statistics = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load statistics';
        this.loading = false;
        console.error('Error loading statistics:', err);
      }
    });
  }

  getTrendIcon(trend: string): string {
    switch (trend) {
      case 'UP':
        return '📈';
      case 'DOWN':
        return '📉';
      default:
        return '➡️';
    }
  }

  getTrendClass(trend: string): string {
    switch (trend) {
      case 'UP':
        return 'trend-up';
      case 'DOWN':
        return 'trend-down';
      default:
        return 'trend-stable';
    }
  }

  formatPercentage(value: number): string {
    return value.toFixed(1) + '%';
  }

  formatNumber(value: number): string {
    return Math.round(value).toString();
  }

  getStatusColor(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': '#ffa726',
      'VALIDATED': '#42a5f5',
      'REJECTED': '#ef5350',
      'IN_PROGRESS': '#66bb6a',
      'COMPLETED': '#26a69a'
    };
    return statusMap[status] || '#999';
  }

  goBack(): void {
    this.router.navigate(['/admin-dashboard']);
  }

  getMaxCount(data: any[]): number {
    if (!data || data.length === 0) return 1;
    return Math.max(...data.map(d => d.count));
  }

  // Expose Math for template
  Math = Math;
}
