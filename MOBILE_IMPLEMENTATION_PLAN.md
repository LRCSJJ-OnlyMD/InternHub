# InternHub Mobile App - Complete Implementation Plan

## Current Status (December 16, 2025)

### ✅ Completed
- Project scaffolding with Flutter 3.38.5 and Dart 3.10.4
- Core dependencies installed (Riverpod, Dio, GoRouter, etc.)
- Base services layer (api_service, auth_service, internship_service)
- State management providers (auth_provider, internship_provider)
- Login screen and basic student dashboard
- Models: User, Internship, Sector
- Android emulator setup (Medium_Phone_API_36.1)
- Backend API ready at http://10.0.2.2:8080/api

### ⚠️ Current Issues
- **201 compilation errors** in Flutter mobile app (must fix before continuing)
- Missing models and services that screens depend on
- Inconsistent enum names (DRAFT vs draft, VALIDATED vs validated)
- Missing dependencies (file_picker)
- Incomplete providers and services

---

## 📋 Implementation Roadmap

### **CRITICAL PRIORITY** (Must complete first, no errors allowed)

These features are essential for basic app functionality. Each file must be 100% error-free before moving to the next.

#### Phase 1: Fix All Compilation Errors (CURRENT)
**Estimated Time: 2-4 hours**

**Step 1.1: Fix Internship Model**
- [ ] Add missing getters to `Internship` class:
  - `sectorName` → return `sector?.name ?? 'Unknown'`
  - `durationInDays` → calculate from startDate and endDate
  - `studentName` → return `student?.fullName ?? 'Unknown'`
  - `studentEmail` → return `student?.email ?? ''`
  - `instructorName` → return `instructor?.fullName`
  - `hasReport` → return `bool` (add to backend response)
- [ ] Add extension methods to `InternshipStatus` enum:
  - `value` → return String representation
  - `displayName` → return user-friendly name
  - `statusText` → return formatted text

**Step 1.2: Fix Missing Dependencies**
- [ ] Add `file_picker: ^8.0.0` to pubspec.yaml
- [ ] Run `flutter pub get`

**Step 1.3: Fix InternshipService**
- [ ] Add `getInternshipById(int id)` method
- [ ] Add `uploadReport(int id, File file)` method
- [ ] Add `downloadReport(int id, String savePath)` method

**Step 1.4: Fix Providers**
- [ ] Create `currentUserProvider` in auth_provider
- [ ] Create `studentInternshipsProvider` in internship_provider
- [ ] Fix `sectorsProvider` naming conflict (use import prefixes)

**Step 1.5: Fix InternshipRequest Model**
- [ ] Rename `CreateInternshipRequest` to `InternshipRequest` OR
- [ ] Update all usages to `CreateInternshipRequest`

**Step 1.6: Fix AuthService**
- [ ] Add `changePassword(String oldPassword, String newPassword)` method

**Step 1.7: Create AppTheme Utility**
- [ ] Create `lib/utils/app_theme.dart`
- [ ] Add `getStatusColor(String status)` method
- [ ] Define color constants for each status

**Step 1.8: Verify Zero Errors**
- [ ] Run `flutter analyze`
- [ ] Fix any remaining errors
- [ ] Confirm: **0 errors, 0 warnings**

---

#### Phase 2: Complete Authentication Flow
**Estimated Time: 3-4 hours**

**Priority: CRITICAL** - Users cannot use app without authentication

**Step 2.1: Register Screen** (COMPLETE)
- [ ] Email/password fields with validation
- [ ] Department and role selection
- [ ] Registration API integration
- [ ] Navigate to email verification screen
- [ ] Error handling and loading states
- [ ] **Test: Successfully create account**

**Step 2.2: Email Verification Screen** (COMPLETE)
- [ ] Display verification instructions
- [ ] Resend verification email button
- [ ] Check verification status API
- [ ] Auto-redirect to login after verification
- [ ] **Test: Verify email successfully**

**Step 2.3: Forgot Password Screen** (COMPLETE)
- [ ] Email input field
- [ ] Request reset code API integration
- [ ] Navigate to reset password screen
- [ ] Success/error messages
- [ ] **Test: Receive reset code**

