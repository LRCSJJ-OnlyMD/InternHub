import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/internship.dart';
import '../models/user.dart';
import '../services/admin_service.dart';

// Provider for AdminService
final adminServiceProvider = Provider<AdminService>((ref) {
  return AdminService();
});

// Provider for admin statistics
final adminStatsProvider = FutureProvider<Map<String, dynamic>>((ref) async {
  final service = ref.watch(adminServiceProvider);

  try {
    final statsByStatus = await service.getStatsByStatus();
    final statsBySector = await service.getStatsBySector();

    // Get all users count
    final allUsers = await service.getAllUsers();

    // Calculate totals
    int totalInternships = 0;
    int pendingInternships = 0;
    int validatedInternships = 0;

    statsByStatus.forEach((status, count) {
      totalInternships += count;
      if (status.toUpperCase().contains('PENDING')) {
        pendingInternships += count;
      } else if (status.toUpperCase().contains('VALIDATED')) {
        validatedInternships += count;
      }
    });

    return {
      'totalUsers': allUsers.length,
      'totalInternships': totalInternships,
      'pendingInternships': pendingInternships,
      'validatedInternships': validatedInternships,
      'byStatus': statsByStatus,
      'bySector': statsBySector,
    };
  } catch (e) {
    throw Exception('Failed to load admin statistics: $e');
  }
});

// Provider for all users
final allUsersProvider = FutureProvider<List<User>>((ref) async {
  final service = ref.watch(adminServiceProvider);
  return await service.getAllUsers();
});

// Provider for all instructors
final allInstructorsProvider = FutureProvider<List<User>>((ref) async {
  final service = ref.watch(adminServiceProvider);
  return await service.getAllInstructors();
});

// Provider for all students
final allStudentsProvider = FutureProvider<List<User>>((ref) async {
  final service = ref.watch(adminServiceProvider);
  return await service.getAllStudents();
});

// Provider for all admin internships
final adminInternshipsProvider = FutureProvider<List<Internship>>((ref) async {
  final service = ref.watch(adminServiceProvider);
  return await service.getAllInternships();
});
