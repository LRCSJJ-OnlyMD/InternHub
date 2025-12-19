import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/internship.dart';
import '../../providers/internship_provider.dart';
import '../../providers/auth_provider.dart';

class InstructorInternshipsScreen extends ConsumerStatefulWidget {
  const InstructorInternshipsScreen({super.key});

  @override
  ConsumerState<InstructorInternshipsScreen> createState() =>
      _InstructorInternshipsScreenState();
}

class _InstructorInternshipsScreenState
    extends ConsumerState<InstructorInternshipsScreen> {
  InternshipStatus? _filterStatus;
  String _searchQuery = '';
  bool _bulkSelectMode = false;
  final Set<int> _selectedInternships = {};

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authStateProvider);
    final internshipsState = ref.watch(internshipsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Assigned Internships'),
        actions: [
          if (!_bulkSelectMode)
            IconButton(
              icon: const Icon(Icons.checklist),
              tooltip: 'Bulk Select',
              onPressed: () {
                setState(() => _bulkSelectMode = true);
              },
            ),
          if (_bulkSelectMode)
            TextButton(
              onPressed: () {
                setState(() {
                  _bulkSelectMode = false;
                  _selectedInternships.clear();
                });
              },
              child: const Text('Cancel'),
            ),
        ],
      ),
      body: authState.when(
        data: (user) {
          if (user == null) return const Center(child: Text('Not logged in'));

          return Column(
            children: [
              // Search and Filter Bar
              Container(
                padding: const EdgeInsets.all(16),
                color: Colors.grey[100],
                child: Column(
                  children: [
                    TextField(
                      decoration: InputDecoration(
                        hintText: 'Search by student name or company...',
                        prefixIcon: const Icon(Icons.search),
                        filled: true,
                        fillColor: Colors.white,
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide.none,
                        ),
                      ),
                      onChanged: (value) {
                        setState(() => _searchQuery = value.toLowerCase());
                      },
                    ),
                    const SizedBox(height: 12),
                    SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: Row(
                        children: [
                          _FilterChip(
                            label: 'All',
                            isSelected: _filterStatus == null,
                            onTap: () {
                              setState(() => _filterStatus = null);
                            },
                          ),
                          const SizedBox(width: 8),
                          _FilterChip(
                            label: 'Pending',
                            isSelected:
                                _filterStatus ==
                                InternshipStatus.PENDING_VALIDATION,
                            color: Colors.orange,
                            onTap: () {
                              setState(
                                () => _filterStatus =
                                    InternshipStatus.PENDING_VALIDATION,
                              );
                            },
                          ),
                          const SizedBox(width: 8),
                          _FilterChip(
                            label: 'Validated',
                            isSelected:
                                _filterStatus == InternshipStatus.VALIDATED,
                            color: Colors.green,
                            onTap: () {
                              setState(
                                () =>
                                    _filterStatus = InternshipStatus.VALIDATED,
                              );
                            },
                          ),
                          const SizedBox(width: 8),
                          _FilterChip(
                            label: 'Refused',
                            isSelected:
                                _filterStatus == InternshipStatus.REFUSED,
                            color: Colors.red,
                            onTap: () {
                              setState(
                                () => _filterStatus = InternshipStatus.REFUSED,
                              );
                            },
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              // Internships List
              Expanded(
                child: internshipsState.when(
                  data: (internships) {
                    // Filter by instructor
                    var filtered = internships
                        .where((i) => i.instructor?.id == user.id)
                        .toList();

                    // Apply status filter
                    if (_filterStatus != null) {
                      filtered = filtered
                          .where((i) => i.status == _filterStatus)
                          .toList();
                    }

                    // Apply search filter
                    if (_searchQuery.isNotEmpty) {
                      filtered = filtered.where((i) {
                        return i.studentName.toLowerCase().contains(
                              _searchQuery,
                            ) ||
                            i.companyName.toLowerCase().contains(
                              _searchQuery,
                            ) ||
                            i.title.toLowerCase().contains(_searchQuery);
                      }).toList();
                    }

                    if (filtered.isEmpty) {
                      return Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Icons.inbox,
                              size: 64,
                              color: Colors.grey[400],
                            ),
                            const SizedBox(height: 16),
                            Text(
                              _searchQuery.isNotEmpty
                                  ? 'No internships found'
                                  : 'No assigned internships',
                              style: TextStyle(color: Colors.grey[600]),
                            ),
                          ],
                        ),
                      );
                    }

                    return ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: filtered.length,
                      itemBuilder: (context, index) {
                        final internship = filtered[index];
                        final isSelected = _selectedInternships.contains(
                          internship.id,
                        );

                        return Card(
                          margin: const EdgeInsets.only(bottom: 12),
                          child: ListTile(
                            leading: _bulkSelectMode
                                ? Checkbox(
                                    value: isSelected,
                                    onChanged: (value) {
                                      setState(() {
                                        if (value == true) {
                                          _selectedInternships.add(
                                            internship.id,
                                          );
                                        } else {
                                          _selectedInternships.remove(
                                            internship.id,
                                          );
                                        }
                                      });
                                    },
                                  )
                                : _buildStatusIcon(internship.status),
                            title: Text(
                              internship.title,
                              style: const TextStyle(
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            subtitle: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const SizedBox(height: 4),
                                Text('Student: ${internship.studentName}'),
                                Text('Company: ${internship.companyName}'),
                                const SizedBox(height: 4),
                                _StatusBadge(status: internship.status),
                              ],
                            ),
                            trailing: _bulkSelectMode
                                ? null
                                : const Icon(Icons.arrow_forward_ios, size: 16),
                            onTap: _bulkSelectMode
                                ? () {
                                    setState(() {
                                      if (isSelected) {
                                        _selectedInternships.remove(
                                          internship.id,
                                        );
                                      } else {
                                        _selectedInternships.add(internship.id);
                                      }
                                    });
                                  }
                                : () {
                                    context.push(
                                      '/instructor/internship/${internship.id}/detail',
                                      extra: internship,
                                    );
                                  },
                          ),
                        );
                      },
                    );
                  },
                  loading: () =>
                      const Center(child: CircularProgressIndicator()),
                  error: (error, _) => Center(child: Text('Error: $error')),
                ),
              ),
            ],
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(child: Text('Error: $error')),
      ),
      bottomNavigationBar: _bulkSelectMode && _selectedInternships.isNotEmpty
          ? _buildBulkActions()
          : null,
    );
  }

  Widget _buildStatusIcon(InternshipStatus status) {
    Color color;
    IconData icon;

    switch (status) {
      case InternshipStatus.DRAFT:
        color = Colors.grey;
        icon = Icons.drafts;
        break;
      case InternshipStatus.PENDING_VALIDATION:
        color = Colors.orange;
        icon = Icons.pending_actions;
        break;
      case InternshipStatus.VALIDATED:
        color = Colors.green;
        icon = Icons.check_circle;
        break;
      case InternshipStatus.REFUSED:
        color = Colors.red;
        icon = Icons.cancel;
        break;
    }

    return CircleAvatar(
      backgroundColor: color.withOpacity(0.2),
      child: Icon(icon, color: color, size: 20),
    );
  }

  Widget _buildBulkActions() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 4,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            '${_selectedInternships.length} selected',
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: _handleBulkRefuse,
                  icon: const Icon(Icons.cancel),
                  label: const Text('Refuse'),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: Colors.red,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                flex: 2,
                child: ElevatedButton.icon(
                  onPressed: _handleBulkValidate,
                  icon: const Icon(Icons.check_circle),
                  label: const Text('Validate'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Future<void> _handleBulkValidate() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Bulk Validate'),
        content: Text(
          'Are you sure you want to validate ${_selectedInternships.length} internship(s)?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.green,
              foregroundColor: Colors.white,
            ),
            child: const Text('Validate'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      // TODO: Implement bulk validate API
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('${_selectedInternships.length} internships validated'),
          backgroundColor: Colors.green,
        ),
      );
      setState(() {
        _bulkSelectMode = false;
        _selectedInternships.clear();
      });
      ref.read(internshipsProvider.notifier).loadInternships();
    }
  }

  Future<void> _handleBulkRefuse() async {
    final controller = TextEditingController();

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Bulk Refuse'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Refuse ${_selectedInternships.length} internship(s)?'),
            const SizedBox(height: 16),
            TextField(
              controller: controller,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: 'Reason (required)',
                border: OutlineInputBorder(),
                hintText: 'Enter refusal reason...',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              if (controller.text.trim().isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Please enter a reason'),
                    backgroundColor: Colors.red,
                  ),
                );
                return;
              }
              Navigator.pop(context, true);
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
            ),
            child: const Text('Refuse'),
          ),
        ],
      ),
    );

    if (confirmed == true && controller.text.trim().isNotEmpty) {
      // TODO: Implement bulk refuse API
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('${_selectedInternships.length} internships refused'),
          backgroundColor: Colors.orange,
        ),
      );
      setState(() {
        _bulkSelectMode = false;
        _selectedInternships.clear();
      });
      ref.read(internshipsProvider.notifier).loadInternships();
    }
  }
}

class _FilterChip extends StatelessWidget {
  final String label;
  final bool isSelected;
  final Color? color;
  final VoidCallback onTap;

  const _FilterChip({
    required this.label,
    required this.isSelected,
    this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final chipColor = color ?? Colors.blue;

    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? chipColor : Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: isSelected ? chipColor : Colors.grey[300]!),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: isSelected ? Colors.white : Colors.grey[700],
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
          ),
        ),
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  final InternshipStatus status;

  const _StatusBadge({required this.status});

  @override
  Widget build(BuildContext context) {
    Color color;
    switch (status) {
      case InternshipStatus.DRAFT:
        color = Colors.grey;
        break;
      case InternshipStatus.PENDING_VALIDATION:
        color = Colors.orange;
        break;
      case InternshipStatus.VALIDATED:
        color = Colors.green;
        break;
      case InternshipStatus.REFUSED:
        color = Colors.red;
        break;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.2),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        status.displayName,
        style: TextStyle(
          color: color,
          fontWeight: FontWeight.w600,
          fontSize: 11,
        ),
      ),
    );
  }
}
