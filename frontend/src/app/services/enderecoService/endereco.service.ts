import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, of, switchMap } from 'rxjs';
import { Endereco } from '../../models/Endereco';
import { AuthService } from '../../auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class EnderecoService {
  private apiUrl = 'http://localhost:8082/endereco';

  constructor(private http: HttpClient, private authService: AuthService) {}

  getEnderecos(userId: string): Observable<Endereco[]> {
    return this.authService.getUserId().pipe(
      catchError((err) => {
        console.error('Erro ao obter ID do usuário', err);
        return of(null);  // Retorna null ou algum valor padrão em caso de erro ao pegar o userId
      }),
      switchMap((userId) => {
        if (userId) {
          console.log('Obtendo endereços para o usuário com ID:', userId);
          const token = this.authService.getAcessToken();
          let headers = new HttpHeaders();

          if (token) {
            headers = headers.set('Authorization', `Bearer ${token}`);
          } else {
            console.warn('Token de acesso não encontrado');
          }

          return this.http.get<Endereco[]>(`${this.apiUrl}/${userId}`, { headers }).pipe(
            catchError((err) => {
              console.error('Erro ao buscar endereços', err);
              return of([]); // Retorna um array vazio em caso de erro
            })
          );
        } else {
          console.error('User ID não encontrado');
          return of([]); // Retorna um array vazio se não encontrar o userId
        }
      })
    );
  }
}
