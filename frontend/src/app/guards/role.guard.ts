import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard to protect routes based on user roles.
 * Implements role-based access control (RBAC) on the frontend.
 * Checks JWT token for user role before allowing route activation.
 */
export const roleGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
): boolean => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  const expectedRoles = route.data['roles'] as Array<string>;
  
  if (!authService.isLoggedIn()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  const userRole = authService.getUserRole();
  
  if (!userRole || !expectedRoles.includes(userRole)) {
    // User doesn't have required role, redirect to unauthorized page
    router.navigate(['/unauthorized']);
    return false;
  }

  return true;
};
