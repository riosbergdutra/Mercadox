import { Component, OnInit } from '@angular/core';
import { Endereco } from '../models/Endereco';
import { EnderecoService } from '../services/enderecoService/endereco.service';
import { AuthService } from '../auth/auth.service'; // Certifique-se de importar o AuthService
import { take } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-endereco',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './endereco.component.html',
  styleUrl: './endereco.component.css'
})
export class EnderecoComponent implements OnInit {
  enderecos: Endereco[] = [];
  loading = true;

  constructor(
    private enderecoService: EnderecoService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    console.log('[EnderecoComponent] Inicializando componente...');
    this.authService.getUserId().pipe(take(1)).subscribe({
      next: (idUsuario) => {
        console.log(`[EnderecoComponent] User ID: ${idUsuario}`);
        if (idUsuario) {
          this.loadEnderecos(idUsuario);
        } else {
          console.warn('[EnderecoComponent] User ID não encontrado.');
          this.loading = false;
        }
      },
      error: (err) => {
        console.error('[EnderecoComponent] Erro ao obter User ID:', err);
        this.loading = false;
      },
    });
  }
  
  private loadEnderecos(idUsuario: string): void {
    console.log(`[EnderecoComponent] Carregando endereços para User ID: ${idUsuario}`);
    this.enderecoService.getEnderecos(idUsuario).subscribe({
      next: (enderecos) => {
        console.log('[EnderecoComponent] Endereços carregados:', enderecos);
        this.enderecos = enderecos;
        this.loading = false;
      },
      error: (err) => {
        console.error('[EnderecoComponent] Erro ao carregar endereços:', err);
        this.loading = false;
      },
    });
  }
  
}
