import 'dart:io';
import 'database_helper.dart';
import 'api_service.dart';
import 'internship_service.dart';
import '../models/internship.dart';

class OfflineSyncService {
  final DatabaseHelper _dbHelper = DatabaseHelper();
  final ApiService _apiService = ApiService();
  final InternshipService _internshipService = InternshipService();

  // Check if device is online
  Future<bool> isOnline() async {
    try {
      final result = await InternetAddress.lookup('google.com');
      return result.isNotEmpty && result[0].rawAddress.isNotEmpty;
    } on SocketException catch (_) {
      return false;
    }
  }

  // Sync internships from server to local database
  Future<void> syncInternshipsFromServer() async {
    if (!await isOnline()) {
      throw Exception('No internet connection');
    }

    try {
      // Fetch internships from server
      final internships = await _internshipService.getAll();

      // Clear existing cached internships
      await _dbHelper.clearInternships();

      // Store new internships in local database
      for (final internship in internships) {
        await _dbHelper.insertInternship(internship);
      }
    } catch (e) {
      throw Exception('Failed to sync internships: $e');
    }
  }

  // Sync sectors from server to local database
  Future<void> syncSectorsFromServer() async {
    if (!await isOnline()) {
      throw Exception('No internet connection');
    }

    try {
      final response = await _apiService.get('/sectors');
      final List<dynamic> sectorsJson = response.data;
      final sectors = sectorsJson.map((json) => Sector.fromJson(json)).toList();

      // Clear existing cached sectors
      await _dbHelper.clearSectors();

      // Store new sectors in local database
      for (final sector in sectors) {
        await _dbHelper.insertSector(sector);
      }
    } catch (e) {
      throw Exception('Failed to sync sectors: $e');
    }
  }

  // Sync all data from server
  Future<void> syncAllFromServer() async {
    await syncInternshipsFromServer();
    await syncSectorsFromServer();
  }

  // Get internships from local cache
  Future<List<Internship>> getCachedInternships() async {
    return await _dbHelper.getInternships();
  }

  // Get sectors from local cache
  Future<List<Sector>> getCachedSectors() async {
    return await _dbHelper.getSectors();
  }

  // Save internship for offline creation
  Future<void> queueOfflineCreate(Map<String, dynamic> internshipData) async {
    await _dbHelper.addToSyncQueue('CREATE', '/internships', internshipData);
  }

  // Save internship for offline update
  Future<void> queueOfflineUpdate(
    int id,
    Map<String, dynamic> internshipData,
  ) async {
    await _dbHelper.addToSyncQueue(
      'UPDATE',
      '/internships/$id',
      internshipData,
    );
  }

  // Save internship for offline deletion
  Future<void> queueOfflineDelete(int id) async {
    await _dbHelper.addToSyncQueue('DELETE', '/internships/$id', {});
  }

  // Process offline sync queue
  Future<SyncResult> processSyncQueue() async {
    if (!await isOnline()) {
      return SyncResult(
        success: false,
        message: 'No internet connection',
        syncedCount: 0,
        failedCount: 0,
      );
    }

    final queue = await _dbHelper.getSyncQueue();
    int syncedCount = 0;
    int failedCount = 0;
    final List<String> errors = [];

    for (final item in queue) {
      try {
        final action = item['action'] as String;
        final endpoint = item['endpoint'] as String;
        final data = item['data'] as Map<String, dynamic>;

        switch (action) {
          case 'CREATE':
            await _apiService.post(endpoint, data: data);
            break;
          case 'UPDATE':
            await _apiService.put(endpoint, data: data);
            break;
          case 'DELETE':
            await _apiService.delete(endpoint);
            break;
        }

        // Remove from queue on success
        await _dbHelper.removeSyncQueueItem(item['id'] as int);
        syncedCount++;
      } catch (e) {
        failedCount++;
        errors.add('Failed to sync ${item['action']} ${item['endpoint']}: $e');
      }
    }

    // If sync was successful, refresh cache from server
    if (syncedCount > 0) {
      try {
        await syncAllFromServer();
      } catch (e) {
        errors.add('Failed to refresh cache after sync: $e');
      }
    }

    return SyncResult(
      success: failedCount == 0,
      message: failedCount == 0
          ? 'Successfully synced $syncedCount items'
          : 'Synced $syncedCount items, $failedCount failed',
      syncedCount: syncedCount,
      failedCount: failedCount,
      errors: errors.isNotEmpty ? errors : null,
    );
  }

  // Get sync status
  Future<SyncStatus> getSyncStatus() async {
    final queueItems = await _dbHelper.getSyncQueue();
    final lastSyncTime = await _dbHelper.getLastSyncTime();
    final online = await isOnline();

    return SyncStatus(
      isOnline: online,
      pendingActions: queueItems.length,
      lastSyncTime: lastSyncTime,
    );
  }

  // Clear all offline data
  Future<void> clearOfflineData() async {
    await _dbHelper.clearAllData();
  }
}

class SyncResult {
  final bool success;
  final String message;
  final int syncedCount;
  final int failedCount;
  final List<String>? errors;

  SyncResult({
    required this.success,
    required this.message,
    required this.syncedCount,
    required this.failedCount,
    this.errors,
  });
}

class SyncStatus {
  final bool isOnline;
  final int pendingActions;
  final DateTime? lastSyncTime;

  SyncStatus({
    required this.isOnline,
    required this.pendingActions,
    this.lastSyncTime,
  });
}
