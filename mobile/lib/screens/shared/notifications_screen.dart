import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:timeago/timeago.dart' as timeago;
import '../../models/notification.dart';
import '../../providers/notification_provider.dart';

class NotificationsScreen extends ConsumerWidget {
  const NotificationsScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notificationsAsync = ref.watch(notificationsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notifications'),
        actions: [
          // Settings button
          IconButton(
            icon: const Icon(Icons.settings),
            tooltip: 'Notification preferences',
            onPressed: () {
              context.push('/notification-preferences');
            },
          ),
          // Mark all as read button
          notificationsAsync.when(
            data: (notifications) {
              final hasUnread = notifications.any((n) => !n.read);
              if (!hasUnread) return const SizedBox.shrink();
              
              return IconButton(
                icon: const Icon(Icons.done_all),
                tooltip: 'Mark all as read',
                onPressed: () async {
                  await ref.read(notificationsProvider.notifier).markAllAsRead();
                  if (context.mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('All notifications marked as read')),
                    );
                  }
                },
              );
            },
            loading: () => const SizedBox.shrink(),
            error: (_, __) => const SizedBox.shrink(),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          await ref.read(notificationsProvider.notifier).refresh();
        },
        child: notificationsAsync.when(
          data: (notifications) {
            if (notifications.isEmpty) {
              return Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(
                      Icons.notifications_off_outlined,
                      size: 80,
                      color: Colors.grey[400],
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'No notifications',
                      style: TextStyle(
                        fontSize: 18,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              );
            }

            // Group notifications by date
            final today = DateTime.now();
            final yesterday = today.subtract(const Duration(days: 1));
            
            final todayNotifications = notifications.where((n) => _isSameDay(n.createdAt, today)).toList();
            final yesterdayNotifications = notifications.where((n) => _isSameDay(n.createdAt, yesterday)).toList();
            final olderNotifications = notifications.where((n) => 
              !_isSameDay(n.createdAt, today) && !_isSameDay(n.createdAt, yesterday)
            ).toList();

            return ListView(
              padding: const EdgeInsets.symmetric(vertical: 8),
              children: [
                if (todayNotifications.isNotEmpty) ...[
                  _buildSectionHeader('Today'),
                  ...todayNotifications.map((n) => _buildNotificationItem(context, ref, n)),
                ],
                if (yesterdayNotifications.isNotEmpty) ...[
                  _buildSectionHeader('Yesterday'),
                  ...yesterdayNotifications.map((n) => _buildNotificationItem(context, ref, n)),
                ],
                if (olderNotifications.isNotEmpty) ...[
                  _buildSectionHeader('Older'),
                  ...olderNotifications.map((n) => _buildNotificationItem(context, ref, n)),
                ],
              ],
            );
          },
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (error, stack) => Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.error_outline, size: 60, color: Colors.red),
                const SizedBox(height: 16),
                Text(
                  'Error loading notifications',
                  style: TextStyle(fontSize: 16, color: Colors.grey[700]),
                ),
                const SizedBox(height: 8),
                Text(
                  error.toString(),
                  style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),
                ElevatedButton.icon(
                  onPressed: () => ref.read(notificationsProvider.notifier).refresh(),
                  icon: const Icon(Icons.refresh),
                  label: const Text('Retry'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Text(
        title,
        style: const TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: Colors.grey,
        ),
      ),
    );
  }

  Widget _buildNotificationItem(BuildContext context, WidgetRef ref, AppNotification notification) {
    final iconColor = _getNotificationColor(notification.type);
    
    return Dismissible(
      key: Key('notification_${notification.id}'),
      direction: DismissDirection.endToStart,
      background: Container(
        color: Colors.red,
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 20),
        child: const Icon(Icons.delete, color: Colors.white),
      ),
      confirmDismiss: (direction) async {
        return await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Delete Notification'),
            content: const Text('Are you sure you want to delete this notification?'),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: const Text('Cancel'),
              ),
              TextButton(
                onPressed: () => Navigator.of(context).pop(true),
                style: TextButton.styleFrom(foregroundColor: Colors.red),
                child: const Text('Delete'),
              ),
            ],
          ),
        ) ?? false;
      },
      onDismissed: (direction) async {
        await ref.read(notificationsProvider.notifier).deleteNotification(notification.id);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Notification deleted')),
          );
        }
      },
      child: InkWell(
        onTap: () async {
          // Mark as read if unread
          if (!notification.read) {
            await ref.read(notificationsProvider.notifier).markAsRead(notification.id);
          }
          
          // Navigate to related entity if available
          if (notification.entityType == 'INTERNSHIP' && notification.entityId != null && context.mounted) {
            // Navigate to internship detail
            // Note: We need to fetch the full internship object first
            context.pop(); // Close notifications screen
            // TODO: Navigate to internship detail with entityId
          }
        },
        child: Container(
          color: notification.read ? Colors.transparent : Colors.blue.withOpacity(0.05),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Icon
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: iconColor.withOpacity(0.1),
                    shape: BoxShape.circle,
                  ),
                  child: Center(
                    child: Text(
                      notification.getIcon(),
                      style: const TextStyle(fontSize: 20),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                // Content
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: Text(
                              notification.title,
                              style: TextStyle(
                                fontSize: 15,
                                fontWeight: notification.read ? FontWeight.normal : FontWeight.bold,
                              ),
                            ),
                          ),
                          if (!notification.read)
                            Container(
                              width: 8,
                              height: 8,
                              decoration: const BoxDecoration(
                                color: Colors.blue,
                                shape: BoxShape.circle,
                              ),
                            ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        notification.message,
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey[700],
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        timeago.format(notification.createdAt),
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey[500],
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  bool _isSameDay(DateTime date1, DateTime date2) {
    return date1.year == date2.year &&
        date1.month == date2.month &&
        date1.day == date2.day;
  }

  Color _getNotificationColor(String type) {
    switch (type.toUpperCase()) {
      case 'INTERNSHIP_VALIDATED':
        return Colors.green;
      case 'INTERNSHIP_REFUSED':
        return Colors.red;
      case 'NEW_COMMENT':
        return Colors.blue;
      case 'DOCUMENT_UPLOADED':
        return Colors.orange;
      case 'DEADLINE_REMINDER':
        return Colors.amber;
      case 'ASSIGNMENT':
        return Colors.purple;
      default:
        return Colors.grey;
    }
  }
}
