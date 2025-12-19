import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/internship.dart';
import '../../providers/sector_provider.dart';
import '../../providers/admin_provider.dart';
import '../../utils/app_theme.dart';

class AdvancedSearchScreen extends ConsumerStatefulWidget {
  const AdvancedSearchScreen({Key? key}) : super(key: key);

  @override
  ConsumerState<AdvancedSearchScreen> createState() =>
      _AdvancedSearchScreenState();
}

class _AdvancedSearchScreenState extends ConsumerState<AdvancedSearchScreen> {
  final _formKey = GlobalKey<FormState>();
  final _companyController = TextEditingController();

  int? _selectedSectorId;
  InternshipStatus? _selectedStatus;
  int? _selectedStudentId;
  int? _selectedInstructorId;
  DateTimeRange? _startDateRange;
  DateTimeRange? _endDateRange;

  List<Internship> _searchResults = [];
  bool _isSearching = false;
  bool _hasSearched = false;

  @override
  void dispose() {
    _companyController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final sectorsAsync = ref.watch(sectorsListProvider);
    final studentsAsync = ref.watch(allStudentsProvider);
    final instructorsAsync = ref.watch(allInstructorsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Advanced Search'),
        actions: [
          if (_hasSearched)
            IconButton(
              icon: const Icon(Icons.clear),
              tooltip: 'Clear results',
              onPressed: () {
                setState(() {
                  _searchResults = [];
                  _hasSearched = false;
                });
              },
            ),
        ],
      ),
      body: Column(
        children: [
          // Search form
          Expanded(
            flex: _hasSearched ? 1 : 2,
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Search Filters',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Company name
                    TextFormField(
                      controller: _companyController,
                      decoration: const InputDecoration(
                        labelText: 'Company Name',
                        prefixIcon: Icon(Icons.business),
                        border: OutlineInputBorder(),
                        hintText: 'Optional',
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Sector dropdown
                    sectorsAsync.when(
                      data: (sectors) => DropdownButtonFormField<int>(
                        value: _selectedSectorId,
                        decoration: const InputDecoration(
                          labelText: 'Sector',
                          prefixIcon: Icon(Icons.category),
                          border: OutlineInputBorder(),
                        ),
                        hint: const Text('All Sectors'),
                        items: sectors.map((sector) {
                          return DropdownMenuItem<int>(
                            value: sector.id,
                            child: Text(sector.name),
                          );
                        }).toList(),
                        onChanged: (value) {
                          setState(() {
                            _selectedSectorId = value;
                          });
                        },
                      ),
                      loading: () => const LinearProgressIndicator(),
                      error: (_, __) => const Text('Error loading sectors'),
                    ),
                    const SizedBox(height: 16),

                    // Status dropdown
                    DropdownButtonFormField<InternshipStatus>(
                      value: _selectedStatus,
                      decoration: const InputDecoration(
                        labelText: 'Status',
                        prefixIcon: Icon(Icons.info),
                        border: OutlineInputBorder(),
                      ),
                      hint: const Text('All Statuses'),
                      items: InternshipStatus.values.map((status) {
                        return DropdownMenuItem<InternshipStatus>(
                          value: status,
                          child: Text(status.displayName),
                        );
                      }).toList(),
                      onChanged: (value) {
                        setState(() {
                          _selectedStatus = value;
                        });
                      },
                    ),
                    const SizedBox(height: 16),

                    // Student dropdown
                    studentsAsync.when(
                      data: (students) => DropdownButtonFormField<int>(
                        value: _selectedStudentId,
                        decoration: const InputDecoration(
                          labelText: 'Student',
                          prefixIcon: Icon(Icons.person),
                          border: OutlineInputBorder(),
                        ),
                        hint: const Text('All Students'),
                        items: students.map((student) {
                          return DropdownMenuItem<int>(
                            value: student.id,
                            child: Text(student.fullName),
                          );
                        }).toList(),
                        onChanged: (value) {
                          setState(() {
                            _selectedStudentId = value;
                          });
                        },
                      ),
                      loading: () => const LinearProgressIndicator(),
                      error: (_, __) => const Text('Error loading students'),
                    ),
                    const SizedBox(height: 16),

                    // Instructor dropdown
                    instructorsAsync.when(
                      data: (instructors) => DropdownButtonFormField<int>(
                        value: _selectedInstructorId,
                        decoration: const InputDecoration(
                          labelText: 'Instructor',
                          prefixIcon: Icon(Icons.school),
                          border: OutlineInputBorder(),
                        ),
                        hint: const Text('All Instructors'),
                        items: instructors.map((instructor) {
                          return DropdownMenuItem<int>(
                            value: instructor.id,
                            child: Text(instructor.fullName),
                          );
                        }).toList(),
                        onChanged: (value) {
                          setState(() {
                            _selectedInstructorId = value;
                          });
                        },
                      ),
                      loading: () => const LinearProgressIndicator(),
                      error: (_, __) => const Text('Error loading instructors'),
                    ),
                    const SizedBox(height: 16),

                    // Start date range
                    ListTile(
                      title: const Text('Start Date Range'),
                      subtitle: Text(
                        _startDateRange == null
                            ? 'Not selected'
                            : '${_formatDate(_startDateRange!.start)} - ${_formatDate(_startDateRange!.end)}',
                      ),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          if (_startDateRange != null)
                            IconButton(
                              icon: const Icon(Icons.clear, size: 20),
                              onPressed: () {
                                setState(() {
                                  _startDateRange = null;
                                });
                              },
                            ),
                          IconButton(
                            icon: const Icon(Icons.calendar_today),
                            onPressed: () => _selectStartDateRange(context),
                          ),
                        ],
                      ),
                      contentPadding: EdgeInsets.zero,
                    ),
                    const Divider(),

                    // End date range
                    ListTile(
                      title: const Text('End Date Range'),
                      subtitle: Text(
                        _endDateRange == null
                            ? 'Not selected'
                            : '${_formatDate(_endDateRange!.start)} - ${_formatDate(_endDateRange!.end)}',
                      ),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          if (_endDateRange != null)
                            IconButton(
                              icon: const Icon(Icons.clear, size: 20),
                              onPressed: () {
                                setState(() {
                                  _endDateRange = null;
                                });
                              },
                            ),
                          IconButton(
                            icon: const Icon(Icons.calendar_today),
                            onPressed: () => _selectEndDateRange(context),
                          ),
                        ],
                      ),
                      contentPadding: EdgeInsets.zero,
                    ),
                    const SizedBox(height: 24),

                    // Search and clear buttons
                    Row(
                      children: [
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: _isSearching ? null : _performSearch,
                            icon: _isSearching
                                ? const SizedBox(
                                    width: 16,
                                    height: 16,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  )
                                : const Icon(Icons.search),
                            label: Text(
                              _isSearching ? 'Searching...' : 'Search',
                            ),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 16),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: OutlinedButton.icon(
                            onPressed: _clearFilters,
                            icon: const Icon(Icons.clear_all),
                            label: const Text('Clear Filters'),
                            style: OutlinedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(vertical: 16),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),

          // Results section
          if (_hasSearched) ...[
            const Divider(height: 1),
            Expanded(flex: 1, child: _buildSearchResults()),
          ],
        ],
      ),
    );
  }

  Widget _buildSearchResults() {
    if (_searchResults.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.search_off, size: 64, color: Colors.grey[400]),
            const SizedBox(height: 16),
            Text(
              'No results found',
              style: TextStyle(fontSize: 16, color: Colors.grey[600]),
            ),
            const SizedBox(height: 8),
            Text(
              'Try adjusting your search filters',
              style: TextStyle(fontSize: 14, color: Colors.grey[500]),
            ),
          ],
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.all(16),
          child: Text(
            '${_searchResults.length} result${_searchResults.length == 1 ? '' : 's'} found',
            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
        ),
        Expanded(
          child: ListView.builder(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            itemCount: _searchResults.length,
            itemBuilder: (context, index) {
              final internship = _searchResults[index];
              return _buildInternshipCard(context, internship);
            },
          ),
        ),
      ],
    );
  }

  Widget _buildInternshipCard(BuildContext context, Internship internship) {
    final statusColor = AppTheme.getStatusColorFromEnum(internship.status);

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {
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
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _selectStartDateRange(BuildContext context) async {
    final picked = await showDateRangePicker(
      context: context,
      firstDate: DateTime(2020),
      lastDate: DateTime(2030),
      initialDateRange: _startDateRange,
    );

    if (picked != null) {
      setState(() {
        _startDateRange = picked;
      });
    }
  }

  Future<void> _selectEndDateRange(BuildContext context) async {
    final picked = await showDateRangePicker(
      context: context,
      firstDate: DateTime(2020),
      lastDate: DateTime(2030),
      initialDateRange: _endDateRange,
    );

    if (picked != null) {
      setState(() {
        _endDateRange = picked;
      });
    }
  }

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year}';
  }

  void _clearFilters() {
    setState(() {
      _companyController.clear();
      _selectedSectorId = null;
      _selectedStatus = null;
      _selectedStudentId = null;
      _selectedInstructorId = null;
      _startDateRange = null;
      _endDateRange = null;
      _searchResults = [];
      _hasSearched = false;
    });
  }

  Future<void> _performSearch() async {
    setState(() {
      _isSearching = true;
    });

    try {
      // Note: This would require adding a search method to admin service
      // For now, we'll filter the existing internships
      final allInternships = await ref.read(adminInternshipsProvider.future);

      var results = allInternships;

      // Apply filters
      if (_companyController.text.isNotEmpty) {
        results = results
            .where(
              (i) =>
                  i.company?.toLowerCase().contains(
                    _companyController.text.toLowerCase(),
                  ) ??
                  false,
            )
            .toList();
      }

      if (_selectedSectorId != null) {
        results = results
            .where((i) => i.sector.id == _selectedSectorId)
            .toList();
      }

      if (_selectedStatus != null) {
        results = results.where((i) => i.status == _selectedStatus).toList();
      }

      if (_selectedStudentId != null) {
        results = results
            .where((i) => i.student.id == _selectedStudentId)
            .toList();
      }

      if (_selectedInstructorId != null) {
        results = results
            .where((i) => i.instructor?.id == _selectedInstructorId)
            .toList();
      }

      if (_startDateRange != null) {
        results = results.where((i) {
          final startDate = i.startDate;
          return startDate.isAfter(_startDateRange!.start) &&
              startDate.isBefore(_startDateRange!.end);
        }).toList();
      }

      if (_endDateRange != null) {
        results = results.where((i) {
          final endDate = i.endDate;
          return endDate.isAfter(_endDateRange!.start) &&
              endDate.isBefore(_endDateRange!.end);
        }).toList();
      }

      setState(() {
        _searchResults = results;
        _hasSearched = true;
      });
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error performing search: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isSearching = false;
        });
      }
    }
  }
}
