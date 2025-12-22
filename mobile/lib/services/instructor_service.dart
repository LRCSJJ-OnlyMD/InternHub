import 'package:dio/dio.dart';
import '../models/internship.dart';
import 'api_service.dart';

class InstructorService {
  final ApiService _apiService = ApiService();

  // Get pending internships for validation (assigned to me)
  Future<List<Internship>> getPendingInternships() async {
    try {
      final response = await _apiService.get('/instructor/internships/pending');
      final List<dynamic> data = response.data;
      return data.map((json) => Internship.fromJson(json)).toList();
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // Get available internships for claiming (in my sectors, not assigned)
  Future<List<Internship>> getAvailableInternships() async {
    try {
      final response = await _apiService.get(
        '/instructor/internships/available',
      );
      final List<dynamic> data = response.data;
      return data.map((json) => Internship.fromJson(json)).toList();
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // Get validated internships (I am the instructor)
  Future<List<Internship>> getValidatedInternships() async {
    try {
      final response = await _apiService.get(
        '/instructor/internships/validated',
      );
      final List<dynamic> data = response.data;
      return data.map((json) => Internship.fromJson(json)).toList();
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // Get all my internships (pending + validated)
  Future<List<Internship>> getMyInternships() async {
    try {
      final pending = await getPendingInternships();
      final validated = await getValidatedInternships();
      return [...pending, ...validated];
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // Claim an unassigned internship
  Future<Internship> claimInternship(int internshipId) async {
    try {
      final response = await _apiService.post(
        '/instructor/internships/$internshipId/claim',
      );
      return Internship.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // Validate internship
  Future<Internship> validateInternship(int internshipId) async {
    try {
      final response = await _apiService.post(
        '/instructor/internships/$internshipId/validate',
      );
      return Internship.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // Refuse internship
  Future<Internship> refuseInternship(int internshipId, String reason) async {
    try {
      final response = await _apiService.post(
        '/instructor/internships/$internshipId/refuse',
        data: {'reason': reason},
      );
      return Internship.fromJson(response.data);
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
