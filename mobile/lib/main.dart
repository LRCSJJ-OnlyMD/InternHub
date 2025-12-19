import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'screens/auth/login_screen.dart';
import 'screens/auth/register_screen.dart';
import 'screens/auth/email_verification_screen.dart';
import 'screens/auth/forgot_password_screen.dart';
import 'screens/auth/reset_password_screen.dart';
import 'screens/student/student_dashboard_screen.dart';
import 'screens/student/create_internship_screen.dart';
import 'screens/student/edit_internship_screen.dart';
import 'screens/student/internship_detail_screen.dart';
import 'screens/instructor/instructor_dashboard_screen.dart';
import 'screens/instructor/instructor_internships_screen.dart';
import 'screens/instructor/instructor_internship_detail_screen.dart';
import 'screens/admin/admin_dashboard_screen.dart';
import 'screens/admin/users_management_screen.dart';
import 'screens/admin/user_form_screen.dart';
import 'screens/admin/sectors_management_screen.dart';
import 'screens/admin/admin_internships_screen.dart';
import 'screens/admin/advanced_search_screen.dart';
import 'screens/shared/notifications_screen.dart';
import 'screens/shared/notification_preferences_screen.dart';
import 'screens/shared/profile_screen.dart';
import 'screens/shared/settings_screen.dart';
import 'screens/shared/document_manager_screen.dart';
import 'providers/auth_provider.dart';
import 'models/internship.dart';
import 'models/user.dart';
import 'theme/app_theme.dart';

// Import theme provider from settings screen
import 'screens/shared/settings_screen.dart' show themeProvider;

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const ProviderScope(child: MyApp()));
}

class MyApp extends ConsumerWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = GoRouter(
      initialLocation: '/login',
      redirect: (context, state) {
        final authState = ref.read(authStateProvider);
        final isLoggedIn = authState.value != null;
        final publicPaths = [
          '/login',
          '/register',
          '/verify-email',
          '/forgot-password',
          '/reset-password',
        ];
        final isPublicPath = publicPaths.contains(state.matchedLocation);

        if (!isLoggedIn && !isPublicPath) {
          return '/login';
        }
        if (isLoggedIn && state.matchedLocation == '/login') {
          final user = authState.value;
          if (user != null) {
            // Redirect based on role
            if (user.role == Role.ADMIN) {
              return '/admin/dashboard';
            } else if (user.role == Role.INSTRUCTOR) {
              return '/instructor/dashboard';
            } else {
              return '/dashboard';
            }
          }
          return '/dashboard';
        }
        return null;
      },
      routes: [
        GoRoute(
          path: '/login',
          builder: (context, state) => const LoginScreen(),
        ),
        GoRoute(
          path: '/register',
          builder: (context, state) => const RegisterScreen(),
        ),
        GoRoute(
          path: '/verify-email',
          builder: (context, state) {
            final email = state.uri.queryParameters['email'];
            return EmailVerificationScreen(email: email);
          },
        ),
        GoRoute(
          path: '/forgot-password',
          builder: (context, state) => const ForgotPasswordScreen(),
        ),
        GoRoute(
          path: '/reset-password',
          builder: (context, state) {
            final token = state.uri.queryParameters['token'];
            return ResetPasswordScreen(token: token);
          },
        ),
        GoRoute(
          path: '/dashboard',
          builder: (context, state) => const StudentDashboardScreen(),
        ),
        GoRoute(
          path: '/internship/create',
          builder: (context, state) => const CreateInternshipScreen(),
        ),
        GoRoute(
          path: '/internship/:id/edit',
          builder: (context, state) {
            final internship = state.extra as Internship;
            return EditInternshipScreen(internship: internship);
          },
        ),
        GoRoute(
          path: '/internship/:id/detail',
          builder: (context, state) {
            final internship = state.extra as Internship;
            return InternshipDetailScreen(internship: internship);
          },
        ),
        GoRoute(
          path: '/instructor/dashboard',
          builder: (context, state) => const InstructorDashboardScreen(),
        ),
        GoRoute(
          path: '/instructor/internships',
          builder: (context, state) => const InstructorInternshipsScreen(),
        ),
        GoRoute(
          path: '/instructor/internship/:id/detail',
          builder: (context, state) {
            final internship = state.extra as Internship;
            return InstructorInternshipDetailScreen(internship: internship);
          },
        ),
        GoRoute(
          path: '/notifications',
          builder: (context, state) => const NotificationsScreen(),
        ),
        GoRoute(
          path: '/notification-preferences',
          builder: (context, state) => const NotificationPreferencesScreen(),
        ),
        GoRoute(
          path: '/profile',
          builder: (context, state) => const ProfileScreen(),
        ),
        GoRoute(
          path: '/settings',
          builder: (context, state) => const SettingsScreen(),
        ),
        GoRoute(
          path: '/documents/:id',
          builder: (context, state) {
            final internshipId = int.parse(state.pathParameters['id']!);
            final internshipTitle =
                (state.extra as Map<String, dynamic>?)?['title'] ??
                'Internship';
            return DocumentManagerScreen(
              internshipId: internshipId,
              internshipTitle: internshipTitle,
            );
          },
        ),
        GoRoute(
          path: '/admin/dashboard',
          builder: (context, state) => const AdminDashboardScreen(),
        ),
        GoRoute(
          path: '/admin/users',
          builder: (context, state) => const UsersManagementScreen(),
        ),
        GoRoute(
          path: '/admin/users/create',
          builder: (context, state) => const UserFormScreen(),
        ),
        GoRoute(
          path: '/admin/users/:id/edit',
          builder: (context, state) {
            final user = state.extra as User;
            return UserFormScreen(user: user);
          },
        ),
        GoRoute(
          path: '/admin/sectors',
          builder: (context, state) => const SectorsManagementScreen(),
        ),
        GoRoute(
          path: '/admin/internships',
          builder: (context, state) => const AdminInternshipsScreen(),
        ),
        GoRoute(
          path: '/admin/search',
          builder: (context, state) => const AdvancedSearchScreen(),
        ),
      ],
    );

    return MaterialApp.router(
      title: 'InternHub',
      theme: AppTheme.darkTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: ThemeMode.dark, // Force dark theme for gaming aesthetic
      routerConfig: router,
      debugShowCheckedModeBanner: false,
    );
  }
}
