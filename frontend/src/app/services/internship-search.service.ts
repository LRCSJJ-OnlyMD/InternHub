import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../shared/api-config';
import { InternshipResponse, InternshipStatus } from '../models/internship.model';

export interface InternshipSearchParams {
  sectorId?: number;
  status?: InternshipStatus;
  companyName?: string;
  title?: string;
  studentId?: number;
  instructorId?: number;
  studentName?: string;
  instructorName?: string;
  startDateFrom?: string;
  startDateTo?: string;
  endDateFrom?: string;
  endDateTo?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class InternshipSearchService {
  private apiUrl = `${API_CONFIG.BASE_URL}/api/internships/search`;

  constructor(private http: HttpClient) {}

  /**
   * Search internships with filters and pagination.
   */
  searchInternships(searchParams: InternshipSearchParams): Observable<PageResponse<InternshipResponse>> {
    let params = new HttpParams();

    // Add all non-null parameters
    if (searchParams.sectorId !== undefined && searchParams.sectorId !== null) {
      params = params.set('sectorId', searchParams.sectorId.toString());
    }
    if (searchParams.status) {
      params = params.set('status', searchParams.status);
    }
    if (searchParams.companyName) {
      params = params.set('companyName', searchParams.companyName);
    }
    if (searchParams.title) {
      params = params.set('title', searchParams.title);
    }
    if (searchParams.studentId) {
      params = params.set('studentId', searchParams.studentId.toString());
    }
    if (searchParams.instructorId) {
      params = params.set('instructorId', searchParams.instructorId.toString());
    }
    if (searchParams.studentName) {
      params = params.set('studentName', searchParams.studentName);
    }
    if (searchParams.instructorName) {
      params = params.set('instructorName', searchParams.instructorName);
    }
    if (searchParams.startDateFrom) {
      params = params.set('startDateFrom', searchParams.startDateFrom);
    }
    if (searchParams.startDateTo) {
      params = params.set('startDateTo', searchParams.startDateTo);
    }
    if (searchParams.endDateFrom) {
      params = params.set('endDateFrom', searchParams.endDateFrom);
    }
    if (searchParams.endDateTo) {
      params = params.set('endDateTo', searchParams.endDateTo);
    }

    // Pagination and sorting
    params = params.set('page', (searchParams.page || 0).toString());
    params = params.set('size', (searchParams.size || 20).toString());
    params = params.set('sortBy', searchParams.sortBy || 'createdAt');
    params = params.set('sortDirection', searchParams.sortDirection || 'DESC');

    return this.http.get<PageResponse<InternshipResponse>>(this.apiUrl, { params });
  }

  /**
   * POST version for complex searches.
   */
  searchInternshipsPost(searchParams: InternshipSearchParams): Observable<PageResponse<InternshipResponse>> {
    return this.http.post<PageResponse<InternshipResponse>>(this.apiUrl, searchParams);
  }
}
