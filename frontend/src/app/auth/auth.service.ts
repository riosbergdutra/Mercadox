import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AbstractType, Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { finalize, Observable } from 'rxjs';
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
 // Método para obter o token JWT do cookie
 getToken(): string | null {
  if (isPlatformBrowser(this.platformId)) {
    const cookie = document.cookie.split('; ').find(row => row.startsWith('accessToken='));
    return cookie ? cookie.split('=')[1] : null;
  }
  return null;
}

// Método para decodificar o token JWT
decodeToken(): any {
  const token = this.getToken();
  if (token) {
    try {
      return jwtDecode(token);  // Chamando corretamente a função jwtDecode
    } catch (error) {
      console.error('Erro ao decodificar o token JWT', error);
      return null;
    }
  }
  return null;
}


  register(usuarioRequest: FormData): Observable<void> {
    return this.http.post<void>(`${this.registerUrl}/criar`, usuarioRequest);
  }
}
