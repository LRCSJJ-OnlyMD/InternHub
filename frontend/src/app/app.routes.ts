import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { 
    path: 'login', 
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  { 
    path: 'register', 
    loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent)
  },
  { 
    path: 'verify-email', 
    loadComponent: () => import('./components/verify-email/verify-email.component').then(m => m.VerifyEmailComponent)
  },
  { 
    path: 'forgot-password', 
    loadComponent: () => import('./components/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
  },
  { 
    path: 'reset-password', 
    loadComponent: () => import('./components/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
  },
  { 
    path: 'activate-account', 
    loadComponent: () => import('./components/activate-account/activate-account.component').then(m => m.ActivateAccountComponent)
  },
  { 
    path: 'student-dashboard', 
    loadComponent: () => import('./components/student-dashboard/student-dashboard.component').then(m => m.StudentDashboardComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['STUDENT'] }
  },
  { 
    path: 'instructor-dashboard', 
    loadComponent: () => import('./components/instructor-dashboard/instructor-dashboard.component').then(m => m.InstructorDashboardComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['INSTRUCTOR'] }
  },
  { 
    path: 'admin-dashboard', 
    loadComponent: () => import('./components/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  { 
    path: 'activity-logs', 
    loadComponent: () => import('./components/activity-logs/activity-logs.component').then(m => m.ActivityLogsComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  { 
    path: 'internship-search', 
    loadComponent: () => import('./components/internship-search/internship-search.component').then(m => m.InternshipSearchComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'enhanced-statistics', 
    loadComponent: () => import('./components/enhanced-statistics/enhanced-statistics.component').then(m => m.EnhancedStatisticsComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  { 
    path: 'bulk-operations', 
    loadComponent: () => import('./components/bulk-operations/bulk-operations.component').then(m => m.BulkOperationsComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'INSTRUCTOR'] }
  },
  { 
    path: 'notification-preferences', 
    loadComponent: () => import('./components/notification-preferences/notification-preferences.component').then(m => m.NotificationPreferencesComponent),
    canActivate: [authGuard]
  },
  { 
    path: 'dashboard', 
    redirectTo: '/login',
    pathMatch: 'full'
  },
  { 
    path: 'unauthorized', 
    loadComponent: () => import('./components/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent)
  },
  { path: '**', redirectTo: '/login' }
];
