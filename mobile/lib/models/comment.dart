class Comment {
  final int id;
  final String content;
  final DateTime createdAt;
  final DateTime? updatedAt;
  final CommentAuthor author;
  final int internshipId;

  Comment({
    required this.id,
    required this.content,
    required this.createdAt,
    this.updatedAt,
    required this.author,
    required this.internshipId,
  });

  factory Comment.fromJson(Map<String, dynamic> json) {
    // Backend returns flattened user data, need to construct author object
    final authorJson = {
      'id': json['userId'],
      'fullName': '${json['userFirstName'] ?? ''} ${json['userLastName'] ?? ''}'
          .trim(),
      'email': '', // Not provided in response
      'role': json['userRole'] ?? 'STUDENT',
    };

    return Comment(
      id: json['id'],
      content: json['content'],
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: json['updatedAt'] != null
          ? DateTime.parse(json['updatedAt'])
          : null,
      author: CommentAuthor.fromJson(authorJson),
      internshipId: json['internshipId'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'content': content,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
      'author': author.toJson(),
      'internshipId': internshipId,
    };
  }

  bool get isEdited => updatedAt != null;
}

class CommentAuthor {
  final int id;
  final String fullName;
  final String email;
  final String role;

  CommentAuthor({
    required this.id,
    required this.fullName,
    required this.email,
    required this.role,
  });

  factory CommentAuthor.fromJson(Map<String, dynamic> json) {
    return CommentAuthor(
      id: json['id'],
      fullName: json['fullName'],
      email: json['email'],
      role: json['role'],
    );
  }

  Map<String, dynamic> toJson() {
    return {'id': id, 'fullName': fullName, 'email': email, 'role': role};
  }
}

class CreateCommentRequest {
  final String content;
  final int internshipId;

  CreateCommentRequest({required this.content, required this.internshipId});

  Map<String, dynamic> toJson() {
    return {'content': content, 'internshipId': internshipId};
  }
}

class UpdateCommentRequest {
  final String content;

  UpdateCommentRequest({required this.content});

  Map<String, dynamic> toJson() {
    return {'content': content};
  }
}
