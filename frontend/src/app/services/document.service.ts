import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../shared/api-config';

export interface DocumentResponse {
  id: number;
  internshipId: number;
  internshipTitle: string;
  uploadedById: number;
  uploadedByName: string;
  fileName: string;
  originalFileName: string;
  fileSize: number;
  contentType: string;
  documentType: DocumentType;
  version: number;
  description?: string;
  isLatestVersion: boolean;
  previousVersionId?: number;
  createdAt: string;
  downloadUrl: string;
}

export interface DocumentHistoryResponse {
  originalFileName: string;
  documentType: DocumentType;
  totalVersions: number;
  latestVersionId: number;
  versions: DocumentVersionInfo[];
}

export interface DocumentVersionInfo {
  id: number;
  version: number;
  fileSize: number;
  uploadedByName: string;
  description?: string;
  isLatestVersion: boolean;
  createdAt: string;
}

export enum DocumentType {
  REPORT = 'REPORT',
  CONTRACT = 'CONTRACT',
  CERTIFICATE = 'CERTIFICATE',
  EVALUATION = 'EVALUATION',
  OTHER = 'OTHER'
}

export const DOCUMENT_TYPE_LABELS: Record<DocumentType, string> = {
  [DocumentType.REPORT]: 'Report',
  [DocumentType.CONTRACT]: 'Contract',
  [DocumentType.CERTIFICATE]: 'Certificate',
  [DocumentType.EVALUATION]: 'Evaluation',
  [DocumentType.OTHER]: 'Other'
};

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = `${API_CONFIG.baseUrl}/documents`;

  constructor(private http: HttpClient) {}

  uploadDocument(
    file: File,
    internshipId: number,
    documentType: DocumentType,
    description?: string
  ): Observable<HttpEvent<DocumentResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('internshipId', internshipId.toString());
    formData.append('documentType', documentType);
    if (description) {
      formData.append('description', description);
    }

    const req = new HttpRequest('POST', `${this.apiUrl}/upload`, formData, {
      reportProgress: true
    });

    return this.http.request<DocumentResponse>(req);
  }

  uploadNewVersion(
    file: File,
    internshipId: number,
    originalFileName: string,
    description?: string
  ): Observable<HttpEvent<DocumentResponse>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('internshipId', internshipId.toString());
    formData.append('originalFileName', originalFileName);
    if (description) {
      formData.append('description', description);
    }

    const req = new HttpRequest('POST', `${this.apiUrl}/upload-version`, formData, {
      reportProgress: true
    });

    return this.http.request<DocumentResponse>(req);
  }

  getInternshipDocuments(internshipId: number): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(`${this.apiUrl}/internship/${internshipId}`);
  }

  getLatestDocuments(internshipId: number): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(`${this.apiUrl}/internship/${internshipId}/latest`);
  }

  getDocument(documentId: number): Observable<DocumentResponse> {
    return this.http.get<DocumentResponse>(`${this.apiUrl}/${documentId}`);
  }

  getDocumentHistory(internshipId: number, fileName: string): Observable<DocumentHistoryResponse> {
    return this.http.get<DocumentHistoryResponse>(
      `${this.apiUrl}/internship/${internshipId}/history`,
      { params: { fileName } }
    );
  }

  downloadDocument(documentId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${documentId}/download`, {
      responseType: 'blob'
    });
  }

  deleteDocument(documentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${documentId}`);
  }

  deleteAllVersions(internshipId: number, fileName: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/internship/${internshipId}/all-versions`,
      { params: { fileName } }
    );
  }

  validateFileType(contentType: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/validate-file-type`, {
      params: { contentType }
    });
  }

  validateFileSize(size: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/validate-file-size`, {
      params: { size: size.toString() }
    });
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  getFileIcon(contentType: string): string {
    if (contentType.includes('pdf')) return '📄';
    if (contentType.includes('word')) return '📝';
    if (contentType.includes('excel') || contentType.includes('spreadsheet')) return '📊';
    if (contentType.includes('image')) return '🖼️';
    if (contentType.includes('text')) return '📃';
    return '📁';
  }
}
