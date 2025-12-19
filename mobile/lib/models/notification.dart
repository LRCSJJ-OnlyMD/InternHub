class AppNotification {
  final int id;
  final String type;
  final String title;
  final String message;
  final String? entityType;
  final int? entityId;
  final bool read;
  final DateTime createdAt;

  AppNotification({
    required this.id,
    required this.type,
    required this.title,
    required this.message,
    this.entityType,
    this.entityId,
    required this.read,
    required this.createdAt,
  });

  factory AppNotification.fromJson(Map<String, dynamic> json) {
    return AppNotification(
      id: json['id'],
      type: json['type'],
      title: json['title'],
      message: json['message'],
      entityType: json['entityType'],
      entityId: json['entityId'],
      read: json['read'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'type': type,
      'title': title,
      'message': message,
      'entityType': entityType,
      'entityId': entityId,
      'read': read,
      'createdAt': createdAt.toIso8601String(),
    };
  }

  // Get notification icon based on type
  String getIcon() {
    switch (type.toUpperCase()) {
      case 'INTERNSHIP_VALIDATED':
        return '✓';
      case 'INTERNSHIP_REFUSED':
        return '✗';
      case 'NEW_COMMENT':
        return '💬';
      case 'DOCUMENT_UPLOADED':
        return '📄';
      case 'DEADLINE_REMINDER':
        return '⏰';
      case 'ASSIGNMENT':
        return '📋';
      default:
        return '🔔';
    }
  }

  // Copy with method for updating properties
  AppNotification copyWith({
    int? id,
    String? type,
    String? title,
    String? message,
    String? entityType,
    int? entityId,
    bool? read,
    DateTime? createdAt,
  }) {
    return AppNotification(
      id: id ?? this.id,
      type: type ?? this.type,
      title: title ?? this.title,
      message: message ?? this.message,
      entityType: entityType ?? this.entityType,
      entityId: entityId ?? this.entityId,
      read: read ?? this.read,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}

// Paginated notifications response
class NotificationPage {
  final List<AppNotification> notifications;
  final int totalElements;
  final int totalPages;
  final int currentPage;
  final int size;

  NotificationPage({
    required this.notifications,
    required this.totalElements,
    required this.totalPages,
    required this.currentPage,
    required this.size,
  });

  factory NotificationPage.fromJson(Map<String, dynamic> json) {
    return NotificationPage(
      notifications: (json['content'] as List)
          .map((item) => AppNotification.fromJson(item))
          .toList(),
      totalElements: json['totalElements'],
      totalPages: json['totalPages'],
      currentPage: json['number'],
      size: json['size'],
    );
  }
}
