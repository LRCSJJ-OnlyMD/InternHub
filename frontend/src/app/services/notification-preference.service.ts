import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../shared/api-config';

export interface NotificationPreference {
  id?: number;
  notificationType: NotificationType;
  emailEnabled: boolean;
  pushEnabled: boolean;
  inAppEnabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export enum NotificationType {
  INTERNSHIP_STATUS_CHANGE = 'INTERNSHIP_STATUS_CHANGE',
  INTERNSHIP_ASSIGNED = 'INTERNSHIP_ASSIGNED',
  INTERNSHIP_VALIDATED = 'INTERNSHIP_VALIDATED',
  INTERNSHIP_REJECTED = 'INTERNSHIP_REJECTED',
  NEW_COMMENT = 'NEW_COMMENT',
  DEADLINE_REMINDER = 'DEADLINE_REMINDER',
  REPORT_UPLOADED = 'REPORT_UPLOADED',
  SYSTEM_ANNOUNCEMENT = 'SYSTEM_ANNOUNCEMENT'
}

export const NOTIFICATION_TYPE_LABELS: Record<NotificationType, string> = {
  [NotificationType.INTERNSHIP_STATUS_CHANGE]: 'Internship Status Changes',
  [NotificationType.INTERNSHIP_ASSIGNED]: 'Internship Assigned to Me',
  [NotificationType.INTERNSHIP_VALIDATED]: 'Internship Validated',
  [NotificationType.INTERNSHIP_REJECTED]: 'Internship Rejected',
  [NotificationType.NEW_COMMENT]: 'New Comments',
  [NotificationType.DEADLINE_REMINDER]: 'Deadline Reminders',
  [NotificationType.REPORT_UPLOADED]: 'Report Uploaded',
  [NotificationType.SYSTEM_ANNOUNCEMENT]: 'System Announcements'
};

@Injectable({
  providedIn: 'root'
})
export class NotificationPreferenceService {
  private apiUrl = `${API_CONFIG.BASE_URL}/api/notification-preferences`;

  constructor(private http: HttpClient) {}

  getUserPreferences(): Observable<NotificationPreference[]> {
    return this.http.get<NotificationPreference[]>(this.apiUrl);
  }

  getPreference(notificationType: NotificationType): Observable<NotificationPreference> {
    return this.http.get<NotificationPreference>(`${this.apiUrl}/${notificationType}`);
  }

  updatePreference(preference: NotificationPreference): Observable<NotificationPreference> {
    return this.http.post<NotificationPreference>(this.apiUrl, preference);
  }

  resetToDefaults(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reset`, {});
  }

  deletePreference(notificationType: NotificationType): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationType}`);
  }
}