**Step 2.4: Reset Password Screen** (COMPLETE)
- [ ] Reset code input
- [ ] New password with confirmation
- [ ] Reset password API integration
- [ ] Navigate to login on success
- [ ] **Test: Reset password successfully**

**Step 2.5: Two-Factor Authentication Screen** (if backend supports it)
- [ ] 2FA code input
- [ ] Verify 2FA code API
- [ ] Remember device option
- [ ] Resend code functionality
- [ ] **Test: Complete 2FA login**

**Step 2.6: Integration Testing**
- [ ] Test complete registration → verification → login flow
- [ ] Test forgot password → reset → login flow
- [ ] Test login with wrong credentials
- [ ] Test token expiration and refresh
- [ ] **Confirm: All auth flows work end-to-end**

---

#### Phase 3: Complete Student Internship Management
**Estimated Time: 5-6 hours**

**Priority: CRITICAL** - Core feature of the application

**Step 3.1: Internships List Screen** (VERIFY)
- [ ] Display list of student's internships
- [ ] Filter by status (All, Draft, Pending, Validated, Refused)
- [ ] Search by title/company
- [ ] Sort options (date, title, status)
- [ ] Pull-to-refresh
- [ ] Empty state message
- [ ] Tap internship → navigate to details
- [ ] **Test: View and filter internships**

**Step 3.2: Create Internship Screen** (FIX)
- [ ] Form fields: title, description, company, address, dates, sector
- [ ] Date pickers for start/end dates
- [ ] Sector dropdown from API
- [ ] Form validation
- [ ] Save as draft button
- [ ] Submit button
- [ ] **Test: Create draft and submitted internship**

**Step 3.3: Edit Internship Screen** (NEW)
- [ ] Pre-populate form with existing data
- [ ] Same fields as create screen
- [ ] Update API integration
- [ ] Only allow edit if status = DRAFT
- [ ] **Test: Edit and save draft internship**

**Step 3.4: Internship Detail Screen** (FIX)
- [ ] Display all internship information
- [ ] Status badge with color coding
- [ ] Student/instructor information
- [ ] Document section (report upload/download)
- [ ] Comments section integration
- [ ] Action buttons based on status:
  - DRAFT: Edit, Delete, Submit
  - PENDING_VALIDATION: View only
  - VALIDATED: Upload report, View report
  - REFUSED: View refusal reason
- [ ] **Test: View details, perform actions**

**Step 3.5: Document Upload/Download**
- [ ] File picker integration
- [ ] Upload progress indicator
- [ ] Download report functionality
- [ ] View uploaded documents list
- [ ] Delete document (if allowed)
- [ ] **Test: Upload PDF, download, view**

**Step 3.6: Submit Internship for Validation**
- [ ] Confirmation dialog
- [ ] Submit API call
- [ ] Update status to PENDING_VALIDATION
- [ ] Show success message
- [ ] Disable further editing
- [ ] **Test: Submit internship successfully**

**Step 3.7: Delete Internship**
- [ ] Confirmation dialog
- [ ] Delete API call (only if DRAFT status)
- [ ] Remove from list
- [ ] Navigate back to list
- [ ] **Test: Delete draft internship**

**Step 3.8: Integration Testing**
- [ ] Create → Save Draft → Edit → Submit workflow
- [ ] Create → Submit directly workflow
- [ ] Submit → Instructor validates → Upload report workflow
- [ ] Submit → Instructor refuses → View reason workflow
- [ ] **Confirm: All student workflows functional**

---

### **HIGH PRIORITY** (Important features, implement after critical)

#### Phase 4: Instructor Features
**Estimated Time: 4-5 hours**

**Priority: HIGH** - Instructors need to validate internships

**Step 4.1: Instructor Dashboard**
- [ ] Statistics cards (assigned, pending, validated, refused)
- [ ] Pending validation list (priority view)
- [ ] Recently validated internships
- [ ] Students supervised count
- [ ] **Test: View instructor statistics**

**Step 4.2: Assigned Internships List**
- [ ] Display all internships assigned to instructor
- [ ] Filter by status
- [ ] Search by student name/company
- [ ] Tap to view details
- [ ] **Test: View assigned internships**

