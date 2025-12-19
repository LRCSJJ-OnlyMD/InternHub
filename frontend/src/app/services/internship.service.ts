import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InternshipRequest, InternshipResponse, RefusalRequest, InternshipStatus } from '../models/internship.model';
import { environment } from '../../environments/environment';

/**
 * Service for internship operations.
 * Provides methods for student, instructor, and admin internship management.
 */
@Injectable({
  providedIn: 'root'
})
export class InternshipService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Student operations
  createInternship(request: InternshipRequest): Observable<InternshipResponse> {
    return this.http.post<InternshipResponse>(`${this.apiUrl}/student/internships`, request);
  }

  updateInternship(id: number, request: InternshipRequest): Observable<InternshipResponse> {
    return this.http.put<InternshipResponse>(`${this.apiUrl}/student/internships/${id}`, request);
  }

  submitInternship(id: number): Observable<InternshipResponse> {
    return this.http.post<InternshipResponse>(`${this.apiUrl}/student/internships/${id}/submit`, {});
  }

  getMyInternships(): Observable<InternshipResponse[]> {
    return this.http.get<InternshipResponse[]>(`${this.apiUrl}/student/internships`);
  }

  uploadReport(internshipId: number, file: File): Observable<InternshipResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<InternshipResponse>(`${this.apiUrl}/student/internships/${internshipId}/report`, formData);
  }

  downloadReport(internshipId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/student/internships/${internshipId}/report`, {
      responseType: 'blob'
    });
  }

  viewReport(internshipId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/student/internships/${internshipId}/report?inline=true`, {
      responseType: 'blob'
    });
  }

  // Instructor operations
  getPendingInternships(): Observable<InternshipResponse[]> {
    return this.http.get<InternshipResponse[]>(`${this.apiUrl}/instructor/internships/pending`);
  }

  getAvailableInternships(): Observable<InternshipResponse[]> {
    return this.http.get<InternshipResponse[]>(`${this.apiUrl}/instructor/internships/available`);
  }

  claimInternship(id: number): Observable<InternshipResponse> {
    return this.http.post<InternshipResponse>(`${this.apiUrl}/instructor/internships/${id}/claim`, {});
  }

  validateInternship(id: number): Observable<InternshipResponse> {
    return this.http.post<InternshipResponse>(`${this.apiUrl}/instructor/internships/${id}/validate`, {});
  }

  refuseInternship(id: number, request: RefusalRequest): Observable<InternshipResponse> {
    return this.http.post<InternshipResponse>(`${this.apiUrl}/instructor/internships/${id}/refuse`, request);
  }

  downloadReportAsInstructor(internshipId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/instructor/internships/${internshipId}/report`, {
      responseType: 'blob'
    });
  }

  getMyValidatedInternships(): Observable<InternshipResponse[]> {
    // Get validated internships where I am the instructor
    return this.http.get<InternshipResponse[]>(`${this.apiUrl}/instructor/internships/validated`);
  }

  // Admin operations
  getAllInternships(): Observable<InternshipResponse[]> {
    return this.http.get<InternshipResponse[]>(`${this.apiUrl}/admin/internships`);
  }

  getInternshipById(id: number): Observable<InternshipResponse> {
    return this.http.get<InternshipResponse>(`${this.apiUrl}/admin/internships/${id}`);
  }

  deleteInternship(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/internships/${id}`);
  }

  reassignInstructor(internshipId: number, instructorId: number): Observable<InternshipResponse> {
    return this.http.put<InternshipResponse>(
      `${this.apiUrl}/admin/internships/${internshipId}/reassign/${instructorId}`,
      {}
    );
  }

  // Search operations
  searchInternships(filters: {
    sectorId?: number;
    status?: InternshipStatus;
    companyName?: string;
    studentId?: number;
    instructorId?: number;
  }): Observable<InternshipResponse[]> {
    let params = new HttpParams();
    
    if (filters.sectorId) params = params.set('sectorId', filters.sectorId.toString());
    if (filters.status) params = params.set('status', filters.status);
    if (filters.companyName) params = params.set('companyName', filters.companyName);
    if (filters.studentId) params = params.set('studentId', filters.studentId.toString());
    if (filters.instructorId) params = params.set('instructorId', filters.instructorId.toString());

    return this.http.get<InternshipResponse[]>(`${this.apiUrl}/admin/internships/search`, { params });
  }

  // Export operations
  exportToCSV(internships: InternshipResponse[]): void {
    const headers = ['ID', 'Title', 'Student', 'Company', 'Sector', 'Status', 'Start Date', 'End Date', 'Has Report'];
    const rows = internships.map(i => [
      i.id,
      i.title,
      i.studentName,
      i.companyName,
      i.sectorName,
      i.status,
      i.startDate,
      i.endDate,
      i.hasReport ? 'Yes' : 'No'
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `internships_export_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  exportToExcel(internships: InternshipResponse[]): void {
    // Simple Excel-compatible format (HTML table)
    const headers = ['ID', 'Title', 'Student', 'Company', 'Sector', 'Status', 'Start Date', 'End Date', 'Has Report'];
    const rows = internships.map(i => [
      i.id,
      i.title,
      i.studentName,
      i.companyName,
      i.sectorName,
      i.status,
      i.startDate,
      i.endDate,
      i.hasReport ? 'Yes' : 'No'
    ]);

    let excelContent = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40"><head><meta charset="utf-8"/></head><body><table>';
    excelContent += '<tr>' + headers.map(h => `<th>${h}</th>`).join('') + '</tr>';
    rows.forEach(row => {
      excelContent += '<tr>' + row.map(cell => `<td>${cell}</td>`).join('') + '</tr>';
    });
    excelContent += '</table></body></html>';

    const blob = new Blob([excelContent], { type: 'application/vnd.ms-excel' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `internships_export_${new Date().toISOString().split('T')[0]}.xls`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
