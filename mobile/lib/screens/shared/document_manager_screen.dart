import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:file_picker/file_picker.dart';
import 'package:path/path.dart' as path_utils;
import 'package:timeago/timeago.dart' as timeago;

// Document model
class Document {
  final int id;
  final String name;
  final String type;
  final int size;
  final DateTime uploadedAt;
  final String? uploadedBy;

  Document({
    required this.id,
    required this.name,
    required this.type,
    required this.size,
    required this.uploadedAt,
    this.uploadedBy,
  });

  factory Document.fromJson(Map<String, dynamic> json) {
    return Document(
      id: json['id'],
      name: json['name'],
      type: json['type'] ?? 'Unknown',
      size: json['size'] ?? 0,
      uploadedAt: json['uploadedAt'] != null
          ? DateTime.parse(json['uploadedAt'])
          : DateTime.now(),
      uploadedBy: json['uploadedBy'],
    );
  }

  String get sizeFormatted {
    if (size < 1024) return '$size B';
    if (size < 1024 * 1024) return '${(size / 1024).toStringAsFixed(1)} KB';
    return '${(size / (1024 * 1024)).toStringAsFixed(1)} MB';
  }

  IconData get icon {
    final ext = path_utils.extension(name).toLowerCase();
    switch (ext) {
      case '.pdf':
        return Icons.picture_as_pdf;
      case '.doc':
      case '.docx':
        return Icons.description;
      case '.xls':
      case '.xlsx':
        return Icons.table_chart;
      case '.ppt':
      case '.pptx':
        return Icons.slideshow;
      case '.jpg':
      case '.jpeg':
      case '.png':
      case '.gif':
        return Icons.image;
      case '.zip':
      case '.rar':
        return Icons.folder_zip;
      default:
        return Icons.insert_drive_file;
    }
  }

  Color get iconColor {
    final ext = path_utils.extension(name).toLowerCase();
    switch (ext) {
      case '.pdf':
        return Colors.red;
      case '.doc':
      case '.docx':
        return Colors.blue;
      case '.xls':
      case '.xlsx':
        return Colors.green;
      case '.ppt':
      case '.pptx':
        return Colors.orange;
      case '.jpg':
      case '.jpeg':
      case '.png':
      case '.gif':
        return Colors.purple;
      default:
        return Colors.grey;
    }
  }
}

// Placeholder provider - in real app would fetch from API
final documentsProvider = FutureProvider.family<List<Document>, int>((
  ref,
  internshipId,
) async {
  // Simulated API response
  return [
    Document(
      id: 1,
      name: 'internship_report.pdf',
      type: 'PDF',
      size: 2457600,
      uploadedAt: DateTime.now().subtract(const Duration(days: 2)),
      uploadedBy: 'John Doe',
    ),
    Document(
      id: 2,
      name: 'presentation.pptx',
      type: 'PowerPoint',
      size: 5242880,
      uploadedAt: DateTime.now().subtract(const Duration(days: 5)),
      uploadedBy: 'John Doe',
    ),
  ];
});

class DocumentManagerScreen extends ConsumerStatefulWidget {
  final int internshipId;
  final String internshipTitle;

  const DocumentManagerScreen({
    super.key,
    required this.internshipId,
    required this.internshipTitle,
  });

  @override
  ConsumerState<DocumentManagerScreen> createState() =>
      _DocumentManagerScreenState();
}

class _DocumentManagerScreenState extends ConsumerState<DocumentManagerScreen> {
  bool _isUploading = false;
  double _uploadProgress = 0;
  String? _uploadingFileName;

