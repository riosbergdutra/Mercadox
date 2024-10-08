import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { LoadingService } from '../loading/loading.service'; 

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const loadingService = inject(LoadingService); 

  loadingService.show();

  return new Promise<boolean>((resolve) => {
    const isAuthenticated = authService.isAuthenticated();

    setTimeout(() => {
      loadingService.hide(); 

      if (isAuthenticated) {
        resolve(true); 
      } else {
        router.navigateByUrl('/login');
        resolve(false);
      }
    }, 500); 
  });
};
