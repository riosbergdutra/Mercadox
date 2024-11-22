import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AbstractType, Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { catchError, finalize, Observable, of, switchMap, throwError } from 'rxjs';
import { LoadingService } from '../loading/loading.service';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioRequestDto {
  nome: string;
  email: string;
  senha: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authUrl = 'http://localhost:8081/auth';
  private registerUrl = 'http://localhost:8082/usuario';
  private userUrl = 'http://localhost:8082/usuario/user-info';

  private userInfo: any = null;


  constructor(private http: HttpClient, @Inject(PLATFORM_ID) private platformId: Object, private loadingService: LoadingService) {}


   // Método de registro de usuário
   register(usuarioRequest: UsuarioRequestDto): Observable<void> {
    console.log('Registrando novo usuário', usuarioRequest);
    this.loadingService.show(); // Exibe o loading enquanto o registro é processado
    
    return this.http.post<void>(`${this.registerUrl}/criar`, usuarioRequest).pipe(
      finalize(() => {
        this.loadingService.hide(); // Esconde o loading após a operação
      }),
      catchError((error) => {
        console.error('Erro ao registrar usuário', error);
        return throwError(() => new Error('Erro ao registrar usuário'));
      })
    );
  }

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
            // Chama getUserInfo após sucesso do refresh
            return this.getUserInfo().pipe(
              switchMap(() => {
                return this.getUserId().pipe(  // Chama getUserId após obter as informações do usuário
                  switchMap((userId) => {
                    if (userId) {
                      console.log('ID do usuário após refresh:', userId);
                      return of(true);  // Retorna true se o ID for encontrado
                    }
                    console.error('ID do usuário não encontrado');
                    return of(false);  // Retorna false caso o ID não seja encontrado
                  })
                );
              }),
              catchError((err) => {
                console.error('Erro ao buscar informações do usuário após refresh', err);
                return of(false);
              })
            );
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
  
    // Verifica se o token está presente e tem tempo de expiração no cookie
    if (token) {
      const expTime = this.getTokenExpirationTime(token);
      const now = Math.floor(Date.now() / 1000); // Tempo atual em segundos
      if (expTime && expTime > now) {
        console.log('Token ainda válido');
        // Chama getUserInfo quando o token é válido
        return this.getUserInfo().pipe(
          switchMap(() => {
            return this.getUserId().pipe(  // Chama getUserId após obter as informações do usuário
              switchMap((userId) => {
                if (userId) {
                  console.log('ID do usuário:', userId);
                  return of(true);  // Retorna true se o ID for encontrado
                }
                console.error('ID do usuário não encontrado');
                return of(false);  // Retorna false caso o ID não seja encontrado
              })
            );
          }),
          catchError((err) => {
            console.error('Erro ao buscar informações do usuário', err);
            return of(false);
          })
        );
      } else {
        console.log('Token expirado ou sem expiração válida');
        return this.refreshTokenOnExpiry().pipe(
          switchMap(() => {
            const refreshedToken = this.getAcessToken();
            if (refreshedToken) {
              console.log('Access token recuperado após refresh');
              // Chama getUserInfo após sucesso do refresh
              return this.getUserInfo().pipe(
                switchMap(() => {
                  return this.getUserId().pipe(  // Chama getUserId após obter as informações do usuário
                    switchMap((userId) => {
                      if (userId) {
                        console.log('ID do usuário após refresh:', userId);
                        return of(true);  // Retorna true se o ID for encontrado
                      }
                      console.error('ID do usuário não encontrado');
                      return of(false);  // Retorna false caso o ID não seja encontrado
                    })
                  );
                }),
                catchError((err) => {
                  console.error('Erro ao buscar informações do usuário após refresh', err);
                  return of(false);
                })
              );
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
  
    console.log('Token ausente ou inválido');
    return of(false); // Se o token estiver ausente ou inválido
  }
  
  
  // Método que extrai o tempo de expiração do cookie do access token
  getTokenExpirationTime(token: string): number | null {
    try {
      const payload = token.split('.')[1]; // O payload está no segundo segmento do JWT
      const decoded = JSON.parse(atob(payload)); // Decodifica o base64
      return decoded.exp ? decoded.exp : null; // Retorna o tempo de expiração se existir
    } catch (error) {
      console.error('Erro ao tentar decodificar o token:', error);
      return null; // Retorna null se o token não for decodificado corretamente
    }
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

   // Método para pegar as informações do usuário do back-end e armazenar na memória
   getUserInfo(): Observable<any> {
    const token = this.getAcessToken();
    if (token) {
      console.log('Buscando informações do usuário');
      return this.http.get<any>(this.userUrl, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }).pipe(
        catchError((error) => {
          console.error('Erro ao buscar informações do usuário', error);
          return throwError(() => new Error('Erro ao buscar informações do usuário'));
        }),
        switchMap((user) => {
          if (user) {
            this.userInfo = user;  // Armazena em memória
            console.log('Informações do usuário recebidas:', this.userInfo);  // Verifique as informações aqui
            return of(user);
          }
          return of(null);
        })
      );
    }
    return throwError(() => new Error('Token de acesso ausente'));
  }
  

  // Método para pegar as informações do usuário armazenadas em memória
  getStoredUserInfo(): any {
    return this.userInfo;  // Retorna as informações armazenadas em memória
  }

  // Método para limpar as informações do usuário da memória
  clearUserInfo(): void {
    this.userInfo = null;  // Limpa as informações armazenadas em memória
  }
   // Método para obter o ID do usuário
   getUserId(): Observable<string | null> {
    return this.getUserInfo().pipe(
      switchMap((idUsuario) => {
        if (idUsuario && idUsuario.idUsuario) {
          console.log('ID do usuário:', idUsuario.idUsuario);
          return of(idUsuario.idUsuario);
        }
        return of(null);
      }),
      catchError((error) => {
        console.error('Erro ao obter ID do usuário', error);
        return of(null);
      })
    );
  }
  
  }

