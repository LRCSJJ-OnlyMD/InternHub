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
        stats[item['status']] = item['count'];
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
        stats[item['sectorName']] = item['count'];
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
      final response = await _apiService.get('/admin/users');
      final List<dynamic> data = response.data;
      return data.map((json) => User.fromJson(json)).toList();
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
      final response = await _apiService.post('/admin/users', data: userData);
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
