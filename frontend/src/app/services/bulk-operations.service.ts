import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface BulkOperationRequest {
  internshipIds: number[];
  operationType?: 'UPDATE_STATUS' | 'ASSIGN_INSTRUCTOR' | 'DELETE' | 'VALIDATE' | 'REJECT';
  newStatus?: string;
  newInstructorId?: number;
  rejectionReason?: string;
}

export interface BulkOperationResponse {
  totalRequested: number;
  successCount: number;
  failureCount: number;
  results: OperationResult[];
  message: string;
}

export interface OperationResult {
  internshipId: number;
  success: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class BulkOperationsService {
  private apiUrl = `${environment.apiUrl}/internships/bulk`;

  constructor(private http: HttpClient) {}

  performBulkOperation(request: BulkOperationRequest): Observable<BulkOperationResponse> {
    return this.http.post<BulkOperationResponse>(`${this.apiUrl}/operation`, request);
  }

  bulkUpdateStatus(internshipIds: number[], newStatus: string): Observable<BulkOperationResponse> {
    const request: BulkOperationRequest = {
      internshipIds,
      operationType: 'UPDATE_STATUS',
      newStatus
    };
    return this.http.post<BulkOperationResponse>(`${this.apiUrl}/update-status`, request);
  }

  bulkAssignInstructor(internshipIds: number[], instructorId: number): Observable<BulkOperationResponse> {
    const request: BulkOperationRequest = {
      internshipIds,
      operationType: 'ASSIGN_INSTRUCTOR',
      newInstructorId: instructorId
    };
    return this.http.post<BulkOperationResponse>(`${this.apiUrl}/assign-instructor`, request);
  }

  bulkValidate(internshipIds: number[]): Observable<BulkOperationResponse> {
    const request: BulkOperationRequest = {
      internshipIds,
      operationType: 'VALIDATE'
    };
    return this.http.post<BulkOperationResponse>(`${this.apiUrl}/validate`, request);
  }

  bulkReject(internshipIds: number[], rejectionReason?: string): Observable<BulkOperationResponse> {
    const request: BulkOperationRequest = {
      internshipIds,
      operationType: 'REJECT',
      rejectionReason
    };
    return this.http.post<BulkOperationResponse>(`${this.apiUrl}/reject`, request);
  }

  bulkDelete(internshipIds: number[]): Observable<BulkOperationResponse> {
    const request: BulkOperationRequest = {
      internshipIds,
      operationType: 'DELETE'
    };
    return this.http.post<BulkOperationResponse>(`${this.apiUrl}/delete`, request);
  }
}
