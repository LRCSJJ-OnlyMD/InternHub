# InternHub Mobile Application - Complete Rebuild Guide

## Project Overview
- **Technology**: Flutter 3.38.5 / Dart 3.10.4
- **State Management**: Riverpod
- **HTTP Client**: Dio
- **Routing**: GoRouter
- **Local Storage**: Flutter Secure Storage
- **Backend API**: http://localhost:8080/api
- **Target Platform**: Android Emulator (Primary), iOS Simulator (Secondary)

---

## Phase 1: Backend Analysis & Data Models

### Core Models (from Backend)

#### 1. User Model
```dart
enum Role { STUDENT, INSTRUCTOR, ADMIN }

class User {
  final int id;
  final String email;
  final String firstName;
  final String lastName;
  final String? department;
  final Role role;
  final bool enabled;
  final bool twoFactorEnabled;
  final bool accountActivated;
  final DateTime createdAt;
  final List<Sector>? sectors; // For instructors only
}
```

#### 2. Internship Model
```dart
enum InternshipStatus { DRAFT, PENDING_VALIDATION, VALIDATED, REFUSED }

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
  final DateTime? submittedAt;
  final DateTime? validatedAt;
  final DateTime? refusedAt;
}
```

#### 3. Sector Model
```dart
class Sector {
  final int id;
  final String name;
  final String code;
  final String? description;
}
```

#### 4. Document Model
```dart
enum DocumentType { RAPPORT }

class Document {
  final int id;
  final String fileName;
  final String filePath;
  final DocumentType documentType;
  final int version;
  final int internshipId;
  final DateTime uploadedAt;
  final int fileSize;
}
```

#### 5. Comment Model
```dart
class Comment {
  final int id;
  final String content;
  final int internshipId;
  final User author;
  final DateTime createdAt;
  final DateTime? updatedAt;
}
```

#### 6. Notification Model
```dart
enum NotificationType {
  INTERNSHIP_SUBMITTED,
  INTERNSHIP_VALIDATED,
  INTERNSHIP_REFUSED,
  COMMENT_ADDED,
  DOCUMENT_UPLOADED
}

class Notification {
  final int id;
  final String title;
  final String message;
  final NotificationType type;
  final bool isRead;
  final int userId;
  final DateTime createdAt;
  final Map<String, dynamic>? metadata;
}
```

---

## Phase 2: API Endpoints Structure

### Authentication Endpoints (`/api/auth`)
- POST `/login` - Login with email/password
- POST `/register` - Register new student
- POST `/password-reset/request` - Request password reset
- POST `/password-reset/confirm` - Confirm password reset with token
- GET `/verify-email` - Verify email with token
- POST `/2fa/enable` - Enable 2FA
- POST `/2fa/disable` - Disable 2FA
- POST `/2fa/verify` - Verify 2FA code
- PUT `/profile` - Update user profile
- PUT `/password` - Change password

### Student Endpoints (`/api/student/internships`)
- GET `/` - Get all student's internships
- GET `/{id}` - Get specific internship
- POST `/` - Create new internship
- PUT `/{id}` - Update internship (DRAFT only)
- POST `/{id}/submit` - Submit internship for validation
- DELETE `/{id}` - Delete internship (DRAFT only)

### Instructor Endpoints (`/api/instructor/internships`)
- GET `/` - Get all assigned internships
- GET `/{id}` - Get internship details
- POST `/{id}/validate` - Validate internship
- POST `/{id}/refuse` - Refuse internship with comment

### Admin Endpoints (`/api/admin`)
- GET `/internships` - Get all internships
- GET `/internships/{id}` - Get specific internship
- DELETE `/internships/{id}` - Delete any internship
- PUT `/internships/{id}/reassign/{instructorId}` - Reassign instructor
- GET `/stats/by-status` - Statistics by status
- GET `/stats/by-sector` - Statistics by sector
- GET `/users` - Get all users
- POST `/users` - Create user
- PUT `/users/{id}` - Update user
- DELETE `/users/{id}` - Delete user
- GET `/sectors` - Get all sectors
- POST `/sectors` - Create sector
- PUT `/sectors/{id}` - Update sector
- DELETE `/sectors/{id}` - Delete sector

### Utility Endpoints (`/api/utility`)
- GET `/sectors` - Get all sectors (public)

### Document Endpoints (`/api/documents`)
- POST `/upload` - Upload document
- POST `/upload-version` - Upload new version
- GET `/internship/{internshipId}` - Get all documents
- GET `/internship/{internshipId}/latest` - Get latest document
- GET `/{documentId}/download` - Download document
- DELETE `/{documentId}` - Delete document

