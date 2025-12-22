import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../providers/auth_provider.dart';
import '../../providers/admin_provider.dart';
import '../../theme/app_theme.dart';
import '../../widgets/glowing_card.dart';
import '../../widgets/stat_card.dart';
import '../../widgets/animated_gradient_button.dart';
import '../shared/notification_bell_icon.dart';

class AdminDashboardScreen extends ConsumerWidget {
  const AdminDashboardScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authStateProvider);
    final statsAsync = ref.watch(adminStatsProvider);

    return Scaffold(
      backgroundColor: AppTheme.darkBackground,
      body: SafeArea(
        child: authState.when(
          data: (user) {
            if (user == null) return const Center(child: Text('Not logged in'));

            return CustomScrollView(
              slivers: [
                // Modern App Bar
                SliverAppBar(
                  expandedHeight: 200,
                  floating: false,
                  pinned: true,
                  backgroundColor: AppTheme.darkBackground,
                  flexibleSpace: FlexibleSpaceBar(
                    background: Container(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          colors: [
                            AppTheme.primaryBlue,
                            AppTheme.secondaryOrange,
                          ],
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                        ),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.fromLTRB(20, 60, 20, 20),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: [
                            Row(
                              children: [
                                Container(
                                  padding: EdgeInsets.all(12),
                                  decoration: BoxDecoration(
                                    color: Colors.white.withOpacity(0.2),
                                    shape: BoxShape.circle,
                                  ),
                                  child: Icon(
                                    Icons.admin_panel_settings_rounded,
                                    size: 32,
                                    color: Colors.white,
                                  ),
                                ),
                                SizedBox(width: 16),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        'Admin Control Panel',
                                        style: TextStyle(
                                          color: Colors.white.withOpacity(0.9),
                                          fontSize: 14,
                                          fontWeight: FontWeight.w500,
                                        ),
                                      ),
                                      SizedBox(height: 4),
                                      Text(
                                        user.fullName,
                                        style: TextStyle(
                                          color: Colors.white,
                                          fontSize: 24,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                  actions: [
                    const NotificationBellIcon(),
                    PopupMenuButton<String>(
                      icon: Icon(Icons.more_vert, color: Colors.white),
                      onSelected: (value) {
                        if (value == 'profile') {
                          context.push('/profile');
                        } else if (value == 'settings') {
                          context.push('/settings');
                        } else if (value == 'logout') {
                          ref.read(authStateProvider.notifier).logout();
                          context.go('/login');
                        }
                      },
                      itemBuilder: (context) => [
                        const PopupMenuItem(
                          value: 'profile',
                          child: Row(
                            children: [
                              Icon(Icons.person_rounded),
                              SizedBox(width: 12),
                              Text('Profile'),
                            ],
                          ),
                        ),
                        const PopupMenuItem(
                          value: 'settings',
                          child: Row(
                            children: [
                              Icon(Icons.settings_rounded),
                              SizedBox(width: 12),
                              Text('Settings'),
                            ],
                          ),
                        ),
                        const PopupMenuDivider(),
                        const PopupMenuItem(
                          value: 'logout',
                          child: Row(
                            children: [
                              Icon(Icons.logout_rounded, color: Colors.red),
                              SizedBox(width: 12),
                              Text(
                                'Logout',
                                style: TextStyle(color: Colors.red),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ],
                ),

                // Content
                SliverToBoxAdapter(
                  child: RefreshIndicator(
                    onRefresh: () async {
                      ref.invalidate(adminStatsProvider);
                    },
                    child: Padding(
                      padding: const EdgeInsets.all(20),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          // Quick Actions
                          Text(
                            'Quick Actions',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: AppTheme.textPrimary,
                            ),
                          ),
                          SizedBox(height: 16),
                          GridView.count(
                            crossAxisCount: 2,
                            shrinkWrap: true,
                            physics: NeverScrollableScrollPhysics(),
                            mainAxisSpacing: 12,
                            crossAxisSpacing: 12,
                            childAspectRatio: 1.5,
                            children: [
                              _buildActionCard(
                                context,
                                'Users',
                                Icons.people_rounded,
                                AppTheme.primaryBlue,
                                () => context.push('/admin/users'),
                              ),
                              _buildActionCard(
                                context,
                                'Internships',
                                Icons.work_rounded,
                                AppTheme.secondaryOrange,
                                () => context.push('/admin/internships'),
                              ),
                              _buildActionCard(
                                context,
                                'Sectors',
                                Icons.category_rounded,
                                AppTheme.successGreen,
                                () => context.push('/admin/sectors'),
                              ),
                              _buildActionCard(
                                context,
                                'Search',
                                Icons.search_rounded,
                                AppTheme.primaryBlueLight,
                                () => context.push('/admin/advanced-search'),
                              ),
                            ],
                          ),
                          SizedBox(height: 24),

                          // Statistics
                          Text(
                            'System Overview',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: AppTheme.textPrimary,
                            ),
                          ),
                          SizedBox(height: 16),
                          statsAsync.when(
                            data: (stats) => _buildStatistics(stats),
                            loading: () => Center(
                              child: CircularProgressIndicator(
                                color: AppTheme.primaryBlueLight,
                              ),
                            ),
                            error: (error, _) =>
                                _buildErrorCard(error.toString()),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            );
          },
          loading: () => Center(
            child: CircularProgressIndicator(color: AppTheme.primaryBlueLight),
          ),
          error: (error, _) => Center(
            child: Text('Error: $error', style: TextStyle(color: Colors.red)),
          ),
        ),
      ),
    );
  }

  Widget _buildActionCard(
    BuildContext context,
    String title,
    IconData icon,
    Color color,
    VoidCallback onTap,
  ) {
    return GlowingCard(
      glowColor: color,
      onTap: onTap,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            padding: EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: color.withOpacity(0.2),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(icon, color: color, size: 24),
          ),
          SizedBox(height: 6),
          Text(
            title,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.bold,
              color: AppTheme.textPrimary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatistics(Map<String, dynamic> stats) {
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: NeverScrollableScrollPhysics(),
      mainAxisSpacing: 12,
      crossAxisSpacing: 12,
      childAspectRatio: 1.2,
      children: [
        StatCard(
          title: 'Total Users',
          value: stats['totalUsers']?.toString() ?? '0',
          icon: Icons.people_rounded,
          gradient: AppTheme.primaryGradient,
        ),
        StatCard(
          title: 'Internships',
          value: stats['totalInternships']?.toString() ?? '0',
          icon: Icons.work_rounded,
          gradient: AppTheme.accentGradient,
        ),
        StatCard(
          title: 'Pending',
          value: stats['pendingInternships']?.toString() ?? '0',
          icon: Icons.pending_rounded,
          gradient: LinearGradient(
            colors: [AppTheme.warningYellow, AppTheme.accentOrange],
          ),
        ),
        StatCard(
          title: 'Validated',
          value: stats['validatedInternships']?.toString() ?? '0',
          icon: Icons.check_circle_rounded,
          gradient: AppTheme.successGradient,
        ),
      ],
    );
  }

  Widget _buildErrorCard(String error) {
    return GlowingCard(
      glowColor: AppTheme.errorRed,
      child: Row(
        children: [
          Icon(Icons.error_rounded, color: AppTheme.errorRed, size: 32),
          SizedBox(width: 16),
          Expanded(
            child: Text(
              'Error: $error',
              style: TextStyle(color: AppTheme.textPrimary),
            ),
          ),
        ],
      ),
    );
  }
}
