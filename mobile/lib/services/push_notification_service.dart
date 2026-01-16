import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/notification.dart';

/// Provider for PushNotificationService
final pushNotificationServiceProvider = Provider<PushNotificationService>((
  ref,
) {
  return PushNotificationService();
});

/// Service for handling local push notifications on the device
class PushNotificationService {
  static final PushNotificationService _instance =
      PushNotificationService._internal();
  factory PushNotificationService() => _instance;
  PushNotificationService._internal();

  final FlutterLocalNotificationsPlugin _notificationsPlugin =
      FlutterLocalNotificationsPlugin();

  bool _isInitialized = false;

  // Notification channel details for Android
  static const String _channelId = 'internhub_notifications';
  static const String _channelName = 'InternHub Notifications';
  static const String _channelDescription = 'Notifications from InternHub app';

  // Notification IDs for different types
  static const int _defaultNotificationId = 0;
  static const int _chatNotificationId = 1;
  static const int _internshipNotificationId = 2;
  static const int _systemNotificationId = 3;

  /// Initialize the notification plugin
  Future<void> initialize() async {
    if (_isInitialized) return;

    // Android initialization settings
    const androidInitSettings = AndroidInitializationSettings(
      '@mipmap/ic_launcher',
    );

    // iOS initialization settings (for future iOS support)
    const iosInitSettings = DarwinInitializationSettings(
      requestAlertPermission: true,
      requestBadgePermission: true,
      requestSoundPermission: true,
    );

    const initSettings = InitializationSettings(
      android: androidInitSettings,
      iOS: iosInitSettings,
    );

    await _notificationsPlugin.initialize(
      initSettings,
      onDidReceiveNotificationResponse: _onNotificationTapped,
      onDidReceiveBackgroundNotificationResponse:
          _onBackgroundNotificationTapped,
    );

    // Create notification channel for Android
    if (Platform.isAndroid) {
      await _createNotificationChannel();
    }

    _isInitialized = true;
    debugPrint('🔔 Push notification service initialized');
  }

  /// Create Android notification channel
  Future<void> _createNotificationChannel() async {
    const channel = AndroidNotificationChannel(
      _channelId,
      _channelName,
      description: _channelDescription,
      importance: Importance.high,
      playSound: true,
      enableVibration: true,
      showBadge: true,
    );

    await _notificationsPlugin
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >()
        ?.createNotificationChannel(channel);
  }

  /// Request notification permissions
  Future<bool> requestPermissions() async {
    if (Platform.isAndroid) {
      final android = _notificationsPlugin
          .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin
          >();
      final granted = await android?.requestNotificationsPermission();
      return granted ?? false;
    } else if (Platform.isIOS) {
      final ios = _notificationsPlugin
          .resolvePlatformSpecificImplementation<
            IOSFlutterLocalNotificationsPlugin
          >();
      final granted = await ios?.requestPermissions(
        alert: true,
        badge: true,
        sound: true,
      );
      return granted ?? false;
    }
    return false;
  }

  /// Show a notification from an AppNotification model
  Future<void> showNotification(AppNotification notification) async {
    if (!_isInitialized) {
      await initialize();
    }

    final notificationId = _getNotificationId(notification.type);
    final icon = _getNotificationIcon(notification.type);

    final androidDetails = AndroidNotificationDetails(
      _channelId,
      _channelName,
      channelDescription: _channelDescription,
      importance: Importance.high,
      priority: Priority.high,
      icon: icon,
      largeIcon: const DrawableResourceAndroidBitmap('@mipmap/ic_launcher'),
      styleInformation: BigTextStyleInformation(
        notification.message,
        contentTitle: notification.title,
        summaryText: _getNotificationCategory(notification.type),
      ),
      category: AndroidNotificationCategory.message,
      autoCancel: true,
      playSound: true,
      enableVibration: true,
    );

    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );

    final details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    // Encode notification data for tap handling
    final payload = jsonEncode({
      'id': notification.id,
      'type': notification.type,
      'entityType': notification.entityType,
      'entityId': notification.entityId,
    });

    await _notificationsPlugin.show(
      notificationId,
      notification.title,
      notification.message,
      details,
      payload: payload,
    );

