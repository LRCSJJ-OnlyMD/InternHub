import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:file_picker/file_picker.dart';
import '../../models/internship.dart';
import '../../providers/internship_provider.dart';
import '../../utils/app_theme.dart';
import '../shared/comments_section.dart';
import 'edit_internship_screen.dart';

class InternshipDetailScreen extends ConsumerStatefulWidget {
  final Internship internship;

  const InternshipDetailScreen({super.key, required this.internship});

  @override
  ConsumerState<InternshipDetailScreen> createState() =>
      _InternshipDetailScreenState();
}

class _InternshipDetailScreenState
    extends ConsumerState<InternshipDetailScreen> {
  bool _isUploading = false;
  bool _isDeleting = false;
  bool _isSubmitting = false;

  Future<void> _handleUploadReport() async {
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['pdf', 'doc', 'docx'],
      );

      if (result != null && result.files.single.path != null) {
        setState(() => _isUploading = true);

        // TODO: Implement upload report API call
        await Future.delayed(const Duration(seconds: 2));

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Report uploaded successfully'),
              backgroundColor: Colors.green,
            ),
          );
          ref.read(internshipsProvider.notifier).loadInternships();
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error uploading report: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isUploading = false);
      }
    }
  }

  Future<void> _handleDownloadReport() async {
    try {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Downloading report...'),
          backgroundColor: Colors.blue,
        ),
      );

      // TODO: Implement download report API call
      await Future.delayed(const Duration(seconds: 2));

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Report downloaded successfully'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error downloading report: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _handleSubmit() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Submit Internship'),
        content: const Text(
          'Are you sure you want to submit this internship for validation? You won\'t be able to edit it afterwards.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.blue,
              foregroundColor: Colors.white,
            ),
            child: const Text('Submit'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      setState(() => _isSubmitting = true);

      try {
        await ref
            .read(internshipsProvider.notifier)
            .submitInternship(widget.internship.id);

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Internship submitted successfully'),
              backgroundColor: Colors.green,
            ),
          );
          context.pop(true);
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Error submitting: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      } finally {
        if (mounted) {
          setState(() => _isSubmitting = false);
        }
      }
    }
  }

  Future<void> _handleDelete() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Internship'),
        content: const Text(
          'Are you sure you want to delete this internship? This action cannot be undone.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
            ),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      setState(() => _isDeleting = true);

      try {
        await ref
            .read(internshipsProvider.notifier)
            .deleteInternship(widget.internship.id);

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Internship deleted successfully'),
              backgroundColor: Colors.green,
            ),
          );
          context.pop(true);
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Error deleting: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      } finally {
        if (mounted) {
          setState(() => _isDeleting = false);
        }
      }
    }
  }

  Future<void> _handleEdit() async {
    final result = await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) =>
            EditInternshipScreen(internship: widget.internship),
      ),
    );

    if (result == true) {
      // Reload internships after edit
      ref.read(internshipsProvider.notifier).loadInternships();
      if (mounted) {
        context.pop(true);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final internship = widget.internship;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Internship Details'),
        actions: [
          if (internship.status == InternshipStatus.DRAFT)
            PopupMenuButton<String>(
              onSelected: (value) {
                switch (value) {
                  case 'edit':
                    _handleEdit();
                    break;
                  case 'delete':
                    _handleDelete();
                    break;
                }
              },
              itemBuilder: (context) => [
                const PopupMenuItem(
                  value: 'edit',
                  child: Row(
                    children: [
                      Icon(Icons.edit, size: 20),
                      SizedBox(width: 8),
                      Text('Edit'),
                    ],
                  ),
                ),
                const PopupMenuItem(
                  value: 'delete',
                  child: Row(
                    children: [
                      Icon(Icons.delete, size: 20, color: Colors.red),
                      SizedBox(width: 8),
                      Text('Delete', style: TextStyle(color: Colors.red)),
                    ],
                  ),
                ),
              ],
            ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Status Badge
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: AppTheme.getStatusColor(internship.status.value),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                internship.status.displayName,
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 12,
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Title
            Text(
              internship.title,
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 24),

            // Company Information
            _buildSection('Company Information', [
              _buildInfoRow(Icons.business, 'Company', internship.company),
              _buildInfoRow(Icons.location_on, 'Location', internship.location),
            ]),

            const SizedBox(height: 24),

            // Internship Details
            _buildSection('Internship Details', [
              _buildInfoRow(Icons.category, 'Sector', internship.sectorName),
              _buildInfoRow(
                Icons.calendar_today,
                'Start Date',
                '${internship.startDate.day}/${internship.startDate.month}/${internship.startDate.year}',
              ),
              _buildInfoRow(
                Icons.event,
                'End Date',
                '${internship.endDate.day}/${internship.endDate.month}/${internship.endDate.year}',
              ),
              _buildInfoRow(
                Icons.timelapse,
                'Duration',
                '${internship.durationInDays} days',
              ),
            ]),

            const SizedBox(height: 24),

            // Description
            if (internship.description != null) ...[
              _buildSection('Description', [
                Text(
                  internship.description!,
                  style: const TextStyle(fontSize: 14, height: 1.5),
                ),
              ]),
              const SizedBox(height: 24),
            ],

            // Student Information
            _buildSection('Student Information', [
              _buildInfoRow(Icons.person, 'Name', internship.studentName),
              _buildInfoRow(Icons.email, 'Email', internship.studentEmail),
            ]),

            const SizedBox(height: 24),

            // Instructor Information (if assigned)
            if (internship.instructor != null) ...[
              _buildSection('Instructor Information', [
                _buildInfoRow(
                  Icons.school,
                  'Name',
                  internship.instructorName ?? 'Not assigned',
                ),
              ]),
              const SizedBox(height: 24),
            ],

            // Refusal Comment (if refused)
            if (internship.status == InternshipStatus.REFUSED &&
                internship.refusalComment != null) ...[
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.red.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.red),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Row(
                      children: [
                        Icon(Icons.cancel, color: Colors.red),
                        SizedBox(width: 8),
                        Text(
                          'Refusal Reason',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 16,
                            color: Colors.red,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Text(
                      internship.refusalComment!,
                      style: const TextStyle(fontSize: 14, height: 1.5),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
            ],

            // Documents Section
            if (internship.status == InternshipStatus.VALIDATED) ...[
              _buildSection('Documents', [
                if (internship.hasReport)
                  Card(
                    child: ListTile(
                      leading: const Icon(
                        Icons.description,
                        color: Colors.blue,
                      ),
                      title: const Text('Internship Report'),
                      trailing: IconButton(
                        icon: const Icon(Icons.download),
                        onPressed: _handleDownloadReport,
                      ),
                    ),
                  ),
                const SizedBox(height: 12),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: _isUploading ? null : _handleUploadReport,
                    icon: _isUploading
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.upload_file),
                    label: Text(
                      _isUploading ? 'Uploading...' : 'Upload Report',
                    ),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 16),
                    ),
                  ),
                ),
              ]),
              const SizedBox(height: 24),
            ],

            // Comments Section
            const SizedBox(height: 8),
            CommentsSection(internshipId: internship.id),
          ],
        ),
      ),
      bottomNavigationBar: _buildBottomActions(internship),
    );
  }

  Widget _buildSection(String title, List<Widget> children) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        ...children,
      ],
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Icon(icon, size: 20, color: Colors.grey[600]),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                ),
                const SizedBox(height: 4),
                Text(
                  value,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget? _buildBottomActions(Internship internship) {
    if (internship.status == InternshipStatus.DRAFT) {
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
        child: Row(
          children: [
            Expanded(
              child: OutlinedButton.icon(
                onPressed: _isDeleting ? null : _handleEdit,
                icon: const Icon(Icons.edit),
                label: const Text('Edit'),
                style: OutlinedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              flex: 2,
              child: ElevatedButton.icon(
                onPressed: _isSubmitting ? null : _handleSubmit,
                icon: _isSubmitting
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          valueColor: AlwaysStoppedAnimation<Color>(
                            Colors.white,
                          ),
                        ),
                      )
                    : const Icon(Icons.send),
                label: const Text('Submit for Validation'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.blue,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                ),
              ),
            ),
          ],
        ),
      );
    }
    return null;
  }
}
