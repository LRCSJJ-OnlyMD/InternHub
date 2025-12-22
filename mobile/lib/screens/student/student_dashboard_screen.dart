import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/internship.dart';
import '../../providers/auth_provider.dart';
import '../../providers/internship_provider.dart';
import '../../theme/app_theme.dart';
import '../../widgets/glowing_card.dart';
import '../../widgets/stat_card.dart';
import '../../widgets/animated_gradient_button.dart';
import '../shared/notification_bell_icon.dart';

class StudentDashboardScreen extends ConsumerWidget {
  const StudentDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authStateProvider);
    final internshipsState = ref.watch(internshipsProvider);

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
                        gradient: AppTheme.primaryGradient,
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
                                    Icons.person_rounded,
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
                                        'Welcome Back',
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
                        AnimatedGradientButton(
                          text: 'Create New Internship',
                          icon: Icons.add_circle_rounded,
                          gradient: AppTheme.primaryGradient,
                          onPressed: () => context.push('/internship/create'),
                          height: 60,
                        ),
                        SizedBox(height: 24),

                        // Statistics
                        Text(
                          'Your Progress',
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                            color: AppTheme.textPrimary,
                          ),
                        ),
                        SizedBox(height: 16),
                        internshipsState.when(
                          data: (internships) => _buildStatistics(internships),
                          loading: () => Center(
                            child: CircularProgressIndicator(
                              color: AppTheme.primaryBlueLight,
                            ),
                          ),
                          error: (error, _) =>
                              _buildErrorCard(error.toString()),
                        ),
                        SizedBox(height: 24),

                        // My Internships
                        Row(
                          children: [
                            Text(
                              'My Internships',
                              style: TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                                color: AppTheme.textPrimary,
                              ),
                            ),
                          ],
                        ),
                        SizedBox(height: 16),
                        internshipsState.when(
                          data: (internships) =>
                              _buildInternshipsList(internships, context),
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

  Widget _buildStatistics(List<Internship> internships) {
    final drafts = internships
        .where((i) => i.status == InternshipStatus.DRAFT)
        .length;
    final pending = internships
        .where((i) => i.status == InternshipStatus.PENDING_VALIDATION)
        .length;
    final validated = internships
        .where((i) => i.status == InternshipStatus.VALIDATED)
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
          title: 'Total',
          value: internships.length.toString(),
          icon: Icons.list_alt_rounded,
          gradient: LinearGradient(
            colors: [Color(0xFF667eea), Color(0xFF764ba2)],
          ),
        ),
        StatCard(
          title: 'Validated',
          value: validated.toString(),
          icon: Icons.check_circle_rounded,
          gradient: AppTheme.successGradient,
        ),
        StatCard(
          title: 'Pending',
          value: pending.toString(),
          icon: Icons.hourglass_bottom_rounded,
          gradient: LinearGradient(
            colors: [AppTheme.accentOrange, AppTheme.warningYellow],
          ),
        ),
        StatCard(
          title: 'Drafts',
          value: drafts.toString(),
          icon: Icons.edit_note_rounded,
          gradient: AppTheme.accentGradient,
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
        glowColor: AppTheme.primaryBlueLight,
        child: Column(
          children: [
            Icon(Icons.inbox_rounded, size: 64, color: AppTheme.textSecondary),
            SizedBox(height: 16),
            Text(
              'No internships yet',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: AppTheme.textPrimary,
              ),
            ),
            SizedBox(height: 8),
            Text(
              'Create your first internship to get started!',
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
        '/internship/${internship.id}/detail',
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
                      internship.companyName,
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
                internship.sectorName,
                AppTheme.accentCyan,
              ),
              _buildChip(
                Icons.calendar_today_rounded,
                '${internship.durationInDays} days',
                AppTheme.secondaryPink,
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
