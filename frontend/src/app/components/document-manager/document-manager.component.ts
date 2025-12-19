import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpEventType } from '@angular/common/http';
import {
  DocumentService,
  DocumentResponse,
  DocumentType,
  DOCUMENT_TYPE_LABELS,
  DocumentHistoryResponse
} from '../../services/document.service';

@Component({
  selector: 'app-document-manager',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './document-manager.component.html',
  styleUrls: ['./document-manager.component.css']
})
export class DocumentManagerComponent implements OnInit {
  @Input() internshipId!: number;
  @Input() readOnly = false;

  documents: DocumentResponse[] = [];
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

  // Upload state
  selectedFile: File | null = null;
  selectedDocumentType: DocumentType = DocumentType.REPORT;
  uploadDescription = '';
  uploadProgress = 0;
  uploading = false;

  // Version upload state
  uploadingVersion = false;
  versionFileName: string | null = null;
  versionDescription = '';
  versionFile: File | null = null;
  versionProgress = 0;

  // History modal state
  showHistoryModal = false;
  documentHistory: DocumentHistoryResponse | null = null;
  loadingHistory = false;

  documentTypes = Object.values(DocumentType);
  getLabel = (type: DocumentType) => DOCUMENT_TYPE_LABELS[type];

  constructor(public documentService: DocumentService) {}

  ngOnInit(): void {
    if (this.internshipId) {
      this.loadDocuments();
    }
  }

  loadDocuments(): void {
    this.loading = true;
    this.error = null;

    this.documentService.getLatestDocuments(this.internshipId).subscribe({
      next: (documents) => {
        this.documents = documents;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load documents';
        this.loading = false;
        console.error('Error loading documents:', err);
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  uploadDocument(): void {
    if (!this.selectedFile || !this.internshipId) {
      return;
    }

    this.uploading = true;
    this.uploadProgress = 0;
    this.error = null;

    this.documentService.uploadDocument(
      this.selectedFile,
      this.internshipId,
      this.selectedDocumentType,
      this.uploadDescription
    ).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.uploadProgress = Math.round((100 * event.loaded) / event.total);
        } else if (event.type === HttpEventType.Response) {
          this.uploading = false;
          this.uploadProgress = 0;
          this.selectedFile = null;
          this.uploadDescription = '';
          this.showSuccess('Document uploaded successfully');
          this.loadDocuments();
          
          // Reset file input
          const fileInput = document.getElementById('fileInput') as HTMLInputElement;
          if (fileInput) fileInput.value = '';
        }
      },
      error: (err) => {
        this.uploading = false;
        this.uploadProgress = 0;
        this.error = 'Failed to upload document: ' + (err.error?.message || err.message);
        console.error('Upload error:', err);
      }
    });
  }

  onVersionFileSelected(event: Event, fileName: string): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.versionFile = input.files[0];
      this.versionFileName = fileName;
    }
  }

  uploadVersion(fileName: string): void {
    if (!this.versionFile || !this.internshipId) {
      return;
    }

    this.uploadingVersion = true;
    this.versionProgress = 0;
    this.error = null;

    this.documentService.uploadNewVersion(
      this.versionFile,
      this.internshipId,
      fileName,
      this.versionDescription
    ).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress && event.total) {
          this.versionProgress = Math.round((100 * event.loaded) / event.total);
        } else if (event.type === HttpEventType.Response) {
          this.uploadingVersion = false;
          this.versionProgress = 0;
          this.versionFile = null;
          this.versionFileName = null;
          this.versionDescription = '';
          this.showSuccess('New version uploaded successfully');
          this.loadDocuments();
        }
      },
      error: (err) => {
        this.uploadingVersion = false;
        this.versionProgress = 0;
        this.error = 'Failed to upload version: ' + (err.error?.message || err.message);
        console.error('Version upload error:', err);
      }
    });
  }

  downloadDocument(document: DocumentResponse): void {
    this.documentService.downloadDocument(document.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = document.originalFileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.error = 'Failed to download document';
        console.error('Download error:', err);
      }
    });
  }

  deleteDocument(document: DocumentResponse): void {
    if (!confirm(`Are you sure you want to delete "${document.originalFileName}" (version ${document.version})?`)) {
      return;
    }

    this.documentService.deleteDocument(document.id).subscribe({
      next: () => {
        this.showSuccess('Document deleted successfully');
        this.loadDocuments();
      },
      error: (err) => {
        this.error = 'Failed to delete document';
        console.error('Delete error:', err);
      }
    });
  }

  viewHistory(document: DocumentResponse): void {
    this.showHistoryModal = true;
    this.loadingHistory = true;
    this.documentHistory = null;

    this.documentService.getDocumentHistory(
      document.internshipId,
      document.originalFileName
    ).subscribe({
      next: (history) => {
        this.documentHistory = history;
        this.loadingHistory = false;
      },
      error: (err) => {
        this.error = 'Failed to load document history';
        this.loadingHistory = false;
        this.showHistoryModal = false;
        console.error('History error:', err);
      }
    });
  }

  closeHistoryModal(): void {
    this.showHistoryModal = false;
    this.documentHistory = null;
  }

  downloadVersion(versionId: number): void {
    this.documentService.downloadDocument(versionId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'document';
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.error = 'Failed to download version';
        console.error('Download error:', err);
      }
    });
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      this.successMessage = null;
    }, 3000);
  }

  getDocumentIcon(doc: DocumentResponse): string {
    return this.documentService.getFileIcon(doc.contentType);
  }

  formatFileSize(bytes: number): string {
    return this.documentService.formatFileSize(bytes);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }
}