  Future<void> _pickAndUploadFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: [
        'pdf',
        'doc',
        'docx',
        'xls',
        'xlsx',
        'ppt',
        'pptx',
        'jpg',
        'jpeg',
        'png',
      ],
      allowMultiple: false,
    );

    if (result != null && result.files.single.path != null) {
      final file = File(result.files.single.path!);
      final fileName = path_utils.basename(file.path);

      setState(() {
        _isUploading = true;
        _uploadProgress = 0;
        _uploadingFileName = fileName;
      });

      try {
        // Simulate upload progress
        for (int i = 0; i <= 100; i += 10) {
          await Future.delayed(const Duration(milliseconds: 200));
          setState(() {
            _uploadProgress = i / 100;
          });
        }

        // In real app: await uploadService.uploadDocument(widget.internshipId, file);

        if (mounted) {
          setState(() {
            _isUploading = false;
            _uploadProgress = 0;
            _uploadingFileName = null;
          });

          // Refresh documents list
          ref.invalidate(documentsProvider(widget.internshipId));

          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('$fileName uploaded successfully'),
              backgroundColor: Colors.green,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          setState(() {
            _isUploading = false;
            _uploadProgress = 0;
            _uploadingFileName = null;
          });

          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Failed to upload: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    }
  }

  Future<void> _pickAndUploadMultipleFiles() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: [
        'pdf',
        'doc',
        'docx',
        'xls',
        'xlsx',
        'ppt',
        'pptx',
        'jpg',
        'jpeg',
        'png',
      ],
      allowMultiple: true,
    );

    if (result != null && result.files.isNotEmpty) {
      setState(() {
        _isUploading = true;
        _uploadProgress = 0;
        _uploadingFileName = '${result.files.length} files';
      });

      try {
        int completed = 0;
        for (final file in result.files) {
          if (file.path != null) {
            // In real app: await uploadService.uploadDocument(widget.internshipId, File(file.path!));
            await Future.delayed(const Duration(milliseconds: 500));
          }
          completed++;
          setState(() {
            _uploadProgress = completed / result.files.length;
          });
        }

        if (mounted) {
          setState(() {
            _isUploading = false;
            _uploadProgress = 0;
            _uploadingFileName = null;
          });

          // Refresh documents list
          ref.invalidate(documentsProvider(widget.internshipId));

          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                '${result.files.length} files uploaded successfully',
              ),
              backgroundColor: Colors.green,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          setState(() {
            _isUploading = false;
            _uploadProgress = 0;
            _uploadingFileName = null;
          });

          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Failed to upload: $e'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    }
  }

  void _deleteDocument(Document document) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Document'),
        content: Text('Are you sure you want to delete "${document.name}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(context);

              try {
                // In real app: await documentService.deleteDocument(document.id);

                // Simulate deletion
                await Future.delayed(const Duration(milliseconds: 500));

                // Refresh documents list
                ref.invalidate(documentsProvider(widget.internshipId));

                if (mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('Document deleted successfully'),
                      backgroundColor: Colors.green,
                    ),
                  );
                }
              } catch (e) {
                if (mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text('Failed to delete: $e'),
                      backgroundColor: Colors.red,
                    ),
                  );
                }
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  void _downloadDocument(Document document) {
    // In real app: implement actual download logic
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Downloading ${document.name}...'),
        duration: const Duration(seconds: 2),
      ),
    );
  }

  void _viewDocument(Document document) {
    // In real app: open document viewer
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Opening ${document.name}...'),
        duration: const Duration(seconds: 2),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final documentsAsync = ref.watch(documentsProvider(widget.internshipId));

    return Scaffold(
      appBar: AppBar(title: const Text('Documents')),
      body: Column(
        children: [
          // Internship info card
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            margin: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Theme.of(context).primaryColor.withOpacity(0.1),
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: Theme.of(context).primaryColor.withOpacity(0.3),
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  widget.internshipTitle,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  'Internship ID: ${widget.internshipId}',
                  style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                ),
              ],
            ),
          ),

          // Upload progress indicator
          if (_isUploading)
            Container(
              padding: const EdgeInsets.all(16),
              margin: const EdgeInsets.symmetric(horizontal: 16),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Column(
                children: [
                  Row(
                    children: [
                      const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'Uploading $_uploadingFileName...',
                          style: const TextStyle(fontWeight: FontWeight.w500),
                        ),
                      ),
                      Text(
                        '${(_uploadProgress * 100).toInt()}%',
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  LinearProgressIndicator(value: _uploadProgress),
                ],
              ),
            ),

          // Documents list
          Expanded(
            child: documentsAsync.when(
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
                    Text('Error loading documents: $error'),
                  ],
                ),
              ),
              data: (documents) {
                if (documents.isEmpty) {
                  return Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.folder_open,
                          size: 80,
                          color: Colors.grey[400],
                        ),
                        const SizedBox(height: 16),
                        Text(
                          'No documents yet',
                          style: TextStyle(
                            fontSize: 18,
                            color: Colors.grey[600],
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Upload your first document to get started',
                          style: TextStyle(
                            fontSize: 14,
                            color: Colors.grey[500],
                          ),
                        ),
                      ],
                    ),
                  );
                }

                return ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: documents.length,
                  itemBuilder: (context, index) {
                    final document = documents[index];
                    return Card(
                      margin: const EdgeInsets.only(bottom: 12),
                      child: ListTile(
                        leading: CircleAvatar(
                          backgroundColor: document.iconColor.withOpacity(0.2),
                          child: Icon(document.icon, color: document.iconColor),
                        ),
                        title: Text(
                          document.name,
                          style: const TextStyle(fontWeight: FontWeight.w500),
                        ),
                        subtitle: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const SizedBox(height: 4),
                            Text(
                              '${document.sizeFormatted} • ${document.type}',
                            ),
                            Text(
                              'Uploaded ${timeago.format(document.uploadedAt)}',
                              style: TextStyle(
                                fontSize: 12,
                                color: Colors.grey[600],
                              ),
                            ),
                            if (document.uploadedBy != null)
                              Text(
                                'by ${document.uploadedBy}',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: Colors.grey[600],
                                ),
                              ),
                          ],
                        ),
                        isThreeLine: true,
                        trailing: PopupMenuButton<String>(
                          onSelected: (value) {
                            switch (value) {
                              case 'view':
                                _viewDocument(document);
                                break;
                              case 'download':
                                _downloadDocument(document);
                                break;
                              case 'delete':
                                _deleteDocument(document);
                                break;
                            }
                          },
                          itemBuilder: (context) => [
                            const PopupMenuItem(
                              value: 'view',
                              child: Row(
                                children: [
                                  Icon(Icons.visibility),
                                  SizedBox(width: 8),
                                  Text('View'),
                                ],
                              ),
                            ),
                            const PopupMenuItem(
                              value: 'download',
                              child: Row(
                                children: [
                                  Icon(Icons.download),
                                  SizedBox(width: 8),
                                  Text('Download'),
                                ],
                              ),
                            ),
                            const PopupMenuDivider(),
                            const PopupMenuItem(
                              value: 'delete',
                              child: Row(
                                children: [
                                  Icon(Icons.delete, color: Colors.red),
                                  SizedBox(width: 8),
                                  Text(
                                    'Delete',
                                    style: TextStyle(color: Colors.red),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                        onTap: () => _viewDocument(document),
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
      floatingActionButton: _isUploading
          ? null
          : Column(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                FloatingActionButton.extended(
                  onPressed: _pickAndUploadMultipleFiles,
                  heroTag: 'upload_multiple',
                  icon: const Icon(Icons.cloud_upload),
                  label: const Text('Upload Multiple'),
                ),
                const SizedBox(height: 12),
                FloatingActionButton.extended(
                  onPressed: _pickAndUploadFile,
                  heroTag: 'upload_single',
                  icon: const Icon(Icons.upload_file),
                  label: const Text('Upload File'),
                  backgroundColor: Colors.blue,
                ),
              ],
            ),
    );
  }
}
