class ApiConstants {
  // For Android Emulator: use 10.0.2.2 to access host machine's localhost
  static const String baseUrl = 'http://10.0.2.2:8080/api';

  // Auth endpoints
  static const String login = '/auth/login';
  static const String register = '/auth/register';
  static const String verifyEmail = '/auth/verify-email';
  static const String requestPasswordReset = '/auth/password-reset/request';
  static const String confirmPasswordReset = '/auth/password-reset/confirm';

  // Student endpoints
  static const String studentInternships = '/student/internships';

  // Instructor endpoints
  static const String instructorInternships = '/instructor/internships';

  // Admin endpoints
  static const String adminInternships = '/admin/internships';
  static const String adminUsers = '/admin/users';
  static const String sectors = '/utility/sectors';

  // Comments endpoint
  static const String comments = '/comments';

  // Notifications endpoint
  static const String notifications = '/notifications';
}

class AppConstants {
  static const String appName = 'InternHub';
  static const String tokenKey = 'auth_token';
  static const String userKey = 'user_data';
  static const Duration connectionTimeout = Duration(seconds: 30);
  static const Duration receiveTimeout = Duration(seconds: 30);
}