### Comment Endpoints (`/api/internships/{internshipId}/comments`)
- POST `/` - Add comment
- GET `/` - Get all comments
- PUT `/{commentId}` - Update comment
- DELETE `/{commentId}` - Delete comment

### Notification Endpoints (`/api/notifications`)
- GET `/` - Get all notifications
- GET `/unread` - Get unread notifications
- GET `/unread/count` - Get unread count
- PUT `/{id}/read` - Mark as read
- PUT `/mark-all-read` - Mark all as read
- DELETE `/{id}` - Delete notification

### Statistics Endpoints (`/api/statistics`)
- GET `/by-status` - Stats by status
- GET `/by-sector` - Stats by sector
- GET `/enhanced` - Enhanced statistics
- GET `/instructor/{instructorId}` - Instructor statistics
- GET `/student/{studentId}` - Student statistics

---

## Phase 3: Folder Structure

```
mobile/
├── lib/
│   ├── main.dart                          # App entry point
│   ├── models/                            # Data models
│   │   ├── user.dart
│   │   ├── internship.dart
│   │   ├── sector.dart
│   │   ├── document.dart
│   │   ├── comment.dart
│   │   ├── notification.dart
│   │   └── statistics.dart
│   ├── services/                          # API services
│   │   ├── api_service.dart               # Base HTTP service
│   │   ├── auth_service.dart              # Authentication
│   │   ├── internship_service.dart        # Internship CRUD
│   │   ├── document_service.dart          # Document operations
│   │   ├── comment_service.dart           # Comments
│   │   ├── notification_service.dart      # Notifications
│   │   └── statistics_service.dart        # Statistics
│   ├── providers/                         # Riverpod providers
│   │   ├── auth_provider.dart             # Auth state
│   │   ├── internship_provider.dart       # Internship state
│   │   ├── notification_provider.dart     # Notification state
│   │   └── theme_provider.dart            # Theme state
│   ├── screens/                           # UI screens
│   │   ├── auth/
│   │   │   ├── login_screen.dart
│   │   │   ├── register_screen.dart
│   │   │   ├── forgot_password_screen.dart
│   │   │   └── verify_email_screen.dart
│   │   ├── student/
│   │   │   ├── student_dashboard_screen.dart
│   │   │   ├── internship_list_screen.dart
│   │   │   ├── internship_form_screen.dart
│   │   │   └── internship_detail_screen.dart
│   │   ├── instructor/
│   │   │   ├── instructor_dashboard_screen.dart
│   │   │   ├── assigned_internships_screen.dart
│   │   │   └── validate_internship_screen.dart
│   │   ├── admin/
│   │   │   ├── admin_dashboard_screen.dart
│   │   │   ├── manage_users_screen.dart
│   │   │   ├── manage_sectors_screen.dart
│   │   │   └── statistics_screen.dart
│   │   └── common/
│   │       ├── profile_screen.dart
│   │       └── notifications_screen.dart
│   ├── widgets/                           # Reusable widgets
│   │   ├── custom_button.dart
│   │   ├── custom_text_field.dart
│   │   ├── internship_card.dart
│   │   ├── status_badge.dart
│   │   └── loading_indicator.dart
│   └── utils/                             # Utilities
│       ├── constants.dart                 # API endpoints & constants
│       ├── theme.dart                     # Theme configuration
│       ├── validators.dart                # Form validators
│       └── helpers.dart                   # Helper functions
├── android/                               # Android configuration
├── ios/                                   # iOS configuration
├── assets/                                # Images, fonts, etc.
│   └── images/
├── pubspec.yaml                           # Dependencies
└── README.md                              # Documentation
```

---

## Phase 4: Dependencies (pubspec.yaml)

```yaml
dependencies:
  flutter:
    sdk: flutter
  
  # State Management
  flutter_riverpod: ^2.6.1
  
  # HTTP & API
  dio: ^5.4.0
  
  # Local Storage
  flutter_secure_storage: ^9.2.4
  shared_preferences: ^2.5.3
  
  # Routing
  go_router: ^12.1.3
  
  # UI
  cupertino_icons: ^1.0.6
  
  # Forms & Validation
  form_field_validator: ^1.1.0
  intl: ^0.20.2
  
  # Utilities
  timeago: ^3.6.0
```

---

## Phase 5: Implementation Steps

### Step 1: Setup Android Emulator
1. Install Android Studio
2. Open Android Studio > Tools > AVD Manager
3. Create Virtual Device > Phone > Pixel 4 (or any modern device)
4. Download System Image (Android 13 or later)
5. Finish and Launch Emulator

