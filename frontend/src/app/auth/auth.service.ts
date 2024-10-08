import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AbstractType, Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { finalize, Observable } from 'rxjs';
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
  private authUrl = 'http://localhost:8000/auth';
  private registerUrl = 'http://localhost:8000/usuario';


  constructor(private http: HttpClient, @Inject(PLATFORM_ID) private platformId: Object, private loadingService: LoadingService) { }

  login(loginRequest: LoginRequest): Observable<void> {
    this.loadingService.show(); // Mostrar loading
    return this.http.post<void>(`${this.authUrl}/login`, loginRequest, { withCredentials: true })
      .pipe(finalize(() => this.loadingService.hide())); // Ocultar loading após a requisição
  }

  refreshToken(): Observable<void> { 
    return this.http.post<void>(`${this.authUrl}/refresh`, {});
  }

  logout(): void {
    // Chame um endpoint de logout se houver, ou confie na expiração automática do cookie
  }

  isAuthenticated(): boolean {
   if (isPlatformBrowser(this.platformId)) {
    return document.cookie.includes('accessToken');
  }
  return false;
}

  register(usuarioRequest: FormData): Observable<void> {
    return this.http.post<void>(`${this.registerUrl}/criar`, usuarioRequest);
  }
}
