import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
  HttpErrorResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Interceptor HTTP para adicionar o token de acesso a requisições autenticadas
 * e tratar erros de autenticação (ex: token expirado) com a renovação do token.
 */
export const tokenInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  // Injeta a dependência do serviço de autenticação
  const authService = inject(AuthService);

  // Log para identificar a URL sendo interceptada
  console.log(`[TokenInterceptor] Interceptando requisição para: ${req.url}`);

  // Ignora a interceptação para URLs públicas ou quando o header 'skipAuth' está presente
  if (req.url.includes('/public') || req.headers.has('skipAuth')) {
    console.log('[TokenInterceptor] Requisição ignorada para autenticação.');
    return next(req);
  }

  // Recupera o token de acesso do serviço de autenticação
  const accessToken = authService.getAcessToken();
  console.log(`[TokenInterceptor] Token de acesso presente: ${!!accessToken}`);

  // Se o token de acesso estiver presente, clona a requisição e adiciona o header de autorização
  const authReq = accessToken
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${accessToken}`,
        },
      })
    : req;

  // Envia a requisição com ou sem o token, e trata os erros de autenticação
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('[TokenInterceptor] Erro durante a requisição:', error);

      // Se o erro for de autenticação (401), tenta renovar o token
      if (error.status === 401) {
        console.warn('[TokenInterceptor] Token expirado. Tentando renovar.');

        // Chama o método de renovação do token e tenta reexecutar a requisição com o novo token
        return authService.refreshTokenOnExpiry().pipe(
          switchMap((isRefreshed) => {
            if (isRefreshed) {
              // Se o token foi renovado com sucesso, cria uma nova requisição com o novo token
              const newAccessToken = authService.getAcessToken();
              if (newAccessToken) {
                const newAuthReq = req.clone({
                  setHeaders: {
                    Authorization: `Bearer ${newAccessToken}`,
                  },
                });
                return next(newAuthReq); // Retenta a requisição com o novo token
              }
            }
            // Se não foi possível renovar o token, lança um erro
            return throwError(() => new Error('Falha ao obter um novo token.'));
          }),
          catchError((refreshError) => {
            // Se ocorrer um erro ao tentar renovar o token, lança o erro
            console.error('Erro ao tentar renovar o token', refreshError);
            return throwError(() => refreshError);
          })
        );
      }

      // Caso não seja erro 401, apenas retorna o erro original
      return throwError(() => error);
    })
  );
};