**Step 4.3: Validate Internship**
- [ ] Validation dialog
- [ ] Optional comment field
- [ ] Validate API call
- [ ] Update status to VALIDATED
- [ ] Send notification to student
- [ ] **Test: Validate internship successfully**

**Step 4.4: Refuse Internship**
- [ ] Refusal dialog
- [ ] **Required** refusal comment/reason
- [ ] Refuse API call
- [ ] Update status to REFUSED
- [ ] Send notification to student with reason
- [ ] **Test: Refuse internship with reason**

**Step 4.5: Bulk Operations**
- [ ] Select multiple internships
- [ ] Bulk validate
- [ ] Bulk refuse (with common reason)
- [ ] Progress indicator
- [ ] Summary of results
- [ ] **Test: Bulk validate 5 internships**

**Step 4.6: Integration Testing**
- [ ] Student submits → Instructor receives → Validates workflow
- [ ] Student submits → Instructor refuses → Student views reason
- [ ] Bulk validate multiple internships
- [ ] **Confirm: Instructor workflows functional**

---

#### Phase 5: Comments and Communication
**Estimated Time: 3-4 hours**

**Priority: HIGH** - Important for collaboration

**Step 5.1: Comments Section Widget**
- [ ] Display list of comments on internship
- [ ] Show commenter name, timestamp, content
- [ ] Newest first ordering
- [ ] Load more pagination
- [ ] Empty state message
- [ ] **Test: View comments**

**Step 5.2: Add Comment**
- [ ] Text input field
- [ ] Character count (max 500)
- [ ] Post comment button
- [ ] Add comment API
- [ ] Refresh comments list
- [ ] Loading and error states
- [ ] **Test: Add comment successfully**

**Step 5.3: Edit Comment**
- [ ] Edit icon on own comments
- [ ] Pre-populate text field
- [ ] Update comment API
- [ ] Update UI
- [ ] **Test: Edit own comment**

**Step 5.4: Delete Comment**
- [ ] Delete icon on own comments
- [ ] Confirmation dialog
- [ ] Delete API call
- [ ] Remove from UI
- [ ] **Test: Delete own comment**

**Step 5.5: Real-time Updates** (Optional)
- [ ] WebSocket or polling for new comments
- [ ] Show new comment indicator
- [ ] Auto-refresh comments
- [ ] **Test: See new comments in real-time**

---

#### Phase 6: Notifications System
**Estimated Time: 3-4 hours**

**Priority: HIGH** - Keep users informed

**Step 6.1: Notifications Bell Icon**
- [ ] Bell icon in app bar
- [ ] Unread count badge
- [ ] Tap to open notifications screen
- [ ] **Test: See unread count**

**Step 6.2: Notifications List Screen**
- [ ] Display all notifications
- [ ] Group by date (Today, Yesterday, Older)
- [ ] Show notification type icon
- [ ] Mark as read on tap
- [ ] Navigate to related internship
- [ ] **Test: View notifications**

**Step 6.3: Mark Notifications as Read**
- [ ] Auto-mark as read when opened
- [ ] Mark all as read button
- [ ] Update API
- [ ] Update unread count
- [ ] **Test: Mark notifications as read**

**Step 6.4: Notification Types**
- [ ] Internship validated notification
- [ ] Internship refused notification
- [ ] New comment notification
- [ ] Document uploaded notification
- [ ] Deadline reminder notification
- [ ] **Test: Receive each type**

**Step 6.5: Notification Preferences**
- [ ] Enable/disable email notifications
- [ ] Enable/disable push notifications (if implemented)
- [ ] Per-notification-type settings
- [ ] Save preferences API
- [ ] **Test: Update preferences**

**Step 6.6: Push Notifications** (Optional, if FCM configured)
- [ ] Configure Firebase Cloud Messaging
- [ ] Request notification permissions
- [ ] Handle FCM tokens
- [ ] Display push notifications
- [ ] Tap notification opens app
- [ ] **Test: Receive push notification**

---

### **MEDIUM PRIORITY** (Useful features, implement after high priority)

#### Phase 7: Admin Features
**Estimated Time: 5-6 hours**

**Priority: MEDIUM** - Admin oversight and management