### Step 2: Create Flutter Project
```bash
flutter create mobile
cd mobile
flutter pub get
```

### Step 3: Configure API Constants
- Set base URL: `http://10.0.2.2:8080/api` (for Android Emulator)
- Note: `10.0.2.2` is the special IP for localhost from Android Emulator

### Step 4: Implement Core Services
1. **ApiService**: Base HTTP client with interceptors
2. **AuthService**: Login, register, token management
3. **InternshipService**: CRUD operations
4. **DocumentService**: File upload/download
5. **NotificationService**: Real-time notifications

### Step 5: Create Data Models
- Implement all models with `fromJson` and `toJson` methods
- Add enum serialization
- Handle null safety properly

### Step 6: Setup Riverpod Providers
- AuthProvider for user session
- InternshipProvider for internship state
- NotificationProvider for notifications
- ThemeProvider for dark/light mode

### Step 7: Build UI Screens
1. **Authentication Flow**:
   - Login Screen
   - Register Screen
   - Email Verification

2. **Student Flow**:
   - Dashboard with statistics
   - List internships with filters
   - Create/Edit internship form
   - View internship details
   - Upload documents

3. **Instructor Flow**:
   - Dashboard with assigned internships
   - Validate/Refuse internships
   - Add comments

4. **Admin Flow**:
   - Overview statistics
   - Manage users
   - Manage sectors
   - View all internships

### Step 8: Implement Navigation
- Setup GoRouter with role-based routing
- Add route guards for authentication
- Handle deep linking

### Step 9: Add Error Handling
- Global error handling
- Network error recovery
- Form validation errors
- API error messages

### Step 10: Testing
- Test all API endpoints
- Test authentication flow
- Test CRUD operations
- Test file upload/download
- Test on Android Emulator

---

## Phase 6: Critical Implementation Notes

### Authentication
- Store JWT token in FlutterSecureStorage
- Add token to all authenticated requests
- Handle token expiration
- Implement auto-logout on 401

### File Upload
- Use multipart/form-data
- Show upload progress
- Validate file types (PDF only)
- Validate file size (max 10MB)

### State Management
- Use AsyncValue for loading states
- Handle errors gracefully
- Show loading indicators
- Cache data when appropriate

### UI/UX
- Material Design 3
- Consistent color scheme (Blue primary)
- Status badges for internships
- Loading states
- Error states
- Empty states

### Performance
- Lazy load lists
- Paginate API calls
- Cache images
- Minimize rebuilds
- Optimize API calls

---

## Phase 7: Android Emulator Configuration

### Network Configuration
- Backend URL: `http://10.0.2.2:8080/api`
- Port 10.0.2.2 maps to host machine's localhost
- Ensure Docker containers expose ports properly

### Permissions (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

---

## Phase 8: Testing Checklist

### Authentication
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Register new student
- [ ] Verify email
- [ ] Reset password
- [ ] Change password
- [ ] Enable/Disable 2FA

### Student Features
- [ ] View dashboard
- [ ] List all internships
- [ ] Create new internship
- [ ] Edit draft internship
- [ ] Delete draft internship
- [ ] Submit internship for validation
- [ ] View internship details
- [ ] Upload document
- [ ] Download document
- [ ] Add comment
- [ ] View notifications

### Instructor Features
- [ ] View assigned internships
- [ ] Validate internship
- [ ] Refuse internship with comment
- [ ] Add comments
- [ ] View statistics

### Admin Features
- [ ] View all internships
- [ ] Manage users (CRUD)
- [ ] Manage sectors (CRUD)
- [ ] View statistics
- [ ] Reassign instructor

---

## Success Criteria

1. ✅ App runs on Android Emulator without errors
2. ✅ All API endpoints are functional
3. ✅ Authentication flow works correctly
4. ✅ Students can manage internships
5. ✅ Instructors can validate/refuse internships
6. ✅ Admin can manage all entities
7. ✅ File upload/download works
8. ✅ Notifications are received
9. ✅ UI is responsive and intuitive
10. ✅ No white screens or crashes

---

## Troubleshooting Guide

### White Screen Issues
- Check Flutter console for errors
- Verify all imports are correct
- Ensure models have proper constructors
- Check for null safety issues
- Verify API endpoints are accessible

### Network Issues
- Use `10.0.2.2` instead of `localhost` for Android
- Check Docker containers are running
- Verify CORS configuration includes emulator IP
- Test API with Postman first

### Build Issues
- Run `flutter clean`
- Run `flutter pub get`
- Check for dependency conflicts
- Verify Flutter and Dart versions

---

**END OF GUIDE**
