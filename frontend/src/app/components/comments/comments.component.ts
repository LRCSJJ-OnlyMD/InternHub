import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommentService, Comment, CommentRequest } from '../../services/comment.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-comments',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.css']
})
export class CommentsComponent implements OnInit {
  @Input() internshipId!: number;
  
  comments: Comment[] = [];
  commentForm: FormGroup;
  replyForms: Map<number, FormGroup> = new Map();
  isLoading = false;
  isSubmitting = false;
  editingCommentId: number | null = null;
  replyingToCommentId: number | null = null;
  currentUserId: number | null = null;

  constructor(
    private commentService: CommentService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.commentForm = this.fb.group({
      content: ['', [Validators.required, Validators.maxLength(2000)]]
    });
  }

  ngOnInit(): void {
    this.loadComments();
    this.loadCurrentUser();
  }

  loadCurrentUser(): void {
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUserId = user.id;
      },
      error: (error) => console.error('Error loading current user:', error)
    });
  }

  loadComments(): void {
    this.isLoading = true;
    this.commentService.getComments(this.internshipId).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading comments:', error);
        this.isLoading = false;
      }
    });
  }

  submitComment(): void {
    if (this.commentForm.invalid || this.isSubmitting) return;

    this.isSubmitting = true;
    const request: CommentRequest = {
      content: this.commentForm.value.content
    };

    this.commentService.addComment(this.internshipId, request).subscribe({
      next: () => {
        this.commentForm.reset();
        this.loadComments();
        this.isSubmitting = false;
      },
      error: (error) => {
        console.error('Error adding comment:', error);
        this.isSubmitting = false;
      }
    });
  }

  startReply(commentId: number): void {
    this.replyingToCommentId = commentId;
    if (!this.replyForms.has(commentId)) {
      this.replyForms.set(commentId, this.fb.group({
        content: ['', [Validators.required, Validators.maxLength(2000)]]
      }));
    }
  }

  cancelReply(): void {
    this.replyingToCommentId = null;
  }

  submitReply(commentId: number): void {
    const form = this.replyForms.get(commentId);
    if (!form || form.invalid || this.isSubmitting) return;

    this.isSubmitting = true;
    const request: CommentRequest = {
      content: form.value.content,
      parentCommentId: commentId
    };

    this.commentService.addComment(this.internshipId, request).subscribe({
      next: () => {
        form.reset();
        this.replyingToCommentId = null;
        this.loadComments();
        this.isSubmitting = false;
      },
      error: (error) => {
        console.error('Error adding reply:', error);
        this.isSubmitting = false;
      }
    });
  }

  startEdit(comment: Comment): void {
    this.editingCommentId = comment.id;
    this.commentForm.patchValue({ content: comment.content });
  }

  cancelEdit(): void {
    this.editingCommentId = null;
    this.commentForm.reset();
  }

  submitEdit(): void {
    if (this.commentForm.invalid || !this.editingCommentId || this.isSubmitting) return;

    this.isSubmitting = true;
    const request: CommentRequest = {
      content: this.commentForm.value.content
    };

    this.commentService.updateComment(this.internshipId, this.editingCommentId, request).subscribe({
      next: () => {
        this.commentForm.reset();
        this.editingCommentId = null;
        this.loadComments();
        this.isSubmitting = false;
      },
      error: (error) => {
        console.error('Error updating comment:', error);
        this.isSubmitting = false;
      }
    });
  }

  deleteComment(commentId: number): void {
    if (!confirm('Are you sure you want to delete this comment?')) return;

    this.commentService.deleteComment(this.internshipId, commentId).subscribe({
      next: () => {
        this.loadComments();
      },
      error: (error) => {
        console.error('Error deleting comment:', error);
      }
    });
  }

  canEditOrDelete(comment: Comment): boolean {
    return this.currentUserId === comment.userId;
  }

  getRelativeTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  }

  getUserInitials(firstName: string, lastName: string): string {
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'STUDENT': return 'badge-student';
      case 'INSTRUCTOR': return 'badge-instructor';
      case 'ADMIN': return 'badge-admin';
      default: return 'badge-default';
    }
  }
}
