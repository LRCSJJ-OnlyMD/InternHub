enum Role { STUDENT, INSTRUCTOR, ADMIN }

class User {
  final int id;
  final String email;
  final String firstName;
  final String lastName;
  final String? department;
  final Role role;
  final bool enabled;

  User({
    required this.id,
    required this.email,
    required this.firstName,
    required this.lastName,
    this.department,
    required this.role,
    required this.enabled,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'],
      email: json['email'],
      firstName: json['firstName'],
      lastName: json['lastName'],
      department: json['department'],
      role: Role.values.firstWhere((e) => e.name == json['role']),
      enabled: json['enabled'] ?? true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'email': email,
      'firstName': firstName,
      'lastName': lastName,
      'department': department,
      'role': role.name,
      'enabled': enabled,
    };
  }

  String get fullName => '$firstName $lastName';
}

class LoginRequest {
  final String email;
  final String password;

  LoginRequest({required this.email, required this.password});

  Map<String, dynamic> toJson() {
    return {'email': email, 'password': password};
  }
}

class AuthResponse {
  final String token;
  final User user;

  AuthResponse({required this.token, required this.user});

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    if (json['token'] == null) {
      throw Exception('Token is missing from login response');
    }

    // Backend returns user data flattened at top level, not nested under 'user' key
    // Build a user object from the top-level fields
    final userJson = {
      'id': json['userId'],
      'email': json['email'],
      'firstName': json['firstName'],
      'lastName': json['lastName'],
      'department': json['department'],
      'role': json['role'],
      'enabled': json['enabled'] ?? true,
    };

    return AuthResponse(
      token: json['token'] as String,
      user: User.fromJson(userJson),
    );
  }
}

class RegisterRequest {
  final String email;
  final String password;
  final String firstName;
  final String lastName;
  final String? department;

  RegisterRequest({
    required this.email,
    required this.password,
    required this.firstName,
    required this.lastName,
    this.department,
  });

  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'password': password,
      'firstName': firstName,
      'lastName': lastName,
      if (department != null) 'department': department,
    };
  }
}
