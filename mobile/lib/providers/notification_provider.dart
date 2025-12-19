import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/notification.dart';
import '../services/notification_service.dart';

// Provider for NotificationService
final notificationServiceProvider = Provider<NotificationService>((ref) {
  return NotificationService();
});

// Notification state notifier
class NotificationNotifier extends StateNotifier<AsyncValue<List<AppNotification>>> {
  final NotificationService _service;

  NotificationNotifier(this._service) : super(const AsyncValue.loading()) {
    loadNotifications();
  }

  Future<void> loadNotifications({int page = 0, int size = 100}) async {
    state = const AsyncValue.loading();
    try {
      final notificationPage = await _service.getNotifications(page: page, size: size);
      state = AsyncValue.data(notificationPage.notifications);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> markAsRead(int notificationId) async {
    try {
      await _service.markAsRead(notificationId);
      
      // Update local state
      state.whenData((notifications) {
        final updatedNotifications = notifications.map((notification) {
          if (notification.id == notificationId) {
            return notification.copyWith(read: true);
          }
          return notification;
        }).toList();
        state = AsyncValue.data(updatedNotifications);
      });
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> markAllAsRead() async {
    try {
      await _service.markAllAsRead();
      
      // Update local state
      state.whenData((notifications) {
        final updatedNotifications = notifications.map((notification) {
          return notification.copyWith(read: true);
        }).toList();
        state = AsyncValue.data(updatedNotifications);
      });
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> deleteNotification(int notificationId) async {
    try {
      await _service.deleteNotification(notificationId);
      
      // Remove from local state
      state.whenData((notifications) {
        final updatedNotifications = notifications
            .where((notification) => notification.id != notificationId)
            .toList();
        state = AsyncValue.data(updatedNotifications);
      });
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> refresh() async {
    await loadNotifications();
  }
}

// Provider for notification list
final notificationsProvider = StateNotifierProvider<NotificationNotifier, AsyncValue<List<AppNotification>>>((ref) {
  final service = ref.watch(notificationServiceProvider);
  return NotificationNotifier(service);
});

// Provider for unread count
final unreadCountProvider = StreamProvider<int>((ref) {
  final service = ref.watch(notificationServiceProvider);
  
  // Poll every 30 seconds
  return Stream.periodic(const Duration(seconds: 30), (_) async {
    try {
      return await service.getUnreadCount();
    } catch (e) {
      return 0;
    }
  }).asyncMap((event) => event);
});

// Immediate unread count provider
final unreadCountImmediateProvider = FutureProvider<int>((ref) async {
  final service = ref.watch(notificationServiceProvider);
  try {
    return await service.getUnreadCount();
  } catch (e) {
    return 0;
  }
});
