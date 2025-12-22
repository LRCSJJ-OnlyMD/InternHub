import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/internship.dart';
import '../services/instructor_service.dart';

// Provider for InstructorService
final instructorServiceProvider = Provider<InstructorService>((ref) {
  return InstructorService();
});

// Provider for instructor's internships (pending + validated)
final instructorInternshipsProvider = FutureProvider<List<Internship>>((
  ref,
) async {
  final service = ref.watch(instructorServiceProvider);
  return await service.getMyInternships();
});

// Provider for pending internships (for validation)
final instructorPendingProvider = FutureProvider<List<Internship>>((ref) async {
  final service = ref.watch(instructorServiceProvider);
  return await service.getPendingInternships();
});

// Provider for validated internships
final instructorValidatedProvider = FutureProvider<List<Internship>>((
  ref,
) async {
  final service = ref.watch(instructorServiceProvider);
  return await service.getValidatedInternships();
});

// Provider for available internships (to claim)
final instructorAvailableProvider = FutureProvider<List<Internship>>((
  ref,
) async {
  final service = ref.watch(instructorServiceProvider);
  return await service.getAvailableInternships();
});
