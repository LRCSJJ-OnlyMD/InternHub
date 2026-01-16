import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:timeago/timeago.dart' as timeago;
import '../../models/notification.dart';
import '../../providers/notification_provider.dart';

class NotificationsScreen extends ConsumerStatefulWidget {
  const NotificationsScreen({Key? key}) : super(key: key);

  @override
  ConsumerState<NotificationsScreen> createState() =>
      _NotificationsScreenState();
}

class _NotificationsScreenState extends ConsumerState<NotificationsScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  String _selectedFilter = 'all';

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final notificationsAsync = ref.watch(notificationsProvider);
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        elevation: 0,
        backgroundColor: theme.primaryColor,
        foregroundColor: Colors.white,
        title: const Text(
          'Notifications',
          style: TextStyle(fontWeight: FontWeight.w600),
        ),
        actions: [
          // Settings button
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            tooltip: 'Notification Settings',
            onPressed: () => context.push('/notification-preferences'),
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
                  await ref
                      .read(notificationsProvider.notifier)
                      .markAllAsRead();
                  if (context.mounted) {
                    _showSnackBar(
                      context,
                      '✅ All notifications marked as read',
                      Colors.green,
                    );
                  }
                },
              );
            },
            loading: () => const SizedBox.shrink(),
            error: (_, __) => const SizedBox.shrink(),
          ),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(60),
          child: Container(
            color: Colors.white,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Row(
                children: [
                  _buildFilterChip('All', 'all', Icons.all_inbox),
                  const SizedBox(width: 8),
                  _buildFilterChip('Unread', 'unread', Icons.markunread),
                  const SizedBox(width: 8),
                  _buildFilterChip(
                    'Internships',
                    'internship',
                    Icons.work_outline,
                  ),
                  const SizedBox(width: 8),
                  _buildFilterChip(
                    'Messages',
                    'message',
                    Icons.chat_bubble_outline,
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          await ref.read(notificationsProvider.notifier).refresh();
        },
        child: notificationsAsync.when(
          data: (notifications) =>
              _buildNotificationsList(context, ref, notifications),
          loading: () => _buildLoadingState(),
          error: (error, stack) => _buildErrorState(context, ref, error),
        ),
      ),
    );
  }

  Widget _buildFilterChip(String label, String filter, IconData icon) {
    final isSelected = _selectedFilter == filter;
    return FilterChip(
      selected: isSelected,
      label: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            icon,
            size: 16,
            color: isSelected ? Colors.white : Colors.grey[700],
          ),
          const SizedBox(width: 4),
          Text(
            label,
            style: TextStyle(
              fontSize: 12,
              color: isSelected ? Colors.white : Colors.grey[700],
            ),
          ),
        ],
      ),
      backgroundColor: Colors.grey[200],
      selectedColor: Theme.of(context).primaryColor,
      checkmarkColor: Colors.white,
      showCheckmark: false,
      onSelected: (selected) {
        setState(() {
          _selectedFilter = filter;
        });
      },
    );
  }

  Widget _buildNotificationsList(
    BuildContext context,
    WidgetRef ref,
    List<AppNotification> notifications,
  ) {
    // Apply filter
    final filteredNotifications = _filterNotifications(notifications);

    if (filteredNotifications.isEmpty) {
      return _buildEmptyState();
    }

    // Group notifications by date
    final today = DateTime.now();
    final yesterday = today.subtract(const Duration(days: 1));

    final todayNotifications = filteredNotifications
        .where((n) => _isSameDay(n.createdAt, today))
        .toList();
    final yesterdayNotifications = filteredNotifications
        .where((n) => _isSameDay(n.createdAt, yesterday))
        .toList();
    final thisWeekNotifications = filteredNotifications
        .where(
          (n) =>
              !_isSameDay(n.createdAt, today) &&
              !_isSameDay(n.createdAt, yesterday) &&
              n.createdAt.isAfter(today.subtract(const Duration(days: 7))),
        )
        .toList();
    final olderNotifications = filteredNotifications
        .where(
          (n) => n.createdAt.isBefore(today.subtract(const Duration(days: 7))),
        )
        .toList();

    return ListView(
      padding: const EdgeInsets.only(bottom: 16),
      children: [
        if (todayNotifications.isNotEmpty) ...[
          _buildSectionHeader('Today', todayNotifications.length),
          ...todayNotifications.map(
            (n) => _buildNotificationCard(context, ref, n),
          ),
        ],
        if (yesterdayNotifications.isNotEmpty) ...[
          _buildSectionHeader('Yesterday', yesterdayNotifications.length),
          ...yesterdayNotifications.map(
            (n) => _buildNotificationCard(context, ref, n),
          ),
        ],
        if (thisWeekNotifications.isNotEmpty) ...[
          _buildSectionHeader('This Week', thisWeekNotifications.length),
          ...thisWeekNotifications.map(
            (n) => _buildNotificationCard(context, ref, n),
          ),
        ],
        if (olderNotifications.isNotEmpty) ...[
          _buildSectionHeader('Older', olderNotifications.length),
          ...olderNotifications.map(
            (n) => _buildNotificationCard(context, ref, n),
          ),
        ],
      ],
    );
  }

  List<AppNotification> _filterNotifications(
    List<AppNotification> notifications,
  ) {
    switch (_selectedFilter) {
      case 'unread':
        return notifications.where((n) => !n.read).toList();
      case 'internship':
        return notifications
            .where(
              (n) =>
                  n.type.contains('INTERNSHIP') || n.entityType == 'INTERNSHIP',
            )
            .toList();
      case 'message':
        return notifications
            .where(
              (n) =>
                  n.type.contains('CHAT') ||
                  n.type.contains('COMMENT') ||
                  n.type.contains('MESSAGE'),
            )
            .toList();
      default:
        return notifications;
    }
  }

  Widget _buildSectionHeader(String title, int count) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        children: [
          Text(
            title,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: Colors.grey[600],
              letterSpacing: 0.5,
            ),
          ),
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
            decoration: BoxDecoration(
              color: Colors.grey[300],
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              count.toString(),
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w500,
                color: Colors.grey[700],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildNotificationCard(
    BuildContext context,
    WidgetRef ref,
    AppNotification notification,
  ) {
    final colors = _getNotificationColors(notification.type);

    return Dismissible(
      key: Key('notification_${notification.id}'),
      direction: DismissDirection.endToStart,
      background: Container(
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
        decoration: BoxDecoration(
          color: Colors.red,
          borderRadius: BorderRadius.circular(12),
        ),
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 20),
        child: const Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.delete_outline, color: Colors.white, size: 24),
            SizedBox(height: 4),
            Text('Delete', style: TextStyle(color: Colors.white, fontSize: 12)),
          ],
        ),
      ),
      confirmDismiss: (direction) async {
        return await showDialog<bool>(
              context: context,
              builder: (context) => AlertDialog(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
                title: const Row(
                  children: [
                    Icon(Icons.delete_outline, color: Colors.red),
                    SizedBox(width: 8),
                    Text('Delete Notification'),
                  ],
                ),
                content: const Text(
                  'Are you sure you want to delete this notification?',
                ),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.of(context).pop(false),
                    child: const Text('Cancel'),
                  ),
                  ElevatedButton(
                    onPressed: () => Navigator.of(context).pop(true),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('Delete'),
                  ),
                ],
              ),
            ) ??
            false;
      },
      onDismissed: (direction) async {
        await ref
            .read(notificationsProvider.notifier)
            .deleteNotification(notification.id);
        if (context.mounted) {
          _showSnackBar(context, '🗑️ Notification deleted', Colors.grey);
        }
      },
      child: GestureDetector(
        onTap: () async {
          if (!notification.read) {
            await ref
                .read(notificationsProvider.notifier)
                .markAsRead(notification.id);
          }
          if (notification.entityType == 'INTERNSHIP' &&
              notification.entityId != null &&
              context.mounted) {
            context.pop();
          }
        },
        child: Container(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
            border: notification.read
                ? null
                : Border.all(
                    color: colors['primary']!.withOpacity(0.3),
                    width: 1,
                  ),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.04),
                blurRadius: 8,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Notification Icon
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: colors['background'],
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Center(
                    child: Icon(
                      colors['icon'] as IconData,
                      color: colors['primary'],
                      size: 24,
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
                                fontWeight: notification.read
                                    ? FontWeight.w500
                                    : FontWeight.w600,
                                color: Colors.grey[900],
                              ),
                            ),
                          ),
                          if (!notification.read)
                            Container(
                              width: 10,
                              height: 10,
                              decoration: BoxDecoration(
                                color: colors['primary'],
                                shape: BoxShape.circle,
                              ),
                            ),
                        ],
                      ),
                      const SizedBox(height: 6),
                      Text(
                        notification.message,
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey[600],
                          height: 1.4,
                        ),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Icon(
                            Icons.access_time,
                            size: 14,
                            color: Colors.grey[400],
                          ),
                          const SizedBox(width: 4),
                          Text(
                            timeago.format(notification.createdAt),
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey[400],
                            ),
                          ),
                          const Spacer(),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 8,
                              vertical: 2,
                            ),
                            decoration: BoxDecoration(
                              color: colors['background'],
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              _getNotificationCategory(notification.type),
                              style: TextStyle(
                                fontSize: 10,
                                fontWeight: FontWeight.w500,
                                color: colors['primary'],
                              ),
                            ),
                          ),
                        ],
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

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            width: 100,
            height: 100,
            decoration: BoxDecoration(
              color: Colors.grey[100],
              shape: BoxShape.circle,
            ),
            child: Icon(
              Icons.notifications_off_outlined,
              size: 50,
              color: Colors.grey[400],
            ),
          ),
          const SizedBox(height: 24),
          Text(
            'No notifications',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w600,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'You\'re all caught up! Check back later.',
            style: TextStyle(fontSize: 14, color: Colors.grey[500]),
          ),
        ],
      ),
    );
  }

  Widget _buildLoadingState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const CircularProgressIndicator(),
          const SizedBox(height: 16),
          Text(
            'Loading notifications...',
            style: TextStyle(fontSize: 14, color: Colors.grey[600]),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(BuildContext context, WidgetRef ref, Object error) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                color: Colors.red[50],
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.error_outline,
                size: 40,
                color: Colors.red[400],
              ),
            ),
            const SizedBox(height: 24),
            Text(
              'Something went wrong',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w600,
                color: Colors.grey[800],
              ),
            ),
            const SizedBox(height: 8),
            Text(
              error.toString(),
              style: TextStyle(fontSize: 14, color: Colors.grey[600]),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: () =>
                  ref.read(notificationsProvider.notifier).refresh(),
              icon: const Icon(Icons.refresh),
              label: const Text('Retry'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(
                  horizontal: 24,
                  vertical: 12,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Map<String, dynamic> _getNotificationColors(String type) {
    switch (type.toUpperCase()) {
      case 'INTERNSHIP_VALIDATED':
      case 'VALIDATE':
        return {
          'primary': Colors.green,
          'background': Colors.green[50],
          'icon': Icons.check_circle_outline,
        };
      case 'INTERNSHIP_REFUSED':
      case 'REFUSE':
        return {
          'primary': Colors.red,
          'background': Colors.red[50],
          'icon': Icons.cancel_outlined,
        };
      case 'NEW_COMMENT':
      case 'COMMENT_ADDED':
        return {
          'primary': Colors.blue,
          'background': Colors.blue[50],
          'icon': Icons.chat_bubble_outline,
        };
      case 'CHAT_MESSAGE':
        return {
          'primary': Colors.indigo,
          'background': Colors.indigo[50],
          'icon': Icons.message_outlined,
        };
      case 'DOCUMENT_UPLOADED':
      case 'REPORT_UPLOADED':
        return {
          'primary': Colors.orange,
          'background': Colors.orange[50],
          'icon': Icons.upload_file_outlined,
        };
      case 'DEADLINE_REMINDER':
      case 'DEADLINE':
        return {
          'primary': Colors.amber[700],
          'background': Colors.amber[50],
          'icon': Icons.alarm_outlined,
        };
      case 'ASSIGNMENT':
      case 'CLAIM':
        return {
          'primary': Colors.purple,
          'background': Colors.purple[50],
          'icon': Icons.assignment_ind_outlined,
        };
      default:
        return {
          'primary': Colors.grey[700],
          'background': Colors.grey[100],
          'icon': Icons.notifications_outlined,
        };
    }
  }

  String _getNotificationCategory(String type) {
    switch (type.toUpperCase()) {
      case 'INTERNSHIP_VALIDATED':
      case 'VALIDATE':
        return 'Validated';
      case 'INTERNSHIP_REFUSED':
      case 'REFUSE':
        return 'Refused';
      case 'NEW_COMMENT':
      case 'COMMENT_ADDED':
        return 'Comment';
      case 'CHAT_MESSAGE':
        return 'Message';
      case 'DOCUMENT_UPLOADED':
      case 'REPORT_UPLOADED':
        return 'Document';
      case 'DEADLINE_REMINDER':
      case 'DEADLINE':
        return 'Deadline';
      case 'ASSIGNMENT':
      case 'CLAIM':
        return 'Assignment';
      default:
        return 'Notification';
    }
  }

  bool _isSameDay(DateTime date1, DateTime date2) {
    return date1.year == date2.year &&
        date1.month == date2.month &&
        date1.day == date2.day;
  }

  void _showSnackBar(BuildContext context, String message, Color color) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: color,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        margin: const EdgeInsets.all(16),
      ),
    );
  }
}
