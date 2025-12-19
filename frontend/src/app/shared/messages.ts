/**
 * Centralized error messages for frontend.
 * Follows DRY principle - single source of truth for UI messages.
 */
export const ERROR_MESSAGES = {
  // Form validation
  REQUIRED_FIELD: 'This field is required',
  INVALID_EMAIL: 'Please enter a valid email address',
  PASSWORD_MISMATCH: 'Passwords do not match',
  INVALID_2FA_CODE: 'Please enter a valid 6-digit code',
  
  // Authentication
  LOGIN_FAILED: 'Login failed. Please check your credentials.',
  REGISTRATION_FAILED: 'Registration failed. Please try again.',
  UNAUTHORIZED: 'You are not authorized to access this resource.',
  
  // Generic
  NETWORK_ERROR: 'Network error. Please check your connection.',
  UNKNOWN_ERROR: 'An unexpected error occurred. Please try again.',
  
  // Operations
  OPERATION_FAILED: 'Operation failed. Please try again.',
  LOAD_FAILED: 'Failed to load data. Please refresh the page.'
} as const;

export const SUCCESS_MESSAGES = {
  // Operations
  SAVED: 'Changes saved successfully',
  DELETED: 'Deleted successfully',
  UPDATED: 'Updated successfully',
  CREATED: 'Created successfully',
  
  // Authentication
  REGISTRATION_SUCCESS: 'Registration successful! Please check your email.',
  LOGIN_SUCCESS: 'Login successful',
  LOGOUT_SUCCESS: 'Logged out successfully',
  
  // 2FA
  TWO_FA_ENABLED: 'Two-factor authentication enabled successfully',
  TWO_FA_DISABLED: 'Two-factor authentication disabled successfully'
} as const;
