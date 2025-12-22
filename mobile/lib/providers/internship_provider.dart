import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/internship.dart';
import '../services/internship_service.dart';

final internshipServiceProvider = Provider<InternshipService>(
  (ref) => InternshipService(),
);

final internshipsProvider =
    StateNotifierProvider<InternshipNotifier, AsyncValue<List<Internship>>>((
      ref,
    ) {
      return InternshipNotifier(ref.watch(internshipServiceProvider));
    });

// Alias for studentInternshipsProvider (used in some screens)
final studentInternshipsProvider = internshipsProvider;

final sectorsProvider = FutureProvider<List<Sector>>((ref) async {
  final service = ref.watch(internshipServiceProvider);
  return await service.getSectors();
});

class InternshipNotifier extends StateNotifier<AsyncValue<List<Internship>>> {
  final InternshipService _service;

  InternshipNotifier(this._service) : super(const AsyncValue.loading()) {
    loadInternships();
  }

  Future<void> loadInternships() async {
    state = const AsyncValue.loading();
    try {
      final internships = await _service.getAll();
      state = AsyncValue.data(internships);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<Internship?> createInternship(Map<String, dynamic> request) async {
    try {
      final newInternship = await _service.create(request);
      state.whenData((internships) {
        state = AsyncValue.data([...internships, newInternship]);
      });
      await loadInternships(); // Reload to get fresh data
      return newInternship;
    } catch (e) {
      rethrow;
    }
  }

  Future<void> updateInternship(Map<String, dynamic> request) async {
    try {
      final id = request['id'] as int;
      final updated = await _service.update(id, request);
      state.whenData((internships) {
        final index = internships.indexWhere((i) => i.id == id);
        if (index != -1) {
          final newList = [...internships];
          newList[index] = updated;
          state = AsyncValue.data(newList);
        }
      });
      await loadInternships(); // Reload to get fresh data
    } catch (e) {
      rethrow;
    }
  }

  Future<void> deleteInternship(int id) async {
    try {
      await _service.delete(id);
      state.whenData((internships) {
        state = AsyncValue.data(internships.where((i) => i.id != id).toList());
      });
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> submitInternship(int id) async {
    try {
      await _service.submit(id);
      await loadInternships();
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }
}
