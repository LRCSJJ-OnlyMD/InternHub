import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// ===================== Interfaces =====================

export interface LibraryDocument {
  id: number;
  title: string;
  description?: string;
  cloudinaryUrl: string;
  cloudinaryPublicId: string;
  fileType: string;
  fileSize: number;
  authorName: string;
  sector: string;
  academicYear: string;
  keywords: string[];
  viewCount: number;
  downloadCount: number;
  featured: boolean;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  linkedInternshipId?: number;
  linkedInternshipTitle?: string;
  uploadedById: number;
  uploadedByName: string;
  createdAt: string;
  updatedAt: string;
}

export interface LibraryStatistics {
  totalDocuments: number;
  totalViews: number;
  totalDownloads: number;
  documentsThisMonth: number;
  documentsBySector: { [key: string]: number };
  documentsByYear: { [key: string]: number };
  topDownloaded: LibraryDocument[];
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface AddToLibraryRequest {
  internshipId?: number;
  title: string;
  description?: string;
  authorName?: string;
  sector?: string;
  academicYear?: string;
  keywords?: string[];
}

// ===================== Service =====================

@Injectable({
  providedIn: 'root'
})
export class LibraryService {
  private apiUrl = `${environment.apiUrl}/library`;

  constructor(private http: HttpClient) {}

  // ===================== Public Methods =====================

  browseDocuments(page: number = 0, size: number = 12): Observable<Page<LibraryDocument>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<LibraryDocument>>(`${this.apiUrl}/documents`, { params });
  }

  searchDocuments(query: string, page: number = 0, size: number = 12): Observable<Page<LibraryDocument>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<LibraryDocument>>(`${this.apiUrl}/search`, { params });
  }

  getDocumentsBySector(sectorId: number, page: number = 0, size: number = 12): Observable<Page<LibraryDocument>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<LibraryDocument>>(`${this.apiUrl}/sector/${sectorId}`, { params });
  }

  getDocumentsByYear(academicYear: string, page: number = 0, size: number = 12): Observable<Page<LibraryDocument>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<LibraryDocument>>(`${this.apiUrl}/year/${academicYear}`, { params });
  }

  getFeaturedDocuments(): Observable<LibraryDocument[]> {
    return this.http.get<LibraryDocument[]>(`${this.apiUrl}/featured`);
  }

  getPopularDocuments(limit: number = 10): Observable<LibraryDocument[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<LibraryDocument[]>(`${this.apiUrl}/popular`, { params });
  }

  getDocument(documentId: number): Observable<LibraryDocument> {
    return this.http.get<LibraryDocument>(`${this.apiUrl}/documents/${documentId}`);
  }

  getDownloadUrl(documentId: number): Observable<{ downloadUrl: string }> {
    return this.http.get<{ downloadUrl: string }>(`${this.apiUrl}/documents/${documentId}/download`);
  }

  getStatistics(): Observable<LibraryStatistics> {
    return this.http.get<LibraryStatistics>(`${this.apiUrl}/statistics`);
  }

  getAllAcademicYears(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/years`);
  }

  // ===================== Admin Methods =====================

  addFromInternship(request: AddToLibraryRequest): Observable<LibraryDocument> {
    return this.http.post<LibraryDocument>(`${this.apiUrl}/add-from-internship`, request);
  }

  uploadDocument(
    file: File,
    title: string,
    description?: string,
    authorName?: string,
    sector?: string,
    academicYear?: string,
    keywords?: string[]
  ): Observable<LibraryDocument> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    if (description) formData.append('description', description);
    if (authorName) formData.append('authorName', authorName);
    if (sector) formData.append('sector', sector);
    if (academicYear) formData.append('academicYear', academicYear);
    if (keywords) {
      keywords.forEach(k => formData.append('keywords', k));
    }
    
    return this.http.post<LibraryDocument>(`${this.apiUrl}/upload`, formData);
  }

  updateDocument(documentId: number, request: AddToLibraryRequest): Observable<LibraryDocument> {
    return this.http.put<LibraryDocument>(`${this.apiUrl}/documents/${documentId}`, request);
  }

  toggleFeatured(documentId: number): Observable<LibraryDocument> {
    return this.http.post<LibraryDocument>(`${this.apiUrl}/documents/${documentId}/toggle-featured`, {});
  }

  changeStatus(documentId: number, status: string): Observable<LibraryDocument> {
    const params = new HttpParams().set('status', status);
    return this.http.post<LibraryDocument>(
      `${this.apiUrl}/documents/${documentId}/status`,
      {},
      { params }
    );
  }

  deleteDocument(documentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/documents/${documentId}`);
  }

  getAllDocumentsAdmin(page: number = 0, size: number = 20): Observable<Page<LibraryDocument>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<LibraryDocument>>(`${this.apiUrl}/admin/all`, { params });
  }

  // ===================== Utility Methods =====================

  downloadDocument(document: LibraryDocument): void {
    this.getDownloadUrl(document.id).subscribe({
      next: (response) => {
        const link = window.document.createElement('a');
        link.href = response.downloadUrl;
        link.download = document.title;
        link.target = '_blank';
        link.click();
      },
      error: (err) => console.error('Download error:', err)
    });
  }

  getFileIcon(fileType: string): string {
    if (fileType.includes('pdf')) return 'picture_as_pdf';
    if (fileType.includes('word') || fileType.includes('document')) return 'description';
    if (fileType.includes('image')) return 'image';
    if (fileType.includes('spreadsheet') || fileType.includes('excel')) return 'table_chart';
    if (fileType.includes('presentation') || fileType.includes('powerpoint')) return 'slideshow';
    return 'insert_drive_file';
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
