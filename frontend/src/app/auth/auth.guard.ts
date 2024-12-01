import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { LoadingService } from '../loading/loading.service'; 
import { take, tap } from 'rxjs/operators';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router); // Injeção do Router para navegação
  const authService = inject(AuthService); // Injeção do AuthService para verificar a autenticação
  const loadingService = inject(LoadingService); // Injeção do LoadingService para mostrar/esconder o loader

  console.log('[AuthGuard] Verificação de autenticação iniciada.');
  loadingService.show();

  return authService.authenticated$.pipe(
    take(1),
    tap((isAuthenticated) => {
      console.log(`[AuthGuard] Estado de autenticação: ${isAuthenticated}`);
      loadingService.hide();

      if (!isAuthenticated) {
        console.warn('[AuthGuard] Usuário não autenticado. Redirecionando para /login.');
        router.navigateByUrl('/login');
      }
    })
  );
};