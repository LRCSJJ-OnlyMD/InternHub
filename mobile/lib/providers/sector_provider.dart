import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/internship.dart';
import 'internship_provider.dart';

// Separate sectors provider to avoid naming conflicts
final sectorsListProvider = FutureProvider<List<Sector>>((ref) async {
  final service = ref.watch(internshipServiceProvider);
  return await service.getSectors();
});
