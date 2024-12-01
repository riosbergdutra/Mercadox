import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of } from 'rxjs';
import { Endereco } from '../../models/Endereco';

@Injectable({
  providedIn: 'root',
})
export class EnderecoService {
  private apiUrl = 'http://localhost:8082/endereco';

  constructor(private http: HttpClient) {}

  getEnderecos(idUsuario: string): Observable<Endereco[]> {
    return this.http.get<Endereco[]>(`${this.apiUrl}/${idUsuario}`).pipe(
      catchError((err) => {
        console.error('Erro ao buscar endereços:', err);
        return of([]); // Retorna um array vazio em caso de erro
      })
    );
  }
}
