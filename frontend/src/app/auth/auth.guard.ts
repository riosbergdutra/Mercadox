import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { LoadingService } from '../loading/loading.service'; 
import { Observable } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router); // Injeção do Router para navegação
  const authService = inject(AuthService); // Injeção do AuthService para verificar a autenticação
  const loadingService = inject(LoadingService); // Injeção do LoadingService para mostrar/esconder o loader

  loadingService.show(); // Exibe o loader enquanto verifica a autenticação

  // Retorna diretamente a Promise com a verificação de autenticação
  return new Observable<boolean>((observer) => {
    authService.isAuthenticated().subscribe({
      next: (isAuthenticated) => {
        loadingService.hide(); // Esconde o loader após a verificação

        if (isAuthenticated) {
          observer.next(true); // Permite o acesso à rota
        } else {
          router.navigateByUrl('/login'); // Redireciona para o login se não estiver autenticado
          observer.next(false); // Bloqueia o acesso à rota
        }
        observer.complete();
      },
      error: (err) => {
        loadingService.hide();
        console.error('Erro ao verificar autenticação', err);
        observer.next(false); // Bloqueia o acesso caso ocorra um erro na verificação
        observer.complete();
      }
    });
  });
};
