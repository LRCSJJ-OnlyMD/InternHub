import 'package:dio/dio.dart';
import '../models/internship.dart';
import '../utils/constants.dart';
import 'api_service.dart';

class InternshipService {
  final ApiService _apiService = ApiService();

  Future<List<Internship>> getAll() async {
    try {
      final response = await _apiService.get(ApiConstants.studentInternships);
      print('Internships response: ${response.data}');
      print('Internships response type: ${response.data.runtimeType}');

      if (response.data == null) {
        return [];
      }

      final List<dynamic> data = response.data as List<dynamic>;
      return data.map((json) {
        try {
          return Internship.fromJson(json as Map<String, dynamic>);
        } catch (e) {
          print('Error parsing internship: $e');
          print('Internship JSON: $json');
          rethrow;
        }
      }).toList();
    } on DioException catch (e) {
      throw _handleError(e);
    } catch (e) {
      print('Error in getAll internships: $e');
      rethrow;
    }
  }

  Future<Internship> getById(int id) async {
    try {
      final response = await _apiService.get(
        '${ApiConstants.studentInternships}/$id',
      );
      return Internship.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Internship> create(Map<String, dynamic> request) async {
    try {
      final response = await _apiService.post(
        ApiConstants.studentInternships,
        data: request,
      );
      return Internship.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Internship> update(int id, Map<String, dynamic> request) async {
    try {
      final response = await _apiService.put(
        '${ApiConstants.studentInternships}/$id',
        data: request,
      );
      return Internship.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<void> delete(int id) async {
    try {
      await _apiService.delete('${ApiConstants.studentInternships}/$id');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<void> submit(int id) async {
    try {
      await _apiService.post('${ApiConstants.studentInternships}/$id/submit');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<List<Sector>> getSectors() async {
    try {
      final response = await _apiService.get(ApiConstants.sectors);
      final List<dynamic> data = response.data;
      return data.map((json) => Sector.fromJson(json)).toList();
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  // Alias for getById
  Future<Internship> getInternshipById(int id) async {
    return getById(id);
  }

  // File upload/download methods
  Future<void> uploadReport(int id, String filePath, String fileName) async {
    try {
      final formData = FormData.fromMap({
        'file': await MultipartFile.fromFile(filePath, filename: fileName),
      });
      await _apiService.post(
        '${ApiConstants.studentInternships}/$id/report',
        data: formData,
      );
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<void> downloadReport(int id, String savePath) async {
    try {
      // TODO: Implement file download
      await _apiService.get(
        '${ApiConstants.studentInternships}/$id/report/download',
      );
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
