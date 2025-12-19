import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../shared/api-config';

/**
 * Service for handling data export operations.
 * Provides methods to download internships and users in Excel or CSV formats.
 */
@Injectable({
  providedIn: 'root'
})
export class ExportService {
  private apiUrl = `${API_CONFIG.BASE_URL}/api/admin/export`;

  constructor(private http: HttpClient) {}

  /**
   * Export internships to specified format.
   * @param format 'xlsx' or 'csv'
   * @param fromDate Optional start date (YYYY-MM-DD)
   * @param toDate Optional end date (YYYY-MM-DD)
   */
  exportInternships(format: 'xlsx' | 'csv', fromDate?: string, toDate?: string): Observable<Blob> {
    let params = new HttpParams().set('format', format);
    
    if (fromDate) {
      params = params.set('from', fromDate);
    }
    if (toDate) {
      params = params.set('to', toDate);
    }

    return this.http.get(`${this.apiUrl}/internships`, {
      params: params,
      responseType: 'blob'
    });
  }

  /**
   * Export users to specified format.
   * @param format 'xlsx' or 'csv'
   */
  exportUsers(format: 'xlsx' | 'csv'): Observable<Blob> {
    const params = new HttpParams().set('format', format);

    return this.http.get(`${this.apiUrl}/users`, {
      params: params,
      responseType: 'blob'
    });
  }

  /**
   * Download blob as file.
   * @param blob The blob to download
   * @param filename The filename for the download
   */
  downloadFile(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  /**
   * Get filename with current date.
   * @param prefix Filename prefix (e.g., 'internships', 'users')
   * @param extension File extension (e.g., 'xlsx', 'csv')
   */
  getFilename(prefix: string, extension: string): string {
    const date = new Date().toISOString().split('T')[0];
    return `${prefix}_${date}.${extension}`;
  }
}
