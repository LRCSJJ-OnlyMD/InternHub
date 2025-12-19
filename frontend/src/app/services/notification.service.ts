import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, interval } from 'rxjs';
import { tap, switchMap, startWith } from 'rxjs/operators';
import { API_CONFIG } from '../shared/api-config';

export interface Notification {
  id: number;
  type: string;
  title: string;
  message: string;
  entityType?: string;
  entityId?: number;
  read: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {
    // Poll for new notifications every 30 seconds
    interval(30000)
      .pipe(
        startWith(0),
        switchMap(() => this.getUnreadCount())
      )
      .subscribe();
  }

  getNotifications(page: number = 0, size: number = 20): Observable<any> {
    return this.http.get(`${API_CONFIG.NOTIFICATIONS}?page=${page}&size=${size}`);
  }

  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${API_CONFIG.NOTIFICATIONS}/unread`);
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${API_CONFIG.NOTIFICATIONS}/unread/count`).pipe(
      tap(response => this.unreadCountSubject.next(response.count))
    );
  }

  markAsRead(notificationId: number): Observable<any> {
    return this.http.put(`${API_CONFIG.NOTIFICATIONS}/${notificationId}/read`, {}).pipe(
      tap(() => {
        const currentCount = this.unreadCountSubject.value;
        this.unreadCountSubject.next(Math.max(0, currentCount - 1));
      })
    );
  }

  markAllAsRead(): Observable<any> {
    return this.http.put(`${API_CONFIG.NOTIFICATIONS}/mark-all-read`, {}).pipe(
      tap(() => this.unreadCountSubject.next(0))
    );
  }

  deleteNotification(notificationId: number): Observable<any> {
    return this.http.delete(`${API_CONFIG.NOTIFICATIONS}/${notificationId}`);
  }

  refreshUnreadCount(): void {
    this.getUnreadCount().subscribe();
  }
}
