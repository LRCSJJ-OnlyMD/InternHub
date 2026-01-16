import { Component, OnInit, OnDestroy, HostListener, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationService, Notification } from '../../services/notification.service';
import { RealTimeNotificationService } from '../../services/realtime-notification.service';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.css']
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  @ViewChild('dropdownMenu') dropdownMenu!: ElementRef;
  
  unreadCount = 0;
  notifications: Notification[] = [];
  showDropdown = false;
  isLoading = false;
  isConnected = false;
  private subscription = new Subscription();

  constructor(
    private notificationService: NotificationService,
    private realTimeService: RealTimeNotificationService,
    private router: Router,
    private elementRef: ElementRef
  ) {}

  ngOnInit(): void {
    // Subscribe to unread count from regular service
    this.subscription.add(
      this.notificationService.unreadCount$.subscribe(count => {
        this.unreadCount = count;
      })
    );

    // Subscribe to real-time unread count updates
    this.subscription.add(
      this.realTimeService.unreadCount$.subscribe(count => {
        if (count > 0) {
          this.unreadCount = count;
        }
      })
    );

    // Subscribe to connection status
    this.subscription.add(
      this.realTimeService.connectionStatus$.subscribe(connected => {
        this.isConnected = connected;
      })
    );

    // Subscribe to real-time notifications to refresh the list
    this.subscription.add(
      this.realTimeService.notification$.subscribe(() => {
        if (this.showDropdown) {
          this.loadNotifications();
        }
      })
    );
    
    this.notificationService.refreshUnreadCount();

    // Request browser notification permission
    this.realTimeService.requestNotificationPermission();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
    if (this.showDropdown) {
      this.loadNotifications();
    }
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.notificationService.getNotifications(0, 10).subscribe({
      next: (response) => {
        this.notifications = response.content || response;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
        this.isLoading = false;
      }
    });
  }

  markAsRead(notification: Notification, event: Event): void {
    event.stopPropagation();
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.read = true;
          this.handleNotificationClick(notification);
        },
        error: (error) => {
          console.error('Error marking notification as read:', error);
        }
      });
    } else {
      this.handleNotificationClick(notification);
    }
  }

  markAllAsRead(event: Event): void {
    event.stopPropagation();
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
      },
      error: (error) => {
        console.error('Error marking all as read:', error);
      }
    });
  }

  deleteNotification(notificationId: number, event: Event): void {
    event.stopPropagation();
    this.notificationService.deleteNotification(notificationId).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== notificationId);
        this.notificationService.refreshUnreadCount();
      },
      error: (error) => {
        console.error('Error deleting notification:', error);
      }
    });
  }

  handleNotificationClick(notification: Notification): void {
    this.showDropdown = false;
    
    // Navigate based on notification entity type
    if (notification.entityType === 'INTERNSHIP' && notification.entityId) {
      this.router.navigate(['/internships', notification.entityId]);
    }
  }

  getNotificationIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'INFO': '📢',
      'SUCCESS': '✅',
      'WARNING': '⚠️',
      'ERROR': '❌',
      'INTERNSHIP_SUBMITTED': '📝',
      'INTERNSHIP_CLAIMED': '🎯',
      'INTERNSHIP_VALIDATED': '✅',
      'INTERNSHIP_REFUSED': '❌',
      'COMMENT_ADDED': '💬'
    };
    return icons[type] || '📢';
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

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.showDropdown = false;
    }
  }
}
