import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../shared/api-config';

export interface Comment {
  id: number;
  internshipId: number;
  userId: number;
  userFirstName: string;
  userLastName: string;
  userRole: string;
  parentCommentId?: number;
  content: string;
  createdAt: string;
  updatedAt?: string;
  edited: boolean;
  replies?: Comment[];
  replyCount: number;
}

export interface CommentRequest {
  content: string;
  parentCommentId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  constructor(private http: HttpClient) {}

  getComments(internshipId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${API_CONFIG.BASE_URL}/api/internships/${internshipId}/comments`);
  }

  addComment(internshipId: number, request: CommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${API_CONFIG.BASE_URL}/api/internships/${internshipId}/comments`, request);
  }

  updateComment(internshipId: number, commentId: number, request: CommentRequest): Observable<Comment> {
    return this.http.put<Comment>(`${API_CONFIG.BASE_URL}/api/internships/${internshipId}/comments/${commentId}`, request);
  }

  deleteComment(internshipId: number, commentId: number): Observable<void> {
    return this.http.delete<void>(`${API_CONFIG.BASE_URL}/api/internships/${internshipId}/comments/${commentId}`);
  }

  getCommentCount(internshipId: number): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${API_CONFIG.BASE_URL}/api/internships/${internshipId}/comments/count`);
  }
}