    debugPrint('📱 Showed push notification: ${notification.title}');
  }

  /// Show a simple notification with title and body
  Future<void> showSimpleNotification({
    required String title,
    required String body,
    String? type,
    String? entityType,
    int? entityId,
  }) async {
    if (!_isInitialized) {
      await initialize();
    }

    final notificationId = _getNotificationId(type ?? 'default');

    final androidDetails = AndroidNotificationDetails(
      _channelId,
      _channelName,
      channelDescription: _channelDescription,
      importance: Importance.high,
      priority: Priority.high,
      icon: _getNotificationIcon(type ?? 'default'),
      styleInformation: BigTextStyleInformation(body),
      autoCancel: true,
    );

    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );

    final details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    final payload = jsonEncode({
      'type': type,
      'entityType': entityType,
      'entityId': entityId,
    });

    await _notificationsPlugin.show(
      notificationId,
      title,
      body,
      details,
      payload: payload,
    );
  }

  /// Show notification for new chat message
  Future<void> showChatNotification({
    required String senderName,
    required String message,
    required int conversationId,
  }) async {
    await showSimpleNotification(
      title: 'New message from $senderName',
      body: message,
      type: 'CHAT_MESSAGE',
      entityType: 'CONVERSATION',
      entityId: conversationId,
    );
  }

  /// Show notification for internship status change
  Future<void> showInternshipNotification({
    required String title,
    required String message,
    required int internshipId,
    required String status,
  }) async {
    await showSimpleNotification(
      title: title,
      body: message,
      type: 'INTERNSHIP_$status',
      entityType: 'INTERNSHIP',
      entityId: internshipId,
    );
  }

  /// Cancel a specific notification
  Future<void> cancelNotification(int id) async {
    await _notificationsPlugin.cancel(id);
  }

  /// Cancel all notifications
  Future<void> cancelAllNotifications() async {
    await _notificationsPlugin.cancelAll();
  }

  /// Get notification ID based on type
  int _getNotificationId(String type) {
    switch (type.toUpperCase()) {
      case 'CHAT_MESSAGE':
        return _chatNotificationId;
      case 'INTERNSHIP_VALIDATED':
      case 'INTERNSHIP_REFUSED':
      case 'INTERNSHIP_CLAIMED':
      case 'INTERNSHIP_SUBMITTED':
        return _internshipNotificationId;
      case 'SYSTEM':
      case 'ANNOUNCEMENT':
        return _systemNotificationId;
      default:
        return _defaultNotificationId;
    }
  }

  /// Get notification icon based on type
  String _getNotificationIcon(String type) {
    // Use default icon - can be customized with different icons per type
    return '@mipmap/ic_launcher';
  }

  /// Get notification category based on type
  String _getNotificationCategory(String type) {
    switch (type.toUpperCase()) {
      case 'CHAT_MESSAGE':
        return 'Chat';
      case 'INTERNSHIP_VALIDATED':
        return 'Internship Validated';
      case 'INTERNSHIP_REFUSED':
        return 'Internship Refused';
      case 'INTERNSHIP_CLAIMED':
        return 'Internship Claimed';
      case 'COMMENT_ADDED':
        return 'New Comment';
      case 'DEADLINE':
        return 'Deadline Reminder';
      case 'REPORT_UPLOADED':
        return 'Document Uploaded';
      default:
        return 'InternHub';
    }
  }

  /// Handle notification tap
  static void _onNotificationTapped(NotificationResponse response) {
    debugPrint('Notification tapped: ${response.payload}');
    _handleNotificationPayload(response.payload);
  }

  /// Handle background notification tap
  @pragma('vm:entry-point')
  static void _onBackgroundNotificationTapped(NotificationResponse response) {
    debugPrint('Background notification tapped: ${response.payload}');
    _handleNotificationPayload(response.payload);
  }

  /// Handle notification payload for navigation
  static void _handleNotificationPayload(String? payload) {
    if (payload == null) return;

    try {
      final data = jsonDecode(payload) as Map<String, dynamic>;
      final entityType = data['entityType'] as String?;
      final entityId = data['entityId'] as int?;

      // Navigation will be handled by the app when it processes this
      debugPrint('Navigate to: $entityType / $entityId');

      // Store the navigation intent for when the app opens
      // This would typically be handled by the app's navigation system
    } catch (e) {
      debugPrint('Error parsing notification payload: $e');
    }
  }

  /// Check if notifications are enabled
  Future<bool> areNotificationsEnabled() async {
    if (Platform.isAndroid) {
      final android = _notificationsPlugin
          .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin
          >();
      return await android?.areNotificationsEnabled() ?? false;
    }
    return true;
  }
}
