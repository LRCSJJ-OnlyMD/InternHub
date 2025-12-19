import 'dart:convert';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/internship.dart';
import '../models/user.dart';

class DatabaseHelper {
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  static Database? _database;

  factory DatabaseHelper() => _instance;

  DatabaseHelper._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    final databasePath = await getDatabasesPath();
    final path = join(databasePath, 'internhub.db');

    return await openDatabase(path, version: 1, onCreate: _onCreate);
  }

  Future<void> _onCreate(Database db, int version) async {
    // Create internships table
    await db.execute('''
      CREATE TABLE internships (
        id INTEGER PRIMARY KEY,
        title TEXT NOT NULL,
        description TEXT,
        companyName TEXT NOT NULL,
        companyAddress TEXT,
        startDate TEXT NOT NULL,
        endDate TEXT NOT NULL,
        status TEXT NOT NULL,
        studentId INTEGER,
        instructorId INTEGER,
        sectorId INTEGER,
        createdAt TEXT,
        studentData TEXT,
        instructorData TEXT,
        sectorData TEXT,
        isSynced INTEGER DEFAULT 1
      )
    ''');

    // Create users table (for caching user data)
    await db.execute('''
      CREATE TABLE users (
        id INTEGER PRIMARY KEY,
        email TEXT NOT NULL UNIQUE,
        firstName TEXT NOT NULL,
        lastName TEXT NOT NULL,
        department TEXT,
        role TEXT NOT NULL,
        enabled INTEGER NOT NULL,
        cachedAt TEXT NOT NULL
      )
    ''');

    // Create sectors table
    await db.execute('''
      CREATE TABLE sectors (
        id INTEGER PRIMARY KEY,
        name TEXT NOT NULL,
        code TEXT,
        cachedAt TEXT NOT NULL
      )
    ''');

    // Create sync queue table for offline actions
    await db.execute('''
      CREATE TABLE sync_queue (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        action TEXT NOT NULL,
        endpoint TEXT NOT NULL,
        data TEXT NOT NULL,
        createdAt TEXT NOT NULL
      )
    ''');
  }

  // Internships CRUD
  Future<int> insertInternship(Internship internship) async {
    final db = await database;
    return await db.insert('internships', {
      'id': internship.id,
      'title': internship.title,
      'description': internship.description,
      'companyName': internship.companyName,
      'companyAddress': internship.companyAddress,
      'startDate': internship.startDate.toIso8601String(),
      'endDate': internship.endDate.toIso8601String(),
      'status': internship.status.name,
      'studentId': internship.student.id,
      'instructorId': internship.instructor?.id,
      'sectorId': internship.sector.id,
      'createdAt': internship.createdAt.toIso8601String(),
      'studentData': jsonEncode(internship.student.toJson()),
      'instructorData': internship.instructor != null
          ? jsonEncode(internship.instructor!.toJson())
          : null,
      'sectorData': jsonEncode(internship.sector.toJson()),
      'isSynced': 1,
    }, conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<Internship>> getInternships() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query('internships');

    return List.generate(maps.length, (i) {
      final map = maps[i];
      return Internship(
        id: map['id'],
        title: map['title'],
        description: map['description'],
        companyName: map['companyName'],
        companyAddress: map['companyAddress'],
        startDate: DateTime.parse(map['startDate']),
        endDate: DateTime.parse(map['endDate']),
        status: InternshipStatus.values.firstWhere(
          (e) => e.name == map['status'],
        ),
        student: User.fromJson(jsonDecode(map['studentData'])),
        instructor: map['instructorData'] != null
            ? User.fromJson(jsonDecode(map['instructorData']))
            : null,
        sector: Sector.fromJson(jsonDecode(map['sectorData'])),
        createdAt: DateTime.parse(map['createdAt']),
      );
    });
  }

  Future<int> updateInternship(Internship internship) async {
    final db = await database;
    return await db.update(
      'internships',
      {
        'title': internship.title,
        'description': internship.description,
        'companyName': internship.companyName,
        'companyAddress': internship.companyAddress,
        'startDate': internship.startDate.toIso8601String(),
        'endDate': internship.endDate.toIso8601String(),
        'status': internship.status.name,
        'studentId': internship.student.id,
        'instructorId': internship.instructor?.id,
        'sectorId': internship.sector.id,
        'studentData': jsonEncode(internship.student.toJson()),
        'instructorData': internship.instructor != null
            ? jsonEncode(internship.instructor!.toJson())
            : null,
        'sectorData': jsonEncode(internship.sector.toJson()),
      },
      where: 'id = ?',
      whereArgs: [internship.id],
    );
  }

  Future<int> deleteInternship(int id) async {
    final db = await database;
    return await db.delete('internships', where: 'id = ?', whereArgs: [id]);
  }

  Future<void> clearInternships() async {
    final db = await database;
    await db.delete('internships');
  }

  // Users CRUD
  Future<int> insertUser(User user) async {
    final db = await database;
    return await db.insert('users', {
      'id': user.id,
      'email': user.email,
      'firstName': user.firstName,
      'lastName': user.lastName,
      'department': user.department,
      'role': user.role.name,
      'enabled': user.enabled ? 1 : 0,
      'cachedAt': DateTime.now().toIso8601String(),
    }, conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<User>> getUsers() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query('users');

    return List.generate(maps.length, (i) {
      final map = maps[i];
      return User(
        id: map['id'],
        email: map['email'],
        firstName: map['firstName'],
        lastName: map['lastName'],
        department: map['department'],
        role: Role.values.firstWhere((e) => e.name == map['role']),
        enabled: map['enabled'] == 1,
      );
    });
  }

  Future<User?> getUserById(int id) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'users',
      where: 'id = ?',
      whereArgs: [id],
    );

    if (maps.isEmpty) return null;

    final map = maps.first;
    return User(
      id: map['id'],
      email: map['email'],
      firstName: map['firstName'],
      lastName: map['lastName'],
      department: map['department'],
      role: Role.values.firstWhere((e) => e.name == map['role']),
      enabled: map['enabled'] == 1,
    );
  }

  // Sectors CRUD
  Future<int> insertSector(Sector sector) async {
    final db = await database;
    return await db.insert('sectors', {
      'id': sector.id,
      'name': sector.name,
      'code': sector.code,
      'cachedAt': DateTime.now().toIso8601String(),
    }, conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<Sector>> getSectors() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query('sectors');

    return List.generate(maps.length, (i) {
      final map = maps[i];
      return Sector(id: map['id'], name: map['name'], code: map['code']);
    });
  }

  Future<void> clearSectors() async {
    final db = await database;
    await db.delete('sectors');
  }

  // Sync Queue
  Future<int> addToSyncQueue(
    String action,
    String endpoint,
    Map<String, dynamic> data,
  ) async {
    final db = await database;
    return await db.insert('sync_queue', {
      'action': action,
      'endpoint': endpoint,
      'data': jsonEncode(data),
      'createdAt': DateTime.now().toIso8601String(),
    });
  }

  Future<List<Map<String, dynamic>>> getSyncQueue() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'sync_queue',
      orderBy: 'createdAt ASC',
    );

    return maps.map((map) {
      return {
        'id': map['id'],
        'action': map['action'],
        'endpoint': map['endpoint'],
        'data': jsonDecode(map['data']),
        'createdAt': map['createdAt'],
      };
    }).toList();
  }

  Future<int> removeSyncQueueItem(int id) async {
    final db = await database;
    return await db.delete('sync_queue', where: 'id = ?', whereArgs: [id]);
  }

  Future<void> clearSyncQueue() async {
    final db = await database;
    await db.delete('sync_queue');
  }

  // Sync status
  Future<DateTime?> getLastSyncTime() async {
    final db = await database;
    final result = await db.rawQuery(
      'SELECT MAX(cachedAt) as lastSync FROM (SELECT cachedAt FROM users UNION ALL SELECT cachedAt FROM sectors)',
    );

    if (result.isNotEmpty && result.first['lastSync'] != null) {
      return DateTime.parse(result.first['lastSync'] as String);
    }
    return null;
  }

  // Clear all data
  Future<void> clearAllData() async {
    final db = await database;
    await db.delete('internships');
    await db.delete('users');
    await db.delete('sectors');
    await db.delete('sync_queue');
  }

  // Close database
  Future<void> close() async {
    final db = await database;
    await db.close();
  }
}
