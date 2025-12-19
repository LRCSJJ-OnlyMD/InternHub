import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/internship.dart';
import '../../providers/admin_provider.dart';
import '../../utils/app_theme.dart';

class AdminInternshipsScreen extends ConsumerStatefulWidget {
  const AdminInternshipsScreen({Key? key}) : super(key: key);

  @override
  ConsumerState<AdminInternshipsScreen> createState() =>
      _AdminInternshipsScreenState();
}

class _AdminInternshipsScreenState
    extends ConsumerState<AdminInternshipsScreen> {
  String _searchQuery = '';
  InternshipStatus? _selectedStatus;

  @override
  Widget build(BuildContext context) {
    final internshipsAsync = ref.watch(adminInternshipsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('All Internships'),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            tooltip: 'Advanced Search',
            onPressed: () {
              // TODO: Navigate to advanced search screen
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Advanced search coming soon')),
              );
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Search and filter bar
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                // Search bar
                TextField(
                  decoration: InputDecoration(
                    hintText: 'Search by title, student, company...',
                    prefixIcon: const Icon(Icons.search),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    filled: true,
                    fillColor: Colors.grey[100],
                  ),
                  onChanged: (value) {
                    setState(() {
                      _searchQuery = value.toLowerCase();
                    });
                  },
                ),
                const SizedBox(height: 12),
                // Status filter chips
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: [
                      _buildFilterChip(null, 'All'),
                      const SizedBox(width: 8),
                      _buildFilterChip(InternshipStatus.DRAFT, 'Draft'),
                      const SizedBox(width: 8),
                      _buildFilterChip(
                        InternshipStatus.PENDING_VALIDATION,
                        'Pending',
                      ),
                      const SizedBox(width: 8),
                      _buildFilterChip(InternshipStatus.VALIDATED, 'Validated'),
                      const SizedBox(width: 8),
                      _buildFilterChip(InternshipStatus.REFUSED, 'Refused'),
                    ],
                  ),
                ),
              ],
            ),
          ),

          // Internships list
          Expanded(
            child: internshipsAsync.when(
              data: (internships) {
                // Apply filters
                var filteredInternships = internships;

                // Filter by status
                if (_selectedStatus != null) {
                  filteredInternships = filteredInternships
                      .where((i) => i.status == _selectedStatus)
                      .toList();
                }

                // Filter by search query
                if (_searchQuery.isNotEmpty) {
                  filteredInternships = filteredInternships.where((i) {
                    return i.title.toLowerCase().contains(_searchQuery) ||
                        (i.studentName?.toLowerCase().contains(_searchQuery) ??
                            false) ||
                        (i.company?.toLowerCase().contains(_searchQuery) ??
                            false) ||
                        (i.location?.toLowerCase().contains(_searchQuery) ??
                            false) ||
                        (i.status.value.toLowerCase().contains(_searchQuery));
                  }).toList();
                }

                if (filteredInternships.isEmpty) {
                  return Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.work_off, size: 64, color: Colors.grey[400]),
                        const SizedBox(height: 16),
                        Text(
                          'No internships found',
                          style: TextStyle(
                            fontSize: 16,
                            color: Colors.grey[600],
                          ),
                        ),
                      ],
                    ),
                  );
                }

                return RefreshIndicator(
                  onRefresh: () async {
                    ref.invalidate(adminInternshipsProvider);
                  },
                  child: ListView.builder(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    itemCount: filteredInternships.length,
                    itemBuilder: (context, index) {
                      final internship = filteredInternships[index];
                      return _buildInternshipCard(context, internship);
                    },
                  ),
                );
              },
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (error, stack) => Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(
                      Icons.error_outline,
                      size: 64,
                      color: Colors.red,
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'Error loading internships',
                      style: TextStyle(fontSize: 16, color: Colors.grey[700]),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      error.toString(),
                      style: TextStyle(fontSize: 14, color: Colors.grey[600]),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      onPressed: () => ref.invalidate(adminInternshipsProvider),
                      icon: const Icon(Icons.refresh),
                      label: const Text('Retry'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChip(InternshipStatus? status, String label) {
    final isSelected = _selectedStatus == status;
    return FilterChip(
      label: Text(label),
      selected: isSelected,
      onSelected: (selected) {
        setState(() {
          _selectedStatus = selected ? status : null;
        });
      },
      selectedColor: Colors.blue.withOpacity(0.2),
      checkmarkColor: Colors.blue,
    );
  }

  Widget _buildInternshipCard(BuildContext context, Internship internship) {
    final statusColor = AppTheme.getStatusColorFromEnum(internship.status);

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {
          // Navigate to detail view
          context.push(
            '/internship/${internship.id}/detail',
            extra: internship,
          );
        },
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Title and status
              Row(
                children: [
                  Expanded(
                    child: Text(
                      internship.title,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 8,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: statusColor.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text(
                      internship.status.displayName,
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: statusColor,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),

              // Student info
              if (internship.studentName != null)
                Row(
                  children: [
                    Icon(Icons.person, size: 16, color: Colors.grey[600]),
                    const SizedBox(width: 4),
                    Text(
                      internship.studentName!,
                      style: TextStyle(fontSize: 14, color: Colors.grey[700]),
                    ),
                  ],
                ),
              const SizedBox(height: 4),

              // Company
              if (internship.company != null)
                Row(
                  children: [
                    Icon(Icons.business, size: 16, color: Colors.grey[600]),
                    const SizedBox(width: 4),
                    Text(
                      internship.company!,
                      style: TextStyle(fontSize: 14, color: Colors.grey[700]),
                    ),
                  ],
                ),
              const SizedBox(height: 4),

              // Sector
              Row(
                children: [
                  Icon(Icons.category, size: 16, color: Colors.grey[600]),
                  const SizedBox(width: 4),
                  Text(
                    internship.sectorName,
                    style: TextStyle(fontSize: 14, color: Colors.grey[700]),
                  ),
                ],
              ),
              const SizedBox(height: 8),

              // Actions
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  TextButton.icon(
                    onPressed: () => _showReassignDialog(context, internship),
                    icon: const Icon(Icons.swap_horiz, size: 18),
                    label: const Text('Reassign'),
                  ),
                  TextButton.icon(
                    onPressed: () =>
                        _showDeleteConfirmation(context, internship),
                    icon: const Icon(Icons.delete, size: 18),
                    label: const Text('Delete'),
                    style: TextButton.styleFrom(foregroundColor: Colors.red),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _showReassignDialog(
    BuildContext context,
    Internship internship,
  ) async {
    final instructorsAsync = await ref.read(allInstructorsProvider.future);

    if (!mounted) return;

    final selectedInstructor = await showDialog<int>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Reassign Instructor'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Current: ${internship.instructorName ?? "None"}'),
            const SizedBox(height: 16),
            const Text('Select new instructor:'),
            const SizedBox(height: 8),
            ...instructorsAsync.map((instructor) {
              return RadioListTile<int>(
                title: Text(instructor.fullName),
                subtitle: Text(instructor.email),
                value: instructor.id,
                groupValue: internship.instructor?.id,
                onChanged: (value) {
                  Navigator.of(context).pop(value);
                },
              );
            }),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
        ],
      ),
    );

    if (selectedInstructor != null && mounted) {
      try {
        await ref
            .read(adminServiceProvider)
            .reassignInstructor(internship.id, selectedInstructor);
        ref.invalidate(adminInternshipsProvider);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Instructor reassigned successfully')),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
          );
        }
      }
    }
  }

  Future<void> _showDeleteConfirmation(
    BuildContext context,
    Internship internship,
  ) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Internship'),
        content: Text(
          'Are you sure you want to delete "${internship.title}"? This action cannot be undone.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      try {
        await ref.read(adminServiceProvider).deleteInternship(internship.id);
        ref.invalidate(adminInternshipsProvider);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('"${internship.title}" deleted successfully'),
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
          );
        }
      }
    }
  }
}
