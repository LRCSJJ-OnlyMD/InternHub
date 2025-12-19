import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../models/user.dart';
import '../utils/constants.dart';
import 'api_service.dart';

class AuthService {
  final ApiService _apiService = ApiService();
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  Future<AuthResponse> login(LoginRequest request) async {
    try {
      final response = await _apiService.post(
        ApiConstants.login,
        data: request.toJson(),
      );

      if (response.data == null) {
        throw Exception('No response data received from server');
      }

      final authResponse = AuthResponse.fromJson(
        response.data as Map<String, dynamic>,
      );
      await _apiService.saveToken(authResponse.token);
      await _storage.write(
        key: AppConstants.userKey,
        value: jsonEncode(authResponse.user.toJson()),
      );

      return authResponse;
    } on DioException catch (e) {
      throw _handleError(e);
    } catch (e) {
      throw 'Login failed: $e';
    }
  }

  Future<void> register(RegisterRequest request) async {
    try {
      await _apiService.post(ApiConstants.register, data: request.toJson());
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<User?> getCurrentUser() async {
    try {
      final userData = await _storage.read(key: AppConstants.userKey);
      if (userData != null) {
        return User.fromJson(jsonDecode(userData));
      }
      return null;
    } catch (e) {
      return null;
    }
  }

  Future<bool> isAuthenticated() async {
    final token = await _apiService.getToken();
    return token != null;
  }

  Future<void> logout() async {
    await _apiService.clearToken();
  }

  Future<void> changePassword(String oldPassword, String newPassword) async {
    try {
      await _apiService.post(
        '/auth/change-password',
        data: {'oldPassword': oldPassword, 'newPassword': newPassword},
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<User> updateProfile(Map<String, dynamic> profileData) async {
    try {
      final response = await _apiService.put(
        '/users/profile',
        data: profileData,
      );

      final updatedUser = User.fromJson(response.data);
      await _storage.write(
        key: AppConstants.userKey,
        value: jsonEncode(updatedUser.toJson()),
      );

      return updatedUser;
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  String _handleError(DioException error) {
    if (error.response != null) {
      final data = error.response!.data;
      if (data is Map && data.containsKey('message')) {
        return data['message'];
      }
      return 'Server error: ${error.response!.statusCode}';
    } else if (error.type == DioExceptionType.connectionTimeout) {
      return 'Connection timeout';
    } else if (error.type == DioExceptionType.receiveTimeout) {
      return 'Receive timeout';
    } else {
      return 'Network error. Please check your connection.';
    }
  }
}
