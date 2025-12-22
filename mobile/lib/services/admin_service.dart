import 'package:dio/dio.dart';
import '../models/internship.dart';
import '../models/user.dart';
import 'api_service.dart';

class AdminService {
  final ApiService _apiService = ApiService();

  // ========== STATISTICS ==========
  Future<Map<String, int>> getStatsByStatus() async {
    try {
      final response = await _apiService.get('/admin/stats/by-status');

      final Map<String, int> stats = {};
      for (var item in response.data) {
        final label = item['label']?.toString() ?? 'Unknown';
        final count = (item['count'] is int)
            ? item['count']
            : (item['count'] as num?)?.toInt() ?? 0;
        stats[label] = count;
      }
      return stats;
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Map<String, int>> getStatsBySector() async {
    try {
      final response = await _apiService.get('/admin/stats/by-sector');

      final Map<String, int> stats = {};
      for (var item in response.data) {
        final label = item['label']?.toString() ?? 'Unknown';
        final count = (item['count'] is int)
            ? item['count']
            : (item['count'] as num?)?.toInt() ?? 0;
        stats[label] = count;
      }
      return stats;
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // ========== INTERNSHIP MANAGEMENT ==========
  Future<List<Internship>> getAllInternships() async {
    try {
      final response = await _apiService.get('/admin/internships');
      final List<dynamic> data = response.data;
      return data.map((json) => Internship.fromJson(json)).toList();
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<void> deleteInternship(int internshipId) async {
    try {
      await _apiService.delete('/admin/internships/$internshipId');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Internship> reassignInstructor(
    int internshipId,
    int instructorId,
  ) async {
    try {
      final response = await _apiService.put(
        '/admin/internships/$internshipId/reassign/$instructorId',
      );
      return Internship.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // ========== USER MANAGEMENT ==========
  Future<List<User>> getAllUsers() async {
    try {
      // Backend doesn't have /admin/users endpoint, fetch both instructors and students
      final instructors = await getAllInstructors();
      final students = await getAllStudents();
      return [...instructors, ...students];
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<List<User>> getAllInstructors() async {
    try {
      final response = await _apiService.get('/admin/users/instructors');
      final List<dynamic> data = response.data;
      return data.map((json) => User.fromJson(json)).toList();
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<List<User>> getAllStudents() async {
    try {
      final response = await _apiService.get('/admin/users/students');
      final List<dynamic> data = response.data;
      return data.map((json) => User.fromJson(json)).toList();
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<void> deleteUser(int userId) async {
    try {
      await _apiService.delete('/admin/users/$userId');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<User> createUser(Map<String, dynamic> userData) async {
    try {
      // Backend has separate endpoints for creating instructors
      // /api/admin/users/instructors for instructors
      // Currently no endpoint for creating students/admins via API
      final role = userData['role']?.toString().toUpperCase();
      String endpoint;

      if (role == 'INSTRUCTOR') {
        endpoint = '/admin/users/instructors';
      } else {
        // For students and admins, we'll still try the general endpoint
        // Backend might need to add this endpoint
        endpoint = '/admin/users';
      }

      final response = await _apiService.post(endpoint, data: userData);
      return User.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<User> updateUser(int userId, Map<String, dynamic> userData) async {
    try {
      final response = await _apiService.put(
        '/admin/users/$userId',
        data: userData,
      );
      return User.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // ========== SECTOR MANAGEMENT ==========
  Future<Sector> createSector(Map<String, dynamic> sectorData) async {
    try {
      final response = await _apiService.post(
        '/admin/sectors',
        data: sectorData,
      );
      return Sector.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Sector> updateSector(
    int sectorId,
    Map<String, dynamic> sectorData,
  ) async {
    try {
      final response = await _apiService.put(
        '/admin/sectors/$sectorId',
        data: sectorData,
      );
      return Sector.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<void> deleteSector(int sectorId) async {
    try {
      await _apiService.delete('/admin/sectors/$sectorId');
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
      return error.response!.statusMessage ?? 'An error occurred';
    }
    return 'Network error occurred';
  }
}
