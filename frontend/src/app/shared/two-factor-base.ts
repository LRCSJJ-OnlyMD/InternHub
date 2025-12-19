import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Base class for components that need 2FA functionality.
 * Follows DRY principle - eliminates duplicate 2FA code across dashboards.
 */
export abstract class TwoFactorBase {
  protected authService = inject(AuthService);

  twoFactorEnabled = false;
  settingsLoading = false;
  settingsSuccessMessage = '';
  settingsErrorMessage = '';
  showQRCode = false;
  qrCodeUrl = '';
  twoFactorSecret = '';
  confirmationCode = '';

  /**
   * Load user's 2FA status
   */
  loadUserInfo(): void {
    this.authService.getUserInfo().subscribe({
      next: (response) => {
        this.twoFactorEnabled = response.twoFactorEnabled || false;
      },
      error: (error) => {
        console.error('Failed to load user info', error);
      }
    });
  }

  /**
   * Enable two-factor authentication
   */
  enableTwoFactor(): void {
    this.settingsLoading = true;
    this.settingsErrorMessage = '';
    this.settingsSuccessMessage = '';

    this.authService.enableTwoFactor().subscribe({
      next: (response) => {
        this.settingsLoading = false;
        this.qrCodeUrl = response.qrCodeUrl;
        this.twoFactorSecret = response.secret;
        this.showQRCode = true;
        this.settingsSuccessMessage = response.message;
      },
      error: (error) => {
        this.settingsLoading = false;
        this.settingsErrorMessage = error.error?.message || 'Failed to enable 2FA';
      }
    });
  }

  /**
   * Confirm 2FA setup with verification code
   */
  confirm2FA(): void {
    if (!this.confirmationCode || this.confirmationCode.length !== 6) {
      this.settingsErrorMessage = 'Please enter a valid 6-digit code';
      return;
    }

    this.settingsLoading = true;
    this.settingsErrorMessage = '';

    this.authService.confirm2FA(this.confirmationCode).subscribe({
      next: (response) => {
        this.settingsLoading = false;
        this.settingsSuccessMessage = response.message;
        this.showQRCode = false;
        this.confirmationCode = '';
        this.loadUserInfo();
        this.hideSettingsAlertsAfterDelay();
      },
      error: (error) => {
        this.settingsLoading = false;
        this.settingsErrorMessage = error.error?.message || 'Failed to confirm 2FA';
      }
    });
  }

  /**
   * Disable two-factor authentication
   */
  disableTwoFactor(): void {
    this.settingsLoading = true;
    this.settingsErrorMessage = '';
    this.settingsSuccessMessage = '';

    this.authService.disableTwoFactor().subscribe({
      next: (response) => {
        this.settingsLoading = false;
        this.twoFactorEnabled = false;
        this.settingsSuccessMessage = response.message;
        this.hideSettingsAlertsAfterDelay();
      },
      error: (error) => {
        this.settingsLoading = false;
        this.settingsErrorMessage = error.error?.message || 'Failed to disable 2FA';
      }
    });
  }

  /**
   * Cancel QR code setup
   */
  cancelQRSetup(): void {
    this.showQRCode = false;
    this.confirmationCode = '';
    this.qrCodeUrl = '';
    this.twoFactorSecret = '';
  }

  /**
   * Hide alert messages after delay
   */
  hideSettingsAlertsAfterDelay(): void {
    setTimeout(() => {
      this.settingsSuccessMessage = '';
      this.settingsErrorMessage = '';
    }, 5000);
  }
}