**Step 7.1: Admin Dashboard**
- [ ] System-wide statistics
- [ ] Total users, internships, sectors
- [ ] Internships by status chart
- [ ] Recent activity feed
- [ ] **Test: View admin dashboard**

**Step 7.2: User Management Screen**
- [ ] List all users
- [ ] Filter by role (Student, Instructor, Admin)
- [ ] Search by name/email
- [ ] View user details
- [ ] **Test: View users list**

**Step 7.3: Create/Edit User**
- [ ] User form (email, name, role, department)
- [ ] Password field (for creation)
- [ ] Enable/disable user
- [ ] Assign role
- [ ] Create/update API
- [ ] **Test: Create instructor user**

**Step 7.4: Delete User**
- [ ] Confirmation dialog
- [ ] Delete API call
- [ ] Remove from list
- [ ] Handle cascade (reassign internships?)
- [ ] **Test: Delete user**

**Step 7.5: Manage Sectors**
- [ ] List all sectors
- [ ] Add new sector
- [ ] Edit sector name/code
- [ ] Delete sector (if no internships)
- [ ] **Test: Manage sectors**

**Step 7.6: Internships Overview** (Admin view)
- [ ] View all internships (all students)
- [ ] Advanced filters
- [ ] Bulk status updates
- [ ] Export to CSV
- [ ] **Test: View and manage internships**

**Step 7.7: Activity Logs**
- [ ] View system activity logs
- [ ] Filter by user, action, date
- [ ] Search logs
- [ ] Export logs
- [ ] **Test: View activity logs**

---

#### Phase 8: Advanced Search and Filtering
**Estimated Time: 3-4 hours**

**Priority: MEDIUM** - Improves user experience

**Step 8.1: Advanced Search Screen**
- [ ] Search by keyword (title, company, description)
- [ ] Filter by status (multiple selection)
- [ ] Filter by sector (dropdown)
- [ ] Filter by date range
- [ ] Filter by student (instructor/admin only)
- [ ] Filter by instructor (admin only)
- [ ] **Test: Search with multiple filters**

**Step 8.2: Sort Options**
- [ ] Sort by creation date (newest/oldest)
- [ ] Sort by start date
- [ ] Sort by end date
- [ ] Sort by title (A-Z)
- [ ] Sort by company name
- [ ] **Test: Sort internships**

**Step 8.3: Save Search Filters**
- [ ] Save current filters as preset
- [ ] Load saved presets
- [ ] Delete presets
- [ ] Store in local storage
- [ ] **Test: Save and load preset**

**Step 8.4: Search Results**
- [ ] Display search results
- [ ] Highlight matching terms
- [ ] Pagination or infinite scroll
- [ ] Empty state if no results
- [ ] **Test: View search results**

---

### **LOW PRIORITY** (Nice-to-have features, implement last)

#### Phase 9: User Profile and Settings
**Estimated Time: 2-3 hours**

**Priority: LOW** - User customization

**Step 9.1: Profile Screen**
- [ ] Display user information
- [ ] Edit name, email, department
- [ ] Change password
- [ ] Update profile photo (optional)
- [ ] **Test: Update profile**

**Step 9.2: Settings Screen**
- [ ] Language selection (English/French)
- [ ] Theme selection (Light/Dark/System)
- [ ] Notification preferences link
- [ ] About app section
- [ ] Logout button
- [ ] **Test: Change settings**

**Step 9.3: Change Password**
- [ ] Current password field
- [ ] New password with confirmation
- [ ] Password strength indicator
- [ ] Update password API
- [ ] **Test: Change password**

---

#### Phase 10: Offline Mode and Sync
**Estimated Time: 4-5 hours**

**Priority: LOW** - Advanced feature

**Step 10.1: Local Database Setup**
- [ ] Add `sqflite` dependency
- [ ] Create database schema
- [ ] Database helper class
- [ ] **Test: Create local database**

**Step 10.2: Cache Data Locally**
- [ ] Cache internships list
- [ ] Cache user profile
- [ ] Cache sectors
- [ ] Auto-sync on app start
- [ ] **Test: View cached data offline**

