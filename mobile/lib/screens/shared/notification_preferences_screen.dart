import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

// Notification preferences model
class NotificationPreferences {
  final bool internshipValidated;
  final bool internshipRefused;
  final bool newComment;
  final bool documentUploaded;
  final bool deadlineReminder;
  final bool assignment;

  NotificationPreferences({
    this.internshipValidated = true,
    this.internshipRefused = true,
    this.newComment = true,
    this.documentUploaded = true,
    this.deadlineReminder = true,
    this.assignment = true,
  });

  Map<String, dynamic> toJson() {
    return {
      'internshipValidated': internshipValidated,
      'internshipRefused': internshipRefused,
      'newComment': newComment,
      'documentUploaded': documentUploaded,
      'deadlineReminder': deadlineReminder,
      'assignment': assignment,
    };
  }

  factory NotificationPreferences.fromJson(Map<String, dynamic> json) {
    return NotificationPreferences(
      internshipValidated: json['internshipValidated'] ?? true,
      internshipRefused: json['internshipRefused'] ?? true,
      newComment: json['newComment'] ?? true,
      documentUploaded: json['documentUploaded'] ?? true,
      deadlineReminder: json['deadlineReminder'] ?? true,
      assignment: json['assignment'] ?? true,
    );
  }

  NotificationPreferences copyWith({
    bool? internshipValidated,
    bool? internshipRefused,
    bool? newComment,
    bool? documentUploaded,
    bool? deadlineReminder,
    bool? assignment,
  }) {
    return NotificationPreferences(
      internshipValidated: internshipValidated ?? this.internshipValidated,
      internshipRefused: internshipRefused ?? this.internshipRefused,
      newComment: newComment ?? this.newComment,
      documentUploaded: documentUploaded ?? this.documentUploaded,
      deadlineReminder: deadlineReminder ?? this.deadlineReminder,
      assignment: assignment ?? this.assignment,
    );
  }
}

// Preferences provider
final notificationPreferencesProvider = StateNotifierProvider<NotificationPreferencesNotifier, NotificationPreferences>((ref) {
  return NotificationPreferencesNotifier();
});

class NotificationPreferencesNotifier extends StateNotifier<NotificationPreferences> {
  final _storage = const FlutterSecureStorage();
  static const _key = 'notification_preferences';

  NotificationPreferencesNotifier() : super(NotificationPreferences()) {
    _loadPreferences();
  }

  Future<void> _loadPreferences() async {
    try {
      final json = await _storage.read(key: _key);
      if (json != null) {
        state = NotificationPreferences.fromJson(
          Map<String, dynamic>.from(
            // Parse JSON string
            Uri.splitQueryString(json).map((key, value) => MapEntry(key, value == 'true')),
          ),
        );
      }
    } catch (e) {
      // Ignore errors and use default preferences
    }
  }

  Future<void> _savePreferences() async {
    try {
      // Convert to simple query string format
      final json = state.toJson().entries.map((e) => '${e.key}=${e.value}').join('&');
      await _storage.write(key: _key, value: json);
    } catch (e) {
      // Ignore errors
    }
  }

  void updateInternshipValidated(bool value) {
    state = state.copyWith(internshipValidated: value);
    _savePreferences();
  }

  void updateInternshipRefused(bool value) {
    state = state.copyWith(internshipRefused: value);
    _savePreferences();
  }

  void updateNewComment(bool value) {
    state = state.copyWith(newComment: value);
    _savePreferences();
  }

  void updateDocumentUploaded(bool value) {
    state = state.copyWith(documentUploaded: value);
    _savePreferences();
  }

  void updateDeadlineReminder(bool value) {
    state = state.copyWith(deadlineReminder: value);
    _savePreferences();
  }

  void updateAssignment(bool value) {
    state = state.copyWith(assignment: value);
    _savePreferences();
  }
}

class NotificationPreferencesScreen extends ConsumerWidget {
  const NotificationPreferencesScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final preferences = ref.watch(notificationPreferencesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notification Preferences'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const Text(
            'Choose which notifications you want to receive',
            style: TextStyle(fontSize: 16, color: Colors.grey),
          ),
          const SizedBox(height: 24),
          _buildPreferenceCard(
            context,
            title: 'Internship Validated',
            subtitle: 'Get notified when your internship is validated',
            icon: Icons.check_circle,
            iconColor: Colors.green,
            value: preferences.internshipValidated,
            onChanged: (value) {
              ref.read(notificationPreferencesProvider.notifier)
                  .updateInternshipValidated(value);
            },
          ),
          const SizedBox(height: 12),
          _buildPreferenceCard(
            context,
            title: 'Internship Refused',
            subtitle: 'Get notified when your internship is refused',
            icon: Icons.cancel,
            iconColor: Colors.red,
            value: preferences.internshipRefused,
            onChanged: (value) {
              ref.read(notificationPreferencesProvider.notifier)
                  .updateInternshipRefused(value);
            },
          ),
          const SizedBox(height: 12),
          _buildPreferenceCard(
            context,
            title: 'New Comment',
            subtitle: 'Get notified when someone comments on your internship',
            icon: Icons.comment,
            iconColor: Colors.blue,
            value: preferences.newComment,
            onChanged: (value) {
              ref.read(notificationPreferencesProvider.notifier)
                  .updateNewComment(value);
            },
          ),
          const SizedBox(height: 12),
          _buildPreferenceCard(
            context,
            title: 'Document Uploaded',
            subtitle: 'Get notified when a document is uploaded',
            icon: Icons.upload_file,
            iconColor: Colors.orange,
            value: preferences.documentUploaded,
            onChanged: (value) {
              ref.read(notificationPreferencesProvider.notifier)
                  .updateDocumentUploaded(value);
            },
          ),
          const SizedBox(height: 12),
          _buildPreferenceCard(
            context,
            title: 'Deadline Reminder',
            subtitle: 'Get notified about upcoming deadlines',
            icon: Icons.alarm,
            iconColor: Colors.amber,
            value: preferences.deadlineReminder,
            onChanged: (value) {
              ref.read(notificationPreferencesProvider.notifier)
                  .updateDeadlineReminder(value);
            },
          ),
          const SizedBox(height: 12),
          _buildPreferenceCard(
            context,
            title: 'Assignment',
            subtitle: 'Get notified when you are assigned to an internship',
            icon: Icons.assignment,
            iconColor: Colors.purple,
            value: preferences.assignment,
            onChanged: (value) {
              ref.read(notificationPreferencesProvider.notifier)
                  .updateAssignment(value);
            },
          ),
          const SizedBox(height: 24),
          Card(
            color: Colors.blue.shade50,
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(Icons.info_outline, color: Colors.blue.shade700),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'These preferences apply to in-app notifications only. Email notification settings can be configured in your account settings.',
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.blue.shade900,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPreferenceCard(
    BuildContext context, {
    required String title,
    required String subtitle,
    required IconData icon,
    required Color iconColor,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    return Card(
      child: SwitchListTile(
        value: value,
        onChanged: onChanged,
        title: Text(title),
        subtitle: Text(subtitle),
        secondary: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: iconColor.withOpacity(0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(icon, color: iconColor),
        ),
      ),
    );
  }
}
