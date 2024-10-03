import { HttpClient } from '@angular/common/http';
import { AbstractType, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

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
  private registerUrl = 'http://localhost:8082/usuario';


  constructor(private http: HttpClient) { }

  login(loginRequest: LoginRequest): Observable<void> {
    return this.http.post<void>(`${this.authUrl}/login`, loginRequest, { withCredentials: true });
  }
  
  refreshToken(): Observable<void> { 
    return this.http.post<void>(`${this.authUrl}/refresh`, {});
  }

  logout(): void {
    // Chame um endpoint de logout se houver, ou confie na expiração automática do cookie
  }

  isAuthenticated(): boolean {
    // Se quiser verificar a presença do cookie
    return document.cookie.includes('accessToken');
  }

  register(usuarioRequest: FormData): Observable<void> {
    return this.http.post<void>(`${this.registerUrl}/criar`, usuarioRequest);
  }
}
