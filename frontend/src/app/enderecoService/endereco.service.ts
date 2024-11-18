import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, of } from 'rxjs';
import { Endereco } from '../models/Endereco';
import { AuthService } from '../auth/auth.service'; // Certifique-se de importar o AuthService

@Injectable({
  providedIn: 'root'
})
export class EnderecoService {
  private apiUrl = 'http://localhost:8082/endereco'; // URL da API que você está consumindo

  constructor(private http: HttpClient, private authService: AuthService) {}

getEnderecos(userId: string): Observable<Endereco[]> {
  const token = this.authService.getAcessToken(); // Obtém o token de acesso
  console.log('Obtendo token:', token); // Log para verificar o token

  let headers = new HttpHeaders();

  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`); // Adiciona o token ao header
  } else {
    console.warn('Token de acesso não encontrado');
  }

  return this.http.get<Endereco[]>(`${this.apiUrl}/${userId}`, { headers }).pipe(
    // Adicionando tratamento de erro
    catchError((err) => {
      console.error('Erro ao buscar endereços', err); // Log para capturar o erro
      return of([]); // Retorna um array vazio se houver erro
    })
  );
}
}