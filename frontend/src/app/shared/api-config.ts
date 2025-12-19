import { environment } from '../../environments/environment';

/**
 * Constants for API endpoints.
 * Centralized configuration following DRY principle.
 */
export const API_CONFIG = {
  BASE_URL: environment.apiUrl.replace('/api', ''),
  ENDPOINTS: {
    AUTH: '/api/auth',
    ADMIN: '/api/admin',
    STUDENT: '/api/student',
    INSTRUCTOR: '/api/instructor',
    INTERNSHIPS: '/internships',
    SECTORS: '/sectors',
    STATISTICS: '/statistics',
    USERS: '/users'
  },
  NOTIFICATIONS: `${environment.apiUrl}/notifications`
} as const;

/**
 * Get full API URL for a given endpoint
 */
export function getApiUrl(...paths: string[]): string {
  return `${API_CONFIG.BASE_URL}${paths.join('')}`;
}
