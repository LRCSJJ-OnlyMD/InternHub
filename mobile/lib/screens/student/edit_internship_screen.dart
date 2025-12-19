import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/internship.dart';
import '../../providers/sector_provider.dart';
import '../../providers/internship_provider.dart';

class EditInternshipScreen extends ConsumerStatefulWidget {
  final Internship internship;

  const EditInternshipScreen({super.key, required this.internship});

  @override
  ConsumerState<EditInternshipScreen> createState() =>
      _EditInternshipScreenState();
}

class _EditInternshipScreenState extends ConsumerState<EditInternshipScreen> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _titleController;
  late final TextEditingController _descriptionController;
  late final TextEditingController _companyController;
  late final TextEditingController _locationController;

  Sector? _selectedSector;
  DateTime? _startDate;
  DateTime? _endDate;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.internship.title);
    _descriptionController = TextEditingController(
      text: widget.internship.description,
    );
    _companyController = TextEditingController(text: widget.internship.company);
    _locationController = TextEditingController(
      text: widget.internship.location,
    );
    _startDate = widget.internship.startDate;
    _endDate = widget.internship.endDate;
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    _companyController.dispose();
    _locationController.dispose();
    super.dispose();
  }

  Future<void> _selectDate(BuildContext context, bool isStartDate) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: isStartDate
          ? (_startDate ?? DateTime.now())
          : (_endDate ?? DateTime.now().add(const Duration(days: 30))),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365 * 2)),
    );

    if (picked != null) {
      setState(() {
        if (isStartDate) {
          _startDate = picked;
          if (_endDate != null && _endDate!.isBefore(_startDate!)) {
            _endDate = null;
          }
        } else {
          _endDate = picked;
        }
      });
    }
  }

  Future<void> _handleUpdate(bool asDraft) async {
    if (_formKey.currentState!.validate()) {
      if (_selectedSector == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Please select a sector'),
            backgroundColor: Colors.orange,
          ),
        );
        return;
      }

      if (_startDate == null || _endDate == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Please select start and end dates'),
            backgroundColor: Colors.orange,
          ),
        );
        return;
      }

      setState(() => _isLoading = true);

      try {
        final request = {
          'id': widget.internship.id,
          'title': _titleController.text.trim(),
          'description': _descriptionController.text.trim(),
          'company': _companyController.text.trim(),
          'location': _locationController.text.trim(),
          'sectorId': _selectedSector!.id,
          'startDate': _startDate!.toIso8601String(),
          'endDate': _endDate!.toIso8601String(),
          'status': asDraft ? 'DRAFT' : 'PENDING_VALIDATION',
        };

        await ref.read(internshipsProvider.notifier).updateInternship(request);

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                asDraft
                    ? 'Internship updated as draft'
                    : 'Internship submitted for validation',
              ),
              backgroundColor: Colors.green,
            ),
          );
          context.pop(true); // Return true to indicate update
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(e.toString()), backgroundColor: Colors.red),
          );
        }
      } finally {
        if (mounted) {
          setState(() => _isLoading = false);
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final sectorsAsync = ref.watch(sectorsListProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Edit Internship')),
      body: sectorsAsync.when(
        data: (sectors) {
          // Set selected sector if not already set
          if (_selectedSector == null) {
            _selectedSector = sectors.firstWhere(
              (s) => s.id == widget.internship.sector.id,
              orElse: () => sectors.first,
            );
          }

          return SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    if (widget.internship.status != InternshipStatus.DRAFT)
                      Container(
                        padding: const EdgeInsets.all(12),
                        margin: const EdgeInsets.only(bottom: 16),
                        decoration: BoxDecoration(
                          color: Colors.orange.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: Colors.orange),
                        ),
                        child: const Row(
                          children: [
                            Icon(Icons.warning, color: Colors.orange),
                            SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                'Only draft internships can be edited',
                                style: TextStyle(color: Colors.orange),
                              ),
                            ),
                          ],
                        ),
                      ),
                    TextFormField(
                      controller: _titleController,
                      enabled:
                          widget.internship.status == InternshipStatus.DRAFT,
                      decoration: const InputDecoration(
                        labelText: 'Title *',
                        prefixIcon: Icon(Icons.title),
                        border: OutlineInputBorder(),
                      ),
                      validator: (value) {
                        if (value == null || value.trim().isEmpty) {
                          return 'Please enter a title';
                        }
                        if (value.trim().length < 3) {
                          return 'Title must be at least 3 characters';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    DropdownButtonFormField<Sector>(
                      initialValue: _selectedSector,
                      decoration: const InputDecoration(
                        labelText: 'Sector *',
                        prefixIcon: Icon(Icons.category),
                        border: OutlineInputBorder(),
                      ),
                      items: sectors.map((sector) {
                        return DropdownMenuItem<Sector>(
                          value: sector,
                          child: Text(sector.name),
                        );
                      }).toList(),
                      onChanged:
                          widget.internship.status == InternshipStatus.DRAFT
                          ? (sector) {
                              setState(() => _selectedSector = sector);
                            }
                          : null,
                      validator: (value) {
                        if (value == null) {
                          return 'Please select a sector';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _companyController,
                      enabled:
                          widget.internship.status == InternshipStatus.DRAFT,
                      decoration: const InputDecoration(
                        labelText: 'Company *',
                        prefixIcon: Icon(Icons.business),
                        border: OutlineInputBorder(),
                      ),
                      validator: (value) {
                        if (value == null || value.trim().isEmpty) {
                          return 'Please enter a company name';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _locationController,
                      enabled:
                          widget.internship.status == InternshipStatus.DRAFT,
                      decoration: const InputDecoration(
                        labelText: 'Location *',
                        prefixIcon: Icon(Icons.location_on),
                        border: OutlineInputBorder(),
                      ),
                      validator: (value) {
                        if (value == null || value.trim().isEmpty) {
                          return 'Please enter a location';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Expanded(
                          child: InkWell(
                            onTap:
                                widget.internship.status ==
                                    InternshipStatus.DRAFT
                                ? () => _selectDate(context, true)
                                : null,
                            child: InputDecorator(
                              decoration: const InputDecoration(
                                labelText: 'Start Date *',
                                prefixIcon: Icon(Icons.calendar_today),
                                border: OutlineInputBorder(),
                              ),
                              child: Text(
                                '${_startDate?.day ?? 'Select'}/${_startDate?.month ?? ''}/${_startDate?.year ?? 'date'}',
                                style: TextStyle(
                                  color: _startDate != null
                                      ? Colors.black
                                      : Colors.grey,
                                ),
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: InkWell(
                            onTap:
                                widget.internship.status ==
                                    InternshipStatus.DRAFT
                                ? () => _selectDate(context, false)
                                : null,
                            child: InputDecorator(
                              decoration: const InputDecoration(
                                labelText: 'End Date *',
                                prefixIcon: Icon(Icons.event),
                                border: OutlineInputBorder(),
                              ),
                              child: Text(
                                '${_endDate?.day ?? 'Select'}/${_endDate?.month ?? ''}/${_endDate?.year ?? 'date'}',
                                style: TextStyle(
                                  color: _endDate != null
                                      ? Colors.black
                                      : Colors.grey,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _descriptionController,
                      enabled:
                          widget.internship.status == InternshipStatus.DRAFT,
                      maxLines: 5,
                      decoration: const InputDecoration(
                        labelText: 'Description *',
                        prefixIcon: Icon(Icons.description),
                        border: OutlineInputBorder(),
                        alignLabelWithHint: true,
                      ),
                      validator: (value) {
                        if (value == null || value.trim().isEmpty) {
                          return 'Please enter a description';
                        }
                        if (value.trim().length < 20) {
                          return 'Description must be at least 20 characters';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 24),
                    if (widget.internship.status == InternshipStatus.DRAFT)
                      Row(
                        children: [
                          Expanded(
                            child: OutlinedButton.icon(
                              onPressed: _isLoading
                                  ? null
                                  : () => _handleUpdate(true),
                              icon: const Icon(Icons.save),
                              label: const Text('Save as Draft'),
                              style: OutlinedButton.styleFrom(
                                padding: const EdgeInsets.symmetric(
                                  vertical: 16,
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: ElevatedButton.icon(
                              onPressed: _isLoading
                                  ? null
                                  : () => _handleUpdate(false),
                              icon: _isLoading
                                  ? const SizedBox(
                                      width: 16,
                                      height: 16,
                                      child: CircularProgressIndicator(
                                        strokeWidth: 2,
                                        valueColor:
                                            AlwaysStoppedAnimation<Color>(
                                              Colors.white,
                                            ),
                                      ),
                                    )
                                  : const Icon(Icons.send),
                              label: const Text('Submit'),
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.blue,
                                foregroundColor: Colors.white,
                                padding: const EdgeInsets.symmetric(
                                  vertical: 16,
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                  ],
                ),
              ),
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error, size: 64, color: Colors.red),
              const SizedBox(height: 16),
              Text('Error loading sectors: $error'),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => ref.refresh(sectorsListProvider),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
