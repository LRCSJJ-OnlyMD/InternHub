import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ChangePasswordModalComponent } from '../change-password-modal/change-password-modal.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ChangePasswordModalComponent],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  showPasswordModal = false;
  mustChangePassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const formData = this.loginForm.value;

    this.authService.login(formData).subscribe({
      next: (response) => {
        this.loading = false;
        // Check if password change is required
        if (response.mustChangePassword) {
          this.mustChangePassword = true;
          this.showPasswordModal = true;
        } else {
          // Navigate based on user role
          this.authService.navigateByRole();
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Login failed';
      }
    });
  }

  onPasswordChanged(success: boolean): void {
    this.showPasswordModal = false;
    if (success) {
      // Password changed successfully, navigate to dashboard
      this.authService.navigateByRole();
    }
  }
}
