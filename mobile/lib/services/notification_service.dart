import 'package:dio/dio.dart';
import '../models/notification.dart';
import '../utils/constants.dart';
import 'api_service.dart';

class NotificationService {
  final ApiService _apiService = ApiService();

  // Get paginated notifications
  Future<NotificationPage> getNotifications({
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _apiService.get(
        ApiConstants.notifications,
        queryParameters: {
          'page': page,
          'size': size,
        },
      );

      return NotificationPage.fromJson(response.data);
    } catch (e) {
      throw _handleError(e);
    }
  }

  // Get unread notifications
  Future<List<AppNotification>> getUnreadNotifications() async {
    try {
      final response = await _apiService.get(
        '${ApiConstants.notifications}/unread',
      );

      return (response.data as List)
          .map((item) => AppNotification.fromJson(item))
          .toList();
    } catch (e) {
      throw _handleError(e);
    }
  }

  // Get unread count
  Future<int> getUnreadCount() async {
    try {
      final response = await _apiService.get(
        '${ApiConstants.notifications}/unread/count',
      );

      return response.data['count'] as int;
    } catch (e) {
      throw _handleError(e);
    }
  }

  // Mark notification as read
  Future<void> markAsRead(int notificationId) async {
    try {
      await _apiService.put(
        '${ApiConstants.notifications}/$notificationId/read',
      );
    } catch (e) {
      throw _handleError(e);
    }
  }

  // Mark all notifications as read
  Future<int> markAllAsRead() async {
    try {
      final response = await _apiService.put(
        '${ApiConstants.notifications}/mark-all-read',
      );

      return response.data['count'] as int;
    } catch (e) {
      throw _handleError(e);
    }
  }

  // Delete notification
  Future<void> deleteNotification(int notificationId) async {
    try {
      await _apiService.delete(
        '${ApiConstants.notifications}/$notificationId',
      );
    } catch (e) {
      throw _handleError(e);
    }
  }

  String _handleError(dynamic error) {
    if (error is DioException) {
      if (error.response != null) {
        final data = error.response!.data;
        if (data is Map && data.containsKey('message')) {
          return data['message'];
        }
        return error.response!.statusMessage ?? 'An error occurred';
      }
      return 'Network error occurred';
    }
    return error.toString();
  }
}
