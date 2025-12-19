import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../shared/api-config';

export interface ActivityLog {
  id: number;
  userEmail: string;
  actionType: string;
  entityType: string | null;
  entityId: number | null;
  description: string;
  ipAddress: string | null;
  oldValue: string | null;
  newValue: string | null;
  createdAt: string;
}

export interface ActivityLogPage {
  content: ActivityLog[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class ActivityLogService {
  private apiUrl = `${API_CONFIG.BASE_URL}/api/activity-logs`;

  constructor(private http: HttpClient) {}

  /**
   * Get all activity logs with pagination.
   */
  getAllLogs(page: number = 0, size: number = 50): Observable<ActivityLogPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ActivityLogPage>(this.apiUrl, { params });
  }

  /**
   * Search logs with filters.
   */
  searchLogs(
    userEmail?: string,
    actionType?: string,
    entityType?: string,
    startDate?: string,
    endDate?: string,
    page: number = 0,
    size: number = 50
  ): Observable<ActivityLogPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (userEmail) params = params.set('userEmail', userEmail);
    if (actionType) params = params.set('actionType', actionType);
    if (entityType) params = params.set('entityType', entityType);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    return this.http.get<ActivityLogPage>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Get logs by user.
   */
  getLogsByUser(userEmail: string, page: number = 0, size: number = 50): Observable<ActivityLogPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ActivityLogPage>(`${this.apiUrl}/user/${userEmail}`, { params });
  }

  /**
   * Get logs by action type.
   */
  getLogsByAction(actionType: string, page: number = 0, size: number = 50): Observable<ActivityLogPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ActivityLogPage>(`${this.apiUrl}/action/${actionType}`, { params });
  }

  /**
   * Get logs by entity.
   */
  getLogsByEntity(entityType: string, entityId: number, page: number = 0, size: number = 50): Observable<ActivityLogPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ActivityLogPage>(`${this.apiUrl}/entity/${entityType}/${entityId}`, { params });
  }

  /**
   * Get distinct action types.
   */
  getActionTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/action-types`);
  }

  /**
   * Get distinct entity types.
   */
  getEntityTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/entity-types`);
  }
}
