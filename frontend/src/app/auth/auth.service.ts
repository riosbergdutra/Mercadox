import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, NgZone, PLATFORM_ID } from '@angular/core';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';
import { UserInfo } from '../interfaces/UserInfo';

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
  providedIn: 'root',
})
export class AuthService {
  private readonly authUrl = 'http://localhost:8081/auth';
  private readonly registerUrl = 'http://localhost:8082/usuario';
  private readonly userUrl = 'http://localhost:8082/usuario/user-info';

  private userInfo: any = null; // Armazena os dados do usuário em memória
  private authenticatedSubject = new BehaviorSubject<boolean>(false);

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.initializeAuthState();
  }

  private initializeAuthState(): void {
    const token = this.getAcessToken();
    const refreshToken = this.getRefreshToken();
    
    // Apenas define o estado de autenticação com base na presença dos tokens
    if (token || refreshToken) {
      this.authenticatedSubject.next(true);
    } else {
      this.authenticatedSubject.next(false);
    }
  }

  get authenticated$(): Observable<boolean> {
    return this.authenticatedSubject.asObservable();
  }

  login(loginRequest: LoginRequest): Observable<void> {
    return this.http.post<any>(`${this.authUrl}/login`, loginRequest, { withCredentials: true })
      .pipe(
        switchMap(() => {
          this.authenticatedSubject.next(true);
          return this.getUserInfo();
        }),
        catchError((error) => {
          this.authenticatedSubject.next(false);
          return throwError(() => new Error('Erro ao realizar login'));
        })
      );
  }

  register(usuarioRequest: UsuarioRequestDto): Observable<void> {
    return this.http.post<void>(`${this.registerUrl}/criar`, usuarioRequest).pipe(
      catchError((error) => {
        return throwError(() => new Error('Erro ao registrar usuário'));
      })
    );
  }

  refreshToken(): Observable<void> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<any>(`${this.authUrl}/refresh`, { refreshToken }, { withCredentials: true })
      .pipe(
        catchError((error) => {
          console.error('Erro ao fazer refresh do token', error);
          return throwError(() => error);
        }),
        switchMap(() => {
          return of(void 0);
        })
      );
  }

  logout(): void {
    this.clearUserInfo();
    this.authenticatedSubject.next(false);
  }

  getUserInfo(): Observable<any> {
    return this.http.get<any>(this.userUrl).pipe(
      switchMap((user) => {
        this.userInfo = user;
        return of(user);
      }),
      catchError((error) => {
        return throwError(() => new Error('Erro ao buscar informações do usuário'));
      })
    );
  }

  getUserId(): Observable<string | null> {
    return this.getUserInfo().pipe(
      switchMap((user) => of(user?.idUsuario ?? null)),
      catchError(() => of(null))
    );
  }

  getStoredUserInfo(): any {
    return this.userInfo;
  }

  clearUserInfo(): void {
    this.userInfo = null;
  }

  getAcessToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      const cookie = document.cookie.split('; ').find(row => row.startsWith('accessToken='));
      const token = cookie ? cookie.split('=')[1] : null;
      return token;
    }
    return null;
  }

  getRefreshToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      const cookie = document.cookie.split('; ').find(row => row.startsWith('refreshToken='));
      const token = cookie ? cookie.split('=')[1] : null;
      return token;
    }
    return null;
  }

  refreshTokenOnExpiry(): Observable<boolean> {
    return this.refreshToken().pipe(
      switchMap(() => {
        this.authenticatedSubject.next(true);
        return this.validateSession();
      }),
      catchError(() => {
        this.authenticatedSubject.next(false);
        return of(false);
      })
    );
  }

    /** Valida a sessão do usuário consultando o back-end */
    private validateSession(): Observable<boolean> {
      return this.getUserInfo().pipe(
        switchMap(() => {
          this.authenticatedSubject.next(true);
          return of(true);
        }),
        catchError(() => {
          this.authenticatedSubject.next(false);
          return of(false);
        })
      );
    }
}