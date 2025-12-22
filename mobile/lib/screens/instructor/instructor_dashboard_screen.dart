import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/internship.dart';
import '../../providers/auth_provider.dart';
import '../../providers/instructor_provider.dart';
import '../../theme/app_theme.dart';
import '../../widgets/glowing_card.dart';
import '../../widgets/stat_card.dart';
import '../shared/notification_bell_icon.dart';

class InstructorDashboardScreen extends ConsumerWidget {
  const InstructorDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authStateProvider);
    final internshipsState = ref.watch(instructorInternshipsProvider);

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
                          colors: [AppTheme.accentOrange, AppTheme.accentCyan],
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
                                    Icons.school_rounded,
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
                                        'Instructor Panel',
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
                    onRefresh: () async =>
                        ref.refresh(instructorInternshipsProvider.future),
                    child: Padding(
                      padding: const EdgeInsets.all(20),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          // Statistics
                          Text(
                            'Supervision Overview',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: AppTheme.textPrimary,
                            ),
                          ),
                          SizedBox(height: 16),
                          internshipsState.when(
                            data: (internships) =>
                                _buildStatistics(internships),
                            loading: () => Center(
                              child: CircularProgressIndicator(
                                color: AppTheme.accentCyan,
                              ),
                            ),
                            error: (error, _) =>
                                _buildErrorCard(error.toString()),
                          ),
                          SizedBox(height: 24),

                          // Available Internships Button
                          GlowingCard(
                            glowColor: AppTheme.accentCyan,
                            child: InkWell(
                              onTap: () =>
                                  context.push('/instructor/available'),
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: Row(
                                  children: [
                                    Container(
                                      padding: EdgeInsets.all(12),
                                      decoration: BoxDecoration(
                                        color: AppTheme.accentCyan.withOpacity(
                                          0.2,
                                        ),
                                        borderRadius: BorderRadius.circular(12),
                                      ),
                                      child: Icon(
                                        Icons.workspace_premium,
                                        color: AppTheme.accentCyan,
                                        size: 28,
                                      ),
                                    ),
                                    SizedBox(width: 16),
                                    Expanded(
                                      child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                          Text(
                                            'Claim Available Internships',
                                            style: TextStyle(
                                              fontSize: 16,
                                              fontWeight: FontWeight.bold,
                                              color: AppTheme.textPrimary,
                                            ),
                                          ),
                                          SizedBox(height: 4),
                                          Text(
                                            'View unassigned internships in your sectors',
                                            style: TextStyle(
                                              fontSize: 13,
                                              color: AppTheme.textSecondary,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                    Icon(
                                      Icons.arrow_forward_ios,
                                      color: AppTheme.accentCyan,
                                      size: 20,
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          ),
                          SizedBox(height: 24),

                          // Assigned Internships
                          Text(
                            'Assigned Internships',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.bold,
                              color: AppTheme.textPrimary,
                            ),
                          ),
                          SizedBox(height: 16),
                          internshipsState.when(
                            data: (internships) =>
                                _buildInternshipsList(internships, context),
                            loading: () => Center(
                              child: CircularProgressIndicator(
                                color: AppTheme.accentCyan,
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
            child: CircularProgressIndicator(color: AppTheme.accentCyan),
          ),
          error: (error, _) => Center(
            child: Text('Error: $error', style: TextStyle(color: Colors.red)),
          ),
        ),
      ),
    );
  }

  Widget _buildStatistics(List<Internship> internships) {
    final pending = internships
        .where((i) => i.status == InternshipStatus.PENDING_VALIDATION)
        .length;
    final validated = internships
        .where((i) => i.status == InternshipStatus.VALIDATED)
        .length;
    final refused = internships
        .where((i) => i.status == InternshipStatus.REFUSED)
        .length;

    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: NeverScrollableScrollPhysics(),
      mainAxisSpacing: 12,
      crossAxisSpacing: 12,
      childAspectRatio: 1.2,
      children: [
        StatCard(
          title: 'Total Assigned',
          value: internships.length.toString(),
          icon: Icons.assignment_rounded,
          gradient: AppTheme.accentGradient,
        ),
        StatCard(
          title: 'Pending Review',
          value: pending.toString(),
          icon: Icons.pending_actions_rounded,
          gradient: LinearGradient(
            colors: [AppTheme.warningYellow, AppTheme.accentOrange],
          ),
        ),
        StatCard(
          title: 'Validated',
          value: validated.toString(),
          icon: Icons.check_circle_rounded,
          gradient: AppTheme.successGradient,
        ),
        StatCard(
          title: 'Refused',
          value: refused.toString(),
          icon: Icons.cancel_rounded,
          gradient: LinearGradient(
            colors: [AppTheme.errorRed, Color(0xFFD32F2F)],
          ),
        ),
      ],
    );
  }

  Widget _buildInternshipsList(
    List<Internship> internships,
    BuildContext context,
  ) {
    if (internships.isEmpty) {
      return GlowingCard(
        glowColor: AppTheme.accentCyan,
        child: Column(
          children: [
            Icon(
              Icons.assignment_late_rounded,
              size: 64,
              color: AppTheme.textSecondary,
            ),
            SizedBox(height: 16),
            Text(
              'No internships assigned',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: AppTheme.textPrimary,
              ),
            ),
            SizedBox(height: 8),
            Text(
              'You will see assigned internships here',
              textAlign: TextAlign.center,
              style: TextStyle(color: AppTheme.textSecondary),
            ),
          ],
        ),
      );
    }

    return ListView.separated(
      shrinkWrap: true,
      physics: NeverScrollableScrollPhysics(),
      itemCount: internships.length,
      separatorBuilder: (_, __) => SizedBox(height: 12),
      itemBuilder: (context, index) {
        final internship = internships[index];
        return _buildInternshipCard(internship, context);
      },
    );
  }

  Widget _buildInternshipCard(Internship internship, BuildContext context) {
    Color statusColor;
    IconData statusIcon;

    switch (internship.status) {
      case InternshipStatus.VALIDATED:
        statusColor = AppTheme.successGreen;
        statusIcon = Icons.check_circle_rounded;
        break;
      case InternshipStatus.PENDING_VALIDATION:
        statusColor = AppTheme.warningYellow;
        statusIcon = Icons.pending_rounded;
        break;
      case InternshipStatus.REFUSED:
        statusColor = AppTheme.errorRed;
        statusIcon = Icons.cancel_rounded;
        break;
      case InternshipStatus.DRAFT:
        statusColor = AppTheme.textSecondary;
        statusIcon = Icons.edit_note_rounded;
        break;
    }

    return GlowingCard(
      glowColor: statusColor,
      onTap: () => context.push(
        '/instructor/internship/${internship.id}/detail',
        extra: internship,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: statusColor.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(statusIcon, color: statusColor, size: 24),
              ),
              SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      internship.title,
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: AppTheme.textPrimary,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    SizedBox(height: 4),
                    Text(
                      'Student: ${internship.studentName}',
                      style: TextStyle(
                        fontSize: 14,
                        color: AppTheme.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
              Icon(
                Icons.arrow_forward_ios_rounded,
                color: AppTheme.textSecondary,
                size: 16,
              ),
            ],
          ),
          SizedBox(height: 16),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _buildChip(
                Icons.business_rounded,
                internship.companyName,
                AppTheme.accentCyan,
              ),
              _buildChip(
                Icons.flag_rounded,
                internship.statusText,
                statusColor,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildChip(IconData icon, String label, Color color) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color.withOpacity(0.15),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withOpacity(0.3), width: 1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: color),
          SizedBox(width: 6),
          Text(
            label,
            style: TextStyle(
              fontSize: 12,
              color: color,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
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
