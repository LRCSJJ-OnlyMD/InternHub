# InternHub Mobile - API Endpoints Summary

## Authentication Endpoints (`/api/auth`)
- `POST /register` - User registration
- `POST /login` - User login
- `GET /verify-email` - Email verification
- `POST /password-reset/request` - Request password reset
- `POST /password-reset/confirm` - Confirm password reset
- `GET /me` - Get current user profile
- `PUT /profile` - Update user profile
- `POST /change-password` - Change user password
- `POST /2fa/enable` - Enable 2FA
- `POST /2fa/disable` - Disable 2FA
- `POST /2fa/confirm` - Confirm 2FA code
- `POST /2fa/send-email` - Send 2FA via email
- `POST /activate-account` - Activate account

## Student Endpoints (`/api/student/internships`)
- `GET /` - Get all internships for student
- `POST /` - Create new internship
- `PUT /{id}` - Update internship
- `POST /{id}/submit` - Submit internship
- `POST /{id}/report` - Upload internship report
- `GET /{id}/report` - Get internship report

**Note:** Student endpoints do NOT have GET /{id} to fetch a single internship. The mobile app passes the internship object via navigation.

## Instructor Endpoints (`/api/instructor/internships`)
- `GET /pending` - Get pending internships
- `GET /available` - Get available internships (unclaimed)
- `GET /validated` - Get validated internships
- `POST /{id}/claim` - Claim an internship
- `POST /{id}/validate` - Validate an internship
- `POST /{id}/refuse` - Refuse an internship
- `GET /{id}/report` - Get internship report

## Admin Endpoints (`/api/admin`)
### Sectors
- `POST /sectors` - Create sector
- `PUT /sectors/{id}` - Update sector
- `DELETE /sectors/{id}` - Delete sector
- `GET /sectors` - Get all sectors
- `GET /sectors/{id}` - Get sector by ID

### Internships
- `GET /internships` - Get all internships
- `GET /internships/{id}` - Get internship by ID ⭐ (Used for detail view)
- `DELETE /internships/{id}` - Delete internship
- `PUT /internships/{id}/reassign/{instructorId}` - Reassign internship
- `GET /internships/search` - Advanced search (alias: `/admin/advanced-search`)

### Statistics
- `GET /stats/by-status` - Statistics by status (returns {label, count})
- `GET /stats/by-sector` - Statistics by sector (returns {label, count})
- `GET /stats/detailed` - Detailed statistics

## User Management (`/api/admin/users`)
- `POST /instructors` - Create instructor
- `GET /instructors` - Get all instructors
- `GET /students` - Get all students
- `PUT /instructors/{id}/sectors` - Update instructor sectors
- `DELETE /{id}` - Delete user

## Comments (`/api/internships/{internshipId}/comments`)
- `POST /` - Add comment
- `GET /` - Get all comments for internship
- `PUT /{commentId}` - Update comment
- `DELETE /{commentId}` - Delete comment
- `GET /count` - Get comment count

## Documents (`/api/documents`)
- `POST /upload` - Upload document
- `POST /upload-version` - Upload new version
- `GET /internship/{internshipId}` - Get documents for internship
- `GET /internship/{internshipId}/latest` - Get latest document
- `GET /{documentId}` - Get document by ID
- `GET /internship/{internshipId}/history` - Get document history
- `GET /{documentId}/download` - Download document
- `DELETE /{documentId}` - Delete document
- `DELETE /internship/{internshipId}/all-versions` - Delete all versions

## Notifications (`/api/notifications`)
- `GET /` - Get all notifications
- `GET /unread` - Get unread notifications
- `GET /unread/count` - Get unread count
- `PUT /{id}/read` - Mark as read
- `PUT /mark-all-read` - Mark all as read
- `DELETE /{id}` - Delete notification

## Statistics (`/api/statistics`)
- `GET /by-status` - Statistics by status
- `GET /by-sector` - Statistics by sector
- `GET /by-status-and-sector` - Combined statistics
- `GET /enhanced` - Enhanced statistics
- `GET /instructor/{instructorId}` - Instructor statistics
- `GET /student/{studentId}` - Student statistics

## Search (`/api/search`)
- `GET /search` - Simple search
- `POST /search` - Advanced search with filters

## Bulk Operations (`/api/bulk`)
- `POST /operation` - Perform bulk operation
- `POST /update-status` - Bulk update status
- `POST /assign-instructor` - Bulk assign instructor
- `POST /validate` - Bulk validate
- `POST /reject` - Bulk reject
- `POST /delete` - Bulk delete

## Export (`/api/export`)
- `GET /internships` - Export internships (PDF/Excel)
- `GET /users` - Export users (PDF/Excel)

## Activity Log (`/api/activity-log`)
- `GET /` - Get all activity logs
- `GET /search` - Search activity logs
- `GET /user/{userEmail}` - Get logs for user
- `GET /action/{actionType}` - Get logs by action
- `GET /entity/{entityType}/{entityId}` - Get logs for entity
- `GET /action-types` - Get available action types
- `GET /entity-types` - Get available entity types

## Health Check (`/api/health`)
- `GET /` - Health status
- `GET /redis` - Redis health status

## Mobile App Navigation Routes
- `/login` - Login screen
- `/dashboard` - Student dashboard
- `/instructor/dashboard` - Instructor dashboard
- `/admin/dashboard` - Admin dashboard
- `/internship/create` - Create internship (student only)
- `/internship/{id}/detail` - Internship detail (requires `extra: internship` object)
- `/admin/users` - User management
- `/admin/sectors` - Sector management
- `/admin/advanced-search` - Advanced search

## Fixed Issues
✅ Student dashboard navigation now passes internship object: `context.push('/internship/${internship.id}/detail', extra: internship)`
✅ Logo displayed in login screen (assets/logo.png)
✅ All emojis removed from professional UI
✅ Role-based routing working correctly
✅ Admin statistics parsing fixed (label/count structure)
✅ Admin users endpoint combines instructors and students
✅ Instructor service uses proper /api/instructor/internships endpoints

## Important Notes
1. **No GET /student/internships/{id}**: Students cannot fetch single internship by ID. The detail screen receives the full internship object via navigation.
2. **GET /admin/internships/{id}**: Only admin endpoint provides single internship fetch functionality.
3. **Statistics Format**: Backend returns `{label: string, count: Long}` not `{status/sectorName: string, count: number}`
4. **All endpoints require authentication** via JWT token in Authorization header
5. **Role-based access**: Endpoints are restricted by user role (STUDENT, INSTRUCTOR, ADMIN)
