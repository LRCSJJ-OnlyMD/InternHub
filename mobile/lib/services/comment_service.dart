import 'package:dio/dio.dart';
import '../models/comment.dart';
import '../utils/constants.dart';
import 'api_service.dart';

class CommentService {
  final ApiService _apiService = ApiService();

  Future<List<Comment>> getCommentsByInternshipId(int internshipId) async {
    try {
      final response = await _apiService.get(
        '/internships/$internshipId/comments',
      );
      final List<dynamic> data = response.data;
      return data.map((json) => Comment.fromJson(json)).toList();
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Comment> createComment(CreateCommentRequest request) async {
    try {
      final response = await _apiService.post(
        '/internships/${request.internshipId}/comments',
        data: request.toJson(),
      );
      return Comment.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<Comment> updateComment(
    int internshipId,
    int id,
    UpdateCommentRequest request,
  ) async {
    try {
      final response = await _apiService.put(
        '/internships/$internshipId/comments/$id',
        data: request.toJson(),
      );
      return Comment.fromJson(response.data);
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  Future<void> deleteComment(int internshipId, int id) async {
    try {
      await _apiService.delete('/internships/$internshipId/comments/$id');
    } on DioException catch (e) {
      throw _handleError(e);
    }
  }

  String _handleError(DioException e) {
    if (e.response != null) {
      final data = e.response!.data;
      if (data is Map && data.containsKey('message')) {
        return data['message'];
      }
      return 'Server error: ${e.response!.statusCode}';
    }
    return 'Network error: ${e.message}';
  }
}