**Step 10.3: Offline Actions Queue**
- [ ] Queue create/edit actions when offline
- [ ] Store pending changes
- [ ] Sync when online
- [ ] Conflict resolution
- [ ] **Test: Create draft offline, sync online**

**Step 10.4: Sync Status Indicator**
- [ ] Show sync status icon
- [ ] Last synced timestamp
- [ ] Manual sync button
- [ ] Sync progress
- [ ] **Test: Monitor sync status**

---

#### Phase 11: Document Management
**Estimated Time: 3-4 hours**

**Priority: LOW** (Basic document upload already in Phase 3)

**Step 11.1: Document Manager Screen**
- [ ] List all documents for internship
- [ ] Document type icons (PDF, DOCX, etc.)
- [ ] File size and upload date
- [ ] Tap to download/view
- [ ] **Test: View documents list**

**Step 11.2: Upload Multiple Documents**
- [ ] Multi-file picker
- [ ] Upload progress for each file
- [ ] Cancel upload option
- [ ] **Test: Upload 3 files simultaneously**

**Step 11.3: Document Viewer** (Optional)
- [ ] In-app PDF viewer
- [ ] Zoom and scroll
- [ ] Share document
- [ ] **Test: View PDF in-app**

**Step 11.4: Document History**
- [ ] Version history
- [ ] Who uploaded/modified
- [ ] Timestamp
- [ ] **Test: View document history**

---

## 🎯 Priority Matrix Summary

| Priority | Phases | Estimated Time | Status |
|----------|--------|----------------|--------|
| **CRITICAL** | Phases 1-3 | 10-14 hours | 🔴 In Progress |
| **HIGH** | Phases 4-6 | 10-13 hours | ⚪ Not Started |
| **MEDIUM** | Phases 7-8 | 8-10 hours | ⚪ Not Started |
| **LOW** | Phases 9-11 | 9-12 hours | ⚪ Not Started |
| **TOTAL** | - | **37-49 hours** | - |

---

## 📝 Implementation Rules

1. **No File Left Behind**: Complete each file 100% before moving to next
2. **Zero Errors Policy**: Fix all compilation errors before continuing
3. **Test Each Feature**: Test every feature after implementation
4. **Sequential Phases**: Complete phases in order (CRITICAL → HIGH → MEDIUM → LOW)
5. **No Priority Changes**: Priorities are fixed and will not change during development
6. **Document As You Go**: Update this file with completion status

---

## ✅ Testing Checklist Per Phase

### After Each Phase:
- [ ] Run `flutter analyze` → 0 errors, 0 warnings
- [ ] Test on Android emulator (Medium_Phone_API_36.1)
- [ ] Test all user flows end-to-end
- [ ] Verify API integration works correctly
- [ ] Check error handling (network errors, validation errors)
- [ ] Check loading states and progress indicators
- [ ] Verify navigation works correctly

### Before Moving to Next Phase:
- [ ] All features in current phase are 100% complete
- [ ] All tests pass
- [ ] No known bugs
- [ ] Code reviewed and cleaned up
- [ ] Update this document with completion status

---

## 🚀 Next Steps (RIGHT NOW)

1. **Fix the 201 compilation errors** (Phase 1)
2. Start with `lib/models/internship.dart` - add missing getters
3. Then fix `lib/utils/app_theme.dart` - create the file
4. Then fix providers and services
5. Verify 0 errors with `flutter analyze`

**DO NOT PROCEED TO PHASE 2 UNTIL PHASE 1 IS 100% COMPLETE AND ERROR-FREE**

---

## 📦 Dependencies to Add

```yaml
dependencies:
  file_picker: ^8.0.0  # For document upload
  sqflite: ^2.3.0  # For offline storage (Phase 10)
  path_provider: ^2.1.0  # For file paths
  open_file: ^3.3.2  # For opening downloaded files
  firebase_messaging: ^14.7.9  # For push notifications (Phase 6, optional)
  firebase_core: ^2.24.2  # Firebase core (if FCM used)
```

Add these as needed during respective phases.

---

**Last Updated**: December 16, 2025
**Current Phase**: Phase 1 - Fix Compilation Errors
**Next Milestone**: Complete Phase 1 with 0 errors
