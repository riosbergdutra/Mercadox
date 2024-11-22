import { Component } from '@angular/core';
import { Endereco } from '../models/Endereco';
import { EnderecoService } from '../services/enderecoService/endereco.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth/auth.service'; // Certifique-se de importar o AuthService
import { take } from 'rxjs';

@Component({
  selector: 'app-endereco',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './endereco.component.html',
  styleUrl: './endereco.component.css'
})
export class EnderecoComponent {
  enderecos: Endereco[] = [];
  loading: boolean = true; // Controle para mostrar um carregando

  constructor(
    private enderecoService: EnderecoService,
    private authService: AuthService // Injete o AuthService
  ) {}

  ngOnInit(): void {
    // Obter o userId com o AuthService
    this.authService.getUserId().pipe(take(1)).subscribe({
      next: (userId) => {
        if (userId) {
          console.log('User ID encontrado:', userId); // Adicionando log para verificar o ID
          this.enderecoService.getEnderecos(userId).subscribe({
            next: (enderecos) => {
              this.enderecos = enderecos;
              this.loading = false;
            },
            error: (err) => {
              console.error('Erro ao carregar endereços:', err);
              this.loading = false;
            }
          });
        } else {
          console.error('User ID não encontrado');
          this.loading = false;
        }
      },
      error: (err) => {
        console.error('Erro ao obter User ID:', err);
        this.loading = false;
      }
    });
  }
}
