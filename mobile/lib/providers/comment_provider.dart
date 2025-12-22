import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/comment.dart';
import '../services/comment_service.dart';

final commentServiceProvider = Provider<CommentService>(
  (ref) => CommentService(),
);

final commentsProvider =
    StateNotifierProvider.family<
      CommentNotifier,
      AsyncValue<List<Comment>>,
      int
    >((ref, internshipId) {
      return CommentNotifier(ref.watch(commentServiceProvider), internshipId);
    });

class CommentNotifier extends StateNotifier<AsyncValue<List<Comment>>> {
  final CommentService _service;
  final int internshipId;

  CommentNotifier(this._service, this.internshipId)
    : super(const AsyncValue.loading()) {
    loadComments();
  }

  Future<void> loadComments() async {
    state = const AsyncValue.loading();
    try {
      final comments = await _service.getCommentsByInternshipId(internshipId);
      state = AsyncValue.data(comments);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> addComment(String content) async {
    try {
      final request = CreateCommentRequest(
        content: content,
        internshipId: internshipId,
      );
      final newComment = await _service.createComment(request);
      state.whenData((comments) {
        state = AsyncValue.data([newComment, ...comments]);
      });
      await loadComments(); // Reload to ensure consistency
    } catch (e) {
      rethrow;
    }
  }

  Future<void> updateComment(int commentId, String content) async {
    try {
      final request = UpdateCommentRequest(content: content);
      final updated = await _service.updateComment(
        internshipId,
        commentId,
        request,
      );
      state.whenData((comments) {
        final index = comments.indexWhere((c) => c.id == commentId);
        if (index != -1) {
          final newList = [...comments];
          newList[index] = updated;
          state = AsyncValue.data(newList);
        }
      });
    } catch (e) {
      rethrow;
    }
  }

  Future<void> deleteComment(int commentId) async {
    try {
      await _service.deleteComment(internshipId, commentId);
      state.whenData((comments) {
        state = AsyncValue.data(
          comments.where((c) => c.id != commentId).toList(),
        );
      });
    } catch (e) {
      rethrow;
    }
  }
}
