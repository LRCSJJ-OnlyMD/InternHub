import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/internship.dart';
import '../../providers/admin_provider.dart';
import '../../providers/sector_provider.dart';

class SectorsManagementScreen extends ConsumerWidget {
  const SectorsManagementScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sectorsAsync = ref.watch(sectorsListProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Sectors Management'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            tooltip: 'Add Sector',
            onPressed: () => _showSectorDialog(context, ref),
          ),
        ],
      ),
      body: sectorsAsync.when(
        data: (sectors) {
          if (sectors.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.category_outlined,
                    size: 64,
                    color: Colors.grey[400],
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'No sectors available',
                    style: TextStyle(fontSize: 16, color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton.icon(
                    onPressed: () => _showSectorDialog(context, ref),
                    icon: const Icon(Icons.add),
                    label: const Text('Add First Sector'),
                  ),
                ],
              ),
            );
          }

          return RefreshIndicator(
            onRefresh: () async {
              ref.invalidate(sectorsListProvider);
            },
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: sectors.length,
              itemBuilder: (context, index) {
                final sector = sectors[index];
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: Colors.green.withOpacity(0.1),
                      child: const Icon(Icons.category, color: Colors.green),
                    ),
                    title: Text(
                      sector.name,
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                    subtitle: sector.code?.isNotEmpty == true
                        ? Text('Code: ${sector.code}')
                        : null,
                    trailing: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          icon: const Icon(Icons.edit, size: 20),
                          onPressed: () =>
                              _showSectorDialog(context, ref, sector: sector),
                          tooltip: 'Edit sector',
                        ),
                        IconButton(
                          icon: const Icon(
                            Icons.delete,
                            size: 20,
                            color: Colors.red,
                          ),
                          onPressed: () =>
                              _showDeleteConfirmation(context, ref, sector),
                          tooltip: 'Delete sector',
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 64, color: Colors.red),
              const SizedBox(height: 16),
              Text(
                'Error loading sectors',
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
                onPressed: () => ref.invalidate(sectorsListProvider),
                icon: const Icon(Icons.refresh),
                label: const Text('Retry'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _showSectorDialog(
    BuildContext context,
    WidgetRef ref, {
    Sector? sector,
  }) async {
    final nameController = TextEditingController(text: sector?.name ?? '');
    final codeController = TextEditingController(text: sector?.code ?? '');
    final formKey = GlobalKey<FormState>();
    final isEdit = sector != null;

    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(isEdit ? 'Edit Sector' : 'Add Sector'),
        content: Form(
          key: formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: nameController,
                decoration: const InputDecoration(
                  labelText: 'Sector Name',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter sector name';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: codeController,
                decoration: const InputDecoration(
                  labelText: 'Sector Code',
                  border: OutlineInputBorder(),
                  hintText: 'Optional',
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              if (formKey.currentState!.validate()) {
                Navigator.of(context).pop(true);
              }
            },
            child: Text(isEdit ? 'Update' : 'Create'),
          ),
        ],
      ),
    );

    if (result == true && context.mounted) {
      try {
        final sectorData = {
          'name': nameController.text.trim(),
          'code': codeController.text.trim().isEmpty
              ? null
              : codeController.text.trim(),
        };

        if (isEdit) {
          await ref
              .read(adminServiceProvider)
              .updateSector(sector.id, sectorData);
          if (context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Sector updated successfully')),
            );
          }
        } else {
          await ref.read(adminServiceProvider).createSector(sectorData);
          if (context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Sector created successfully')),
            );
          }
        }

        ref.invalidate(sectorsListProvider);
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
          );
        }
      }
    }
  }

  Future<void> _showDeleteConfirmation(
    BuildContext context,
    WidgetRef ref,
    Sector sector,
  ) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Sector'),
        content: Text(
          'Are you sure you want to delete "${sector.name}"? This action cannot be undone.\n\n'
          'Note: You cannot delete sectors that have internships associated with them.',
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

    if (confirmed == true && context.mounted) {
      try {
        await ref.read(adminServiceProvider).deleteSector(sector.id);
        ref.invalidate(sectorsListProvider);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('"${sector.name}" deleted successfully')),
          );
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Error deleting sector: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    }
  }
}
