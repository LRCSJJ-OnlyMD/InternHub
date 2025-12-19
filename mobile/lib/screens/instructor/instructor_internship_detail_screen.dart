import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/internship.dart';
import '../../providers/internship_provider.dart';
import '../../utils/app_theme.dart';
import '../shared/comments_section.dart';

class InstructorInternshipDetailScreen extends ConsumerStatefulWidget {
  final Internship internship;

  const InstructorInternshipDetailScreen({super.key, required this.internship});

  @override
  ConsumerState<InstructorInternshipDetailScreen> createState() =>
      _InstructorInternshipDetailScreenState();
}

class _InstructorInternshipDetailScreenState
    extends ConsumerState<InstructorInternshipDetailScreen> {
  bool _isValidating = false;
  bool _isRefusing = false;

  Future<void> _handleValidate() async {
    final commentController = TextEditingController();

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Validate Internship'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Are you sure you want to validate this internship?'),
            const SizedBox(height: 16),
            TextField(
              controller: commentController,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: 'Comment (optional)',
                border: OutlineInputBorder(),
                hintText: 'Add a comment...',
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
      setState(() => _isValidating = true);

      try {
        // TODO: Implement validate API call with optional comment
        await Future.delayed(const Duration(seconds: 2));

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Internship validated successfully'),
              backgroundColor: Colors.green,
            ),
          );
          ref.read(internshipsProvider.notifier).loadInternships();
          context.pop(true);
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Error validating: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      } finally {
        if (mounted) {
          setState(() => _isValidating = false);
        }
      }
    }
  }

  Future<void> _handleRefuse() async {
    final reasonController = TextEditingController();

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Refuse Internship'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text(
              'Please provide a reason for refusing this internship.',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: reasonController,
              maxLines: 4,
              decoration: const InputDecoration(
                labelText: 'Reason (required)',
                border: OutlineInputBorder(),
                hintText: 'Explain why you are refusing this internship...',
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
              if (reasonController.text.trim().isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Please provide a reason'),
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

    if (confirmed == true && reasonController.text.trim().isNotEmpty) {
      setState(() => _isRefusing = true);

      try {
        // TODO: Implement refuse API call with reason
        await Future.delayed(const Duration(seconds: 2));

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Internship refused'),
              backgroundColor: Colors.orange,
            ),
          );
          ref.read(internshipsProvider.notifier).loadInternships();
          context.pop(true);
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Error refusing: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      } finally {
        if (mounted) {
          setState(() => _isRefusing = false);
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final internship = widget.internship;

    return Scaffold(
      appBar: AppBar(title: const Text('Internship Details')),
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

            // Student Information
            _buildSection('Student Information', [
              _buildInfoRow(Icons.person, 'Name', internship.studentName),
              _buildInfoRow(Icons.email, 'Email', internship.studentEmail),
            ]),

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

            // Validation Info (if validated)
            if (internship.status == InternshipStatus.VALIDATED) ...[
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.green.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.green),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.check_circle, color: Colors.green),
                    SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'This internship has been validated',
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                          color: Colors.green,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
            ],

            // Report Info (if validated)
            if (internship.status == InternshipStatus.VALIDATED) ...[
              _buildSection('Report Status', [
                Card(
                  child: ListTile(
                    leading: Icon(
                      internship.hasReport ? Icons.check_circle : Icons.pending,
                      color: internship.hasReport
                          ? Colors.green
                          : Colors.orange,
                    ),
                    title: Text(
                      internship.hasReport
                          ? 'Report submitted'
                          : 'Waiting for report',
                    ),
                    subtitle: Text(
                      internship.hasReport
                          ? 'Student has uploaded the internship report'
                          : 'Student has not uploaded the report yet',
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
      bottomNavigationBar:
          internship.status == InternshipStatus.PENDING_VALIDATION
          ? _buildActionButtons()
          : null,
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

  Widget _buildActionButtons() {
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
              onPressed: (_isValidating || _isRefusing) ? null : _handleRefuse,
              icon: _isRefusing
                  ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Icon(Icons.cancel),
              label: const Text('Refuse'),
              style: OutlinedButton.styleFrom(
                foregroundColor: Colors.red,
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            flex: 2,
            child: ElevatedButton.icon(
              onPressed: (_isValidating || _isRefusing)
                  ? null
                  : _handleValidate,
              icon: _isValidating
                  ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                      ),
                    )
                  : const Icon(Icons.check_circle),
              label: const Text('Validate'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.green,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
