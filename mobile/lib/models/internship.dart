import 'user.dart';

enum InternshipStatus { DRAFT, PENDING_VALIDATION, VALIDATED, REFUSED }

extension InternshipStatusExtension on InternshipStatus {
  String get value {
    switch (this) {
      case InternshipStatus.DRAFT:
        return 'DRAFT';
      case InternshipStatus.PENDING_VALIDATION:
        return 'PENDING_VALIDATION';
      case InternshipStatus.VALIDATED:
        return 'VALIDATED';
      case InternshipStatus.REFUSED:
        return 'REFUSED';
    }
  }

  String get displayName {
    switch (this) {
      case InternshipStatus.DRAFT:
        return 'Draft';
      case InternshipStatus.PENDING_VALIDATION:
        return 'Pending Validation';
      case InternshipStatus.VALIDATED:
        return 'Validated';
      case InternshipStatus.REFUSED:
        return 'Refused';
    }
  }
}

class Sector {
  final int id;
  final String name;
  final String code;

  Sector({required this.id, required this.name, required this.code});

  factory Sector.fromJson(Map<String, dynamic> json) {
    return Sector(
      id: json['id'],
      name: json['name'],
      code: json['code'] ?? '', // Make code optional with default
    );
  }

  Map<String, dynamic> toJson() {
    return {'id': id, 'name': name, 'code': code};
  }
}

class Internship {
  final int id;
  final String title;
  final String? description;
  final String companyName;
  final String? companyAddress;
  final DateTime startDate;
  final DateTime endDate;
  final InternshipStatus status;
  final User student;
  final User? instructor;
  final Sector sector;
  final String? refusalComment;
  final DateTime createdAt;

  Internship({
    required this.id,
    required this.title,
    this.description,
    required this.companyName,
    this.companyAddress,
    required this.startDate,
    required this.endDate,
    required this.status,
    required this.student,
    this.instructor,
    required this.sector,
    this.refusalComment,
    required this.createdAt,
  });

  factory Internship.fromJson(Map<String, dynamic> json) {
    // Backend returns flattened data, need to construct nested objects
    final studentJson = {
      'id': json['studentId'],
      'email': json['studentEmail'] ?? '',
      'firstName': json['studentName']?.split(' ').first ?? '',
      'lastName': json['studentName']?.split(' ').skip(1).join(' ') ?? '',
      'role': 'STUDENT',
      'enabled': true,
    };

    Map<String, dynamic>? instructorJson;
    if (json['instructorId'] != null) {
      instructorJson = {
        'id': json['instructorId'],
        'email': '', // Not provided in response
        'firstName': json['instructorName']?.split(' ').first ?? '',
        'lastName': json['instructorName']?.split(' ').skip(1).join(' ') ?? '',
        'role': 'INSTRUCTOR',
        'enabled': true,
      };
    }

    final sectorJson = {
      'id': json['sectorId'],
      'name': json['sectorName'] ?? '',
      // code field will default to '' in Sector.fromJson
    };

    return Internship(
      id: json['id'],
      title: json['title'],
      description: json['description'],
      companyName: json['companyName'],
      companyAddress: json['companyAddress'],
      startDate: DateTime.parse(json['startDate']),
      endDate: DateTime.parse(json['endDate']),
      status: InternshipStatus.values.firstWhere(
        (e) => e.name == json['status'],
      ),
      student: User.fromJson(studentJson),
      instructor: instructorJson != null ? User.fromJson(instructorJson) : null,
      sector: Sector.fromJson(sectorJson),
      refusalComment: json['refusalComment'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }

  String get statusText {
    switch (status) {
      case InternshipStatus.DRAFT:
        return 'Draft';
      case InternshipStatus.PENDING_VALIDATION:
        return 'Pending Validation';
      case InternshipStatus.VALIDATED:
        return 'Validated';
      case InternshipStatus.REFUSED:
        return 'Refused';
    }
  }

  // Computed getters for UI
  String get sectorName => sector.name;

  int get durationInDays => endDate.difference(startDate).inDays;

  String get studentName => student.fullName;

  String get studentEmail => student.email;

  String? get instructorName => instructor?.fullName;

  bool get hasReport =>
      false; // This should be updated based on actual backend response

  String get company => companyName;

  String get location => companyAddress ?? 'Not specified';
}

class CreateInternshipRequest {
  final String title;
  final String? description;
  final String companyName;
  final String? companyAddress;
  final DateTime startDate;
  final DateTime endDate;
  final int sectorId;

  CreateInternshipRequest({
    required this.title,
    this.description,
    required this.companyName,
    this.companyAddress,
    required this.startDate,
    required this.endDate,
    required this.sectorId,
  });

  Map<String, dynamic> toJson() {
    return {
      'title': title,
      'description': description,
      'companyName': companyName,
      'companyAddress': companyAddress,
      'startDate': startDate.toIso8601String().split('T')[0],
      'endDate': endDate.toIso8601String().split('T')[0],
      'sectorId': sectorId,
    };
  }
}

// Alias for backward compatibility
typedef InternshipRequest = CreateInternshipRequest;
