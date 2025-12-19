import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:timeago/timeago.dart' as timeago;
import '../../services/offline_sync_service.dart';

// Provider for sync status
final syncStatusProvider = FutureProvider.autoDispose<SyncStatus>((ref) async {
  final syncService = OfflineSyncService();
  return await syncService.getSyncStatus();
});

class SyncStatusWidget extends ConsumerWidget {
  const SyncStatusWidget({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final syncStatusAsync = ref.watch(syncStatusProvider);

    return syncStatusAsync.when(
      loading: () => const SizedBox.shrink(),
      error: (error, stack) => const SizedBox.shrink(),
      data: (status) {
        // Don't show anything if online and no pending actions
        if (status.isOnline && status.pendingActions == 0) {
          return const SizedBox.shrink();
        }

        return Container(
          margin: const EdgeInsets.all(8),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          decoration: BoxDecoration(
            color: status.isOnline
                ? Colors.green.shade50
                : Colors.orange.shade50,
            border: Border.all(
              color: status.isOnline ? Colors.green : Colors.orange,
              width: 1,
            ),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Row(
            children: [
              Icon(
                status.isOnline ? Icons.cloud_done : Icons.cloud_off,
                size: 20,
                color: status.isOnline ? Colors.green : Colors.orange,
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      status.isOnline ? 'Online' : 'Offline Mode',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                        color: status.isOnline
                            ? Colors.green.shade900
                            : Colors.orange.shade900,
                      ),
                    ),
                    if (status.pendingActions > 0)
                      Text(
                        '${status.pendingActions} pending ${status.pendingActions == 1 ? 'action' : 'actions'}',
                        style: TextStyle(
                          fontSize: 11,
                          color: status.isOnline
                              ? Colors.green.shade700
                              : Colors.orange.shade700,
                        ),
                      ),
                    if (status.lastSyncTime != null)
                      Text(
                        'Last synced ${timeago.format(status.lastSyncTime!)}',
                        style: TextStyle(
                          fontSize: 10,
                          color: Colors.grey.shade600,
                        ),
                      ),
                  ],
                ),
              ),
              if (status.pendingActions > 0 && status.isOnline)
                IconButton(
                  icon: const Icon(Icons.sync, size: 20),
                  onPressed: () => _showSyncDialog(context, ref),
                  tooltip: 'Sync now',
                  color: Colors.green,
                ),
            ],
          ),
        );
      },
    );
  }

  void _showSyncDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => _SyncDialog(ref: ref),
    );
  }
}

class _SyncDialog extends StatefulWidget {
  final WidgetRef ref;

  const _SyncDialog({required this.ref});

  @override
  State<_SyncDialog> createState() => _SyncDialogState();
}

class _SyncDialogState extends State<_SyncDialog> {
  bool _isSyncing = false;
  String? _message;
  bool? _success;

  @override
  void initState() {
    super.initState();
    _performSync();
  }

  Future<void> _performSync() async {
    setState(() {
      _isSyncing = true;
      _message = 'Syncing data...';
    });

    try {
      final syncService = OfflineSyncService();
      final result = await syncService.processSyncQueue();

      setState(() {
        _isSyncing = false;
        _success = result.success;
        _message = result.message;
      });

      // Refresh sync status provider
      widget.ref.invalidate(syncStatusProvider);
    } catch (e) {
      setState(() {
        _isSyncing = false;
        _success = false;
        _message = 'Sync failed: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Row(
        children: [
          if (_isSyncing)
            const SizedBox(
              width: 24,
              height: 24,
              child: CircularProgressIndicator(strokeWidth: 2),
            )
          else if (_success == true)
            const Icon(Icons.check_circle, color: Colors.green, size: 24)
          else if (_success == false)
            const Icon(Icons.error, color: Colors.red, size: 24),
          const SizedBox(width: 12),
          const Text('Syncing'),
        ],
      ),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(_message ?? ''),
          if (_isSyncing) ...[
            const SizedBox(height: 16),
            const LinearProgressIndicator(),
          ],
        ],
      ),
      actions: [
        if (!_isSyncing)
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
      ],
    );
  }
}

// Floating sync button that can be added to any screen
class SyncFloatingButton extends ConsumerWidget {
  const SyncFloatingButton({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final syncStatusAsync = ref.watch(syncStatusProvider);

    return syncStatusAsync.when(
      loading: () => const SizedBox.shrink(),
      error: (error, stack) => const SizedBox.shrink(),
      data: (status) {
        // Only show if there are pending actions and we're online
        if (status.pendingActions == 0 || !status.isOnline) {
          return const SizedBox.shrink();
        }

        return FloatingActionButton.extended(
          onPressed: () => _showSyncDialog(context, ref),
          icon: const Icon(Icons.sync),
          label: Text('Sync (${status.pendingActions})'),
          backgroundColor: Colors.green,
        );
      },
    );
  }

  void _showSyncDialog(BuildContext context, WidgetRef ref) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => _SyncDialog(ref: ref),
    );
  }
}
