import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { 
  NotificationPreferenceService, 
  NotificationPreference,
  NotificationType,
  NOTIFICATION_TYPE_LABELS 
} from '../../services/notification-preference.service';

@Component({
  selector: 'app-notification-preferences',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './notification-preferences.component.html',
  styleUrls: ['./notification-preferences.component.css']
})
export class NotificationPreferencesComponent implements OnInit {
  preferences: NotificationPreference[] = [];
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;
  notificationTypes = Object.values(NotificationType);
  
  getLabel = (type: NotificationType) => NOTIFICATION_TYPE_LABELS[type];

  constructor(
    private preferenceService: NotificationPreferenceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPreferences();
  }

  loadPreferences(): void {
    this.loading = true;
    this.error = null;

    this.preferenceService.getUserPreferences().subscribe({
      next: (preferences) => {
        this.preferences = preferences;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load notification preferences';
        this.loading = false;
        console.error('Error loading preferences:', err);
      }
    });
  }

  toggleChannel(preference: NotificationPreference, channel: 'email' | 'push' | 'inApp'): void {
    switch (channel) {
      case 'email':
        preference.emailEnabled = !preference.emailEnabled;
        break;
      case 'push':
        preference.pushEnabled = !preference.pushEnabled;
        break;
      case 'inApp':
        preference.inAppEnabled = !preference.inAppEnabled;
        break;
    }
    this.updatePreference(preference);
  }

  updatePreference(preference: NotificationPreference): void {
    this.preferenceService.updatePreference(preference).subscribe({
      next: (updated) => {
        const index = this.preferences.findIndex(p => p.id === updated.id);
        if (index !== -1) {
          this.preferences[index] = updated;
        }
        this.showSuccess('Preference updated successfully');
      },
      error: (err) => {
        this.error = 'Failed to update preference';
        console.error('Error updating preference:', err);
        this.loadPreferences(); // Reload to revert changes
      }
    });
  }

  resetToDefaults(): void {
    if (!confirm('Are you sure you want to reset all preferences to defaults?')) {
      return;
    }

    this.loading = true;
    this.preferenceService.resetToDefaults().subscribe({
      next: () => {
        this.showSuccess('Preferences reset to defaults');
        this.loadPreferences();
      },
      error: (err) => {
        this.error = 'Failed to reset preferences';
        this.loading = false;
        console.error('Error resetting preferences:', err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      this.successMessage = null;
    }, 3000);
  }

  getNotificationIcon(type: NotificationType): string {
    const icons: Record<NotificationType, string> = {
      [NotificationType.INTERNSHIP_STATUS_CHANGE]: '🔄',
      [NotificationType.INTERNSHIP_ASSIGNED]: '📋',
      [NotificationType.INTERNSHIP_VALIDATED]: '✅',
      [NotificationType.INTERNSHIP_REJECTED]: '❌',
      [NotificationType.NEW_COMMENT]: '💬',
      [NotificationType.DEADLINE_REMINDER]: '⏰',
      [NotificationType.REPORT_UPLOADED]: '📄',
      [NotificationType.SYSTEM_ANNOUNCEMENT]: '📢'
    };
    return icons[type] || '🔔';
  }
}
