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
  const authService = inject(AuthService);

  console.log(`[TokenInterceptor] Interceptando requisição para: ${req.url}`);

  // Ignora a interceptação para URLs públicas ou quando o header 'skipAuth' está presente
  if (req.url.includes('/public') || req.headers.has('skipAuth')) {
    console.log('[TokenInterceptor] Requisição ignorada para autenticação.');
    return next(req);
  }

  const accessToken = authService.getAcessToken();
  const refreshToken = authService.getRefreshToken();

  // Se não houver token ou o login falhou, não tenta renovar
  if (!accessToken && !refreshToken) {
    return next(req); // Não adiciona o token e passa a requisição sem o token
  }

  const authReq = accessToken
    ? req.clone({
        setHeaders: {
          Authorization: `Bearer ${accessToken}`,
        },
      })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('[TokenInterceptor] Erro durante a requisição:', error);

      if (error.status === 401) {
        console.warn('[TokenInterceptor] Token expirado. Tentando renovar.');

        return authService.refreshTokenOnExpiry().pipe(
          switchMap((isRefreshed) => {
            if (isRefreshed) {
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
            return throwError(() => new Error('Falha ao obter um novo token.'));
          }),
          catchError((refreshError) => {
            console.error('Erro ao tentar renovar o token', refreshError);
            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  );
};
