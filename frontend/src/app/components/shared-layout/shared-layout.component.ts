import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../services/auth.service';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';

/**
 * Shared layout component with sidebar and topbar.
 * Used by Library, Chat, and other standalone pages.
 */
@Component({
  selector: 'app-shared-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    MatIconModule,
    MatButtonModule,
    NotificationBellComponent
  ],
  templateUrl: './shared-layout.component.html',
  styleUrls: ['./shared-layout.component.css']
})
export class SharedLayoutComponent implements OnInit {
  @Input() pageTitle: string = '';
  @Input() pageSubtitle: string = '';
  
  userName: string = '';
  userRole: string = '';
  dashboardLink: string = '/login';
  portalName: string = 'Portal';
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.userName = `${user.firstName} ${user.lastName}`;
      this.userRole = user.role;
      
      switch (user.role) {
        case 'STUDENT':
          this.dashboardLink = '/student-dashboard';
          this.portalName = 'Student Portal';
          break;
        case 'INSTRUCTOR':
          this.dashboardLink = '/instructor-dashboard';
          this.portalName = 'Instructor Portal';
          break;
        case 'ADMIN':
          this.dashboardLink = '/admin-dashboard';
          this.portalName = 'Admin Portal';
          break;
      }
    }
  }
  
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
  
  getDashboardIcon(): string {
    switch (this.userRole) {
      case 'STUDENT': return 'school';
      case 'INSTRUCTOR': return 'assignment_ind';
      case 'ADMIN': return 'admin_panel_settings';
      default: return 'dashboard';
    }
  }
}
