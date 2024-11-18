import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AbstractType, Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { catchError, finalize, Observable, of, switchMap, throwError } from 'rxjs';
import { LoadingService } from '../loading/loading.service';
import { jwtDecode } from "jwt-decode";

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioRequestDto {
  nome: string;
  email: string;
  senha: string;
  role: string;
  endereco: {
    rua: string;
    numero: string;
    cidade: string;
    estado: string;
    cep: string;
  };
  imagem?: File; // Para o upload da imagem, se necessário
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authUrl = 'http://localhost:8081/auth';
  private registerUrl = 'http://localhost:8000/usuario';

  constructor(private http: HttpClient, @Inject(PLATFORM_ID) private platformId: Object, private loadingService: LoadingService) {}

  login(loginRequest: LoginRequest): Observable<void> {
    console.log('Iniciando login com:', loginRequest);
    this.loadingService.show();
    return this.http.post<void>(`${this.authUrl}/login`, loginRequest, { withCredentials: true })
      .pipe(
        finalize(() => {
          console.log('Login concluído ou falhou, ocultando loading');
          this.loadingService.hide();
        })
      );
  }

  refreshToken(): Observable<void> {
    const refreshToken = this.getRefreshToken();
    console.log('Refresh token encontrado:', refreshToken);
    return this.http.post<any>(`${this.authUrl}/refresh`, { refreshToken }, { withCredentials: true })
      .pipe(
        catchError((error) => {
          console.error('Erro ao fazer refresh do token', error);
          return (error);
        }),
        finalize(() => {
          this.loadingService.hide();
        }),
        switchMap((response) => {
          console.log('Resposta de refresh:', response);
          // Não é necessário armazenar manualmente o token aqui,
          // o back-end já lida com os cookies
          return of(void 0);
        })
      );
  }
  
  
  
  
  
  logout(): void {
    console.log('Executando logout');
    // Lógica de logout (ex: limpar cookies, redirecionar)
  }

  isAuthenticated(): Observable<boolean> {
    console.log('Verificando se o usuário está autenticado');
    const token = this.getAcessToken();
    const refreshToken = this.getRefreshToken();
  
    if (!token && refreshToken) {
      console.log('Access token ausente, tentando fazer refresh com o refresh token');
      return this.refreshTokenOnExpiry().pipe(
        switchMap(() => {
          const refreshedToken = this.getAcessToken();
          if (refreshedToken) {
            console.log('Access token recuperado após refresh:', refreshedToken);
            return of(true); // Após o refresh, se o token for recuperado com sucesso, o usuário está autenticado
          } else {
            console.error('Erro: Access token não encontrado após refresh');
            return of(false); // Se o token não for recuperado após o refresh, o usuário não está autenticado
          }
        }),
        catchError((err) => {
          console.error('Erro ao usar refresh token', err);
          return of(false); // Se o refresh falhar, o usuário não está autenticado
        })
      );
    }
  
    if (token) {
      const decoded = this.decodeToken();
      if (decoded && decoded.exp) {
        const now = Math.floor(Date.now() / 1000);
        if (decoded.exp > now) {
          return of(true); // Token válido
        } else {
          return this.refreshTokenOnExpiry().pipe(
            switchMap(() => {
              const refreshedToken = this.getAcessToken();
              if (refreshedToken) {
                console.log('Access token recuperado após refresh');
                return of(true); // Após o refresh, se o token for recuperado com sucesso, o usuário está autenticado
              } else {
                console.error('Erro: Access token não encontrado após refresh');
                return of(false); // Se o token não for recuperado após o refresh, o usuário não está autenticado
              }
            }),
            catchError((err) => {
              console.error('Erro ao usar refresh token', err);
              return of(false);
            })
          );
        }
      }
    }
  
    console.log('Token ausente ou inválido');
    return of(false);
  }
  


  private refreshTokenOnExpiry(): Observable<boolean> {
    console.log('Tentando fazer refresh do token após expiração');
    return this.refreshToken().pipe(
      switchMap(() => {
        console.log('Refresh do token bem-sucedido');
        return of(true); // Retorna 'true' se o refresh foi bem-sucedido
      }),
      catchError((error) => {
        console.error('Erro ao tentar fazer refresh do token', error);
        return of(false); // Retorna 'false' se não conseguiu refresh
      })
    );
  }
  
  getAcessToken(): string | null {
    console.log('Buscando access token');
    if (isPlatformBrowser(this.platformId)) {
      const cookie = document.cookie.split('; ').find(row => row.startsWith('accessToken='));
      const token = cookie ? cookie.split('=')[1] : null;
      console.log('Access token encontrado:', token);
      return token;
    }
    console.log('Não está no ambiente de browser');
    return null;
  }

  getRefreshToken(): string | null {
    console.log('Buscando refresh token');
    if (isPlatformBrowser(this.platformId)) {
      const cookie = document.cookie.split('; ').find(row => row.startsWith('refreshToken='));
      const token = cookie ? cookie.split('=')[1] : null;
      console.log('Refresh token encontrado:', token);
      return token;
    }
    console.log('Não está no ambiente de browser');
    return null;
  }

  decodeToken(): any {
    console.log('Decodificando token');
    const token = this.getAcessToken();
    if (token) {
      try {
        const decoded = jwtDecode(token);
        console.log('Token decodificado:', decoded);
        return decoded;
      } catch (error) {
        console.error('Erro ao decodificar o token JWT', error);
        return null;
      }
    }
    console.log('Token não encontrado para decodificar');
    return null;
  }

  getUserId(): string | null {
    console.log('Buscando o ID do usuário');
    const decodedToken = this.decodeToken();
    if (decodedToken && decodedToken.sub) {
      console.log('ID do usuário encontrado no token:', decodedToken.sub);
      return decodedToken.sub; // Ajuste a propriedade para corresponder ao payload do seu JWT
    }
    console.log('ID do usuário não encontrado');
    return null;
  }
  

  register(usuarioRequest: FormData): Observable<void> {
    console.log('Registrando novo usuário');
    return this.http.post<void>(`${this.registerUrl}/criar`, usuarioRequest).pipe(
      catchError((error) => {
        console.error('Erro ao registrar usuário', error);
        return throwError(error);
      })
    );
  }
}
