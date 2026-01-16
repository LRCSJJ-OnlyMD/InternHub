import { Injectable, OnDestroy } from '@angular/core';
import { Subject, BehaviorSubject } from 'rxjs';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';
import { NotificationService, Notification } from './notification.service';

export interface RealTimeNotification {
  id?: number;
  type: string;
  title: string;
  message: string;
  entityType?: string;
  entityId?: number;
  read: boolean;
  createdAt: string;
}

export interface UnreadCountUpdate {
  unreadCount: number;
  timestamp: string;
}

export interface SystemNotification {
  title: string;
  message: string;
  type: string;
  timestamp: string;
  isSystem: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class RealTimeNotificationService implements OnDestroy {
  private wsUrl = environment.wsUrl || `${environment.apiUrl.replace('/api', '')}/ws`;
  private stompClient: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private destroy$ = new Subject<void>();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;

  // Observable streams
  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  private notificationSubject = new Subject<RealTimeNotification>();
  private unreadCountSubject = new BehaviorSubject<number>(0);
  private systemNotificationSubject = new Subject<SystemNotification>();

  public connectionStatus$ = this.connectionStatusSubject.asObservable();
  public notification$ = this.notificationSubject.asObservable();
  public unreadCount$ = this.unreadCountSubject.asObservable();
  public systemNotification$ = this.systemNotificationSubject.asObservable();

  constructor(
    private authService: AuthService,
    private toastService: ToastService,
    private notificationService: NotificationService
  ) {
    // Auto-connect when user is logged in
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.connect();
      } else {
        this.disconnect();
      }
    });

    // Subscribe to notifications and show toasts
    this.notification$.subscribe(notification => {
      this.handleIncomingNotification(notification);
    });

    // Subscribe to system notifications
    this.systemNotification$.subscribe(notification => {
      this.handleSystemNotification(notification);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.disconnect();
  }

  /**
   * Connect to WebSocket for real-time notifications
   */
  connect(): void {
    const token = this.authService.getToken();
    if (!token) {
      console.error('No token available for notification WebSocket connection');
      return;
    }

    if (this.stompClient?.active) {
      return; // Already connected
    }

    try {
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS(`${this.wsUrl}?token=${token}`),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: (str) => {
          if (!environment.production) {
            console.log('Notification WS: ' + str);
          }
        }
      });

      this.stompClient.onConnect = () => {
        console.log('🔔 Notification WebSocket Connected');
        this.connectionStatusSubject.next(true);
        this.reconnectAttempts = 0;
        this.subscribeToNotifications();
      };

      this.stompClient.onDisconnect = () => {
        console.log('Notification WebSocket Disconnected');
        this.connectionStatusSubject.next(false);
      };

      this.stompClient.onStompError = (frame) => {
        console.error('Notification STOMP Error:', frame.headers['message']);
      };

      this.stompClient.onWebSocketError = (event) => {
        console.error('Notification WebSocket Error:', event);
        this.handleReconnect();
      };

      this.stompClient.activate();
    } catch (error) {
      console.error('Failed to initialize notification WebSocket:', error);
    }
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect(): void {
    if (this.stompClient) {
      this.subscriptions.forEach(sub => sub.unsubscribe());
      this.subscriptions.clear();
      this.stompClient.deactivate();
      this.stompClient = null;
      this.connectionStatusSubject.next(false);
    }
  }

  /**
   * Subscribe to notification channels
   */
  private subscribeToNotifications(): void {
    if (!this.stompClient || !this.stompClient.active) {
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    // Subscribe to personal notifications
    const notificationSub = this.stompClient.subscribe(
      '/user/queue/notifications',
      (message: IMessage) => {
        try {
          const notification: RealTimeNotification = JSON.parse(message.body);
          this.notificationSubject.next(notification);
        } catch (e) {
          console.error('Error parsing notification:', e);
        }
      }
    );
    this.subscriptions.set('notifications', notificationSub);

    // Subscribe to unread count updates
    const countSub = this.stompClient.subscribe(
      '/user/queue/notification-count',
      (message: IMessage) => {
        try {
          const update: UnreadCountUpdate = JSON.parse(message.body);
          this.unreadCountSubject.next(update.unreadCount);
        } catch (e) {
          console.error('Error parsing count update:', e);
        }
      }
    );
    this.subscriptions.set('notification-count', countSub);

    // Subscribe to toast notifications
    const toastSub = this.stompClient.subscribe(
      '/user/queue/toasts',
      (message: IMessage) => {
        try {
          const toast = JSON.parse(message.body);
          this.toastService.info(toast.title, toast.message, toast.duration || 5000);
        } catch (e) {
          console.error('Error parsing toast:', e);
        }
      }
    );
    this.subscriptions.set('toasts', toastSub);

    // Subscribe to system-wide notifications
    const systemSub = this.stompClient.subscribe(
      '/topic/system-notifications',
      (message: IMessage) => {
        try {
          const notification: SystemNotification = JSON.parse(message.body);
          this.systemNotificationSubject.next(notification);
        } catch (e) {
          console.error('Error parsing system notification:', e);
        }
      }
    );
    this.subscriptions.set('system-notifications', systemSub);

    console.log('📫 Subscribed to notification channels');
  }

  /**
   * Handle incoming notification
   */
  private handleIncomingNotification(notification: RealTimeNotification): void {
    // Show toast notification
    const type = this.mapNotificationType(notification.type);
    this.toastService.showWithAction(
      notification.title,
      notification.message,
      type,
      'View',
      () => {
        // Navigate to the relevant entity
        if (notification.entityType && notification.entityId) {
          this.navigateToEntity(notification.entityType, notification.entityId);
        }
      },
      8000
    );

    // Request browser notification permission and show if granted
    this.showBrowserNotification(notification);

    // Refresh the notification service count
    this.notificationService.refreshUnreadCount();
  }

  /**
   * Handle system notification
   */
  private handleSystemNotification(notification: SystemNotification): void {
    const type = notification.type.toLowerCase() as 'success' | 'error' | 'warning' | 'info';
    this.toastService.info(notification.title, notification.message, 10000);
  }

  /**
   * Map notification type to toast type
   */
  private mapNotificationType(type: string): 'success' | 'error' | 'warning' | 'info' {
    switch (type.toUpperCase()) {
      case 'VALIDATE':
      case 'INTERNSHIP_VALIDATED':
      case 'SUCCESS':
        return 'success';
      case 'REFUSE':
      case 'INTERNSHIP_REFUSED':
      case 'ERROR':
        return 'error';
      case 'DEADLINE':
      case 'WARNING':
        return 'warning';
      default:
        return 'info';
    }
  }

  /**
   * Navigate to the entity related to the notification
   */
  private navigateToEntity(entityType: string, entityId: number): void {
    // Import Router dynamically to avoid circular dependencies
    import('@angular/router').then(({ Router }) => {
      const injector = (window as any).ngRef?.injector;
      if (injector) {
        const router = injector.get(Router);
        switch (entityType.toUpperCase()) {
          case 'INTERNSHIP':
            router.navigate(['/internships', entityId]);
            break;
          case 'CONVERSATION':
            router.navigate(['/chat']);
            break;
          case 'DOCUMENT':
            router.navigate(['/library']);
            break;
        }
      }
    });
  }

  /**
   * Show browser notification (if permission granted)
   */
  private async showBrowserNotification(notification: RealTimeNotification): Promise<void> {
    if (!('Notification' in window)) {
      return;
    }

    try {
      if (Notification.permission === 'default') {
        await Notification.requestPermission();
      }

      if (Notification.permission === 'granted') {
        const browserNotification = new Notification(`InternHub: ${notification.title}`, {
          body: notification.message,
          icon: '/assets/images/logo/internhub-icon.png',
          badge: '/assets/images/logo/internhub-badge.png',
          tag: `notification-${notification.id}`,
          requireInteraction: false,
          silent: false
        });

        browserNotification.onclick = () => {
          window.focus();
          if (notification.entityType && notification.entityId) {
            this.navigateToEntity(notification.entityType, notification.entityId);
          }
          browserNotification.close();
        };

        // Auto-close after 10 seconds
        setTimeout(() => browserNotification.close(), 10000);
      }
    } catch (e) {
      console.error('Error showing browser notification:', e);
    }
  }

  /**
   * Handle reconnection logic
   */
  private handleReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Notification WS reconnect attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
    }
  }

  /**
   * Request browser notification permission
   */
  async requestNotificationPermission(): Promise<NotificationPermission> {
    if (!('Notification' in window)) {
      return 'denied';
    }

    if (Notification.permission === 'default') {
      return await Notification.requestPermission();
    }

    return Notification.permission;
  }

  /**
   * Check if browser notifications are supported and enabled
   */
  areBrowserNotificationsEnabled(): boolean {
    return 'Notification' in window && Notification.permission === 'granted';
  }
}
