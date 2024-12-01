import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { Observable, of, take } from 'rxjs';
import { Endereco } from '../models/Endereco';
import { EnderecoService } from '../services/enderecoService/endereco.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, MatBadgeModule, MatIconModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  cartItemCount = 0;
  isModalOpen = false;
  enderecos: Endereco[] = [];
  idUsuario$: Observable<string | null>;
  isAuthenticated$: Observable<boolean>;

  constructor(
    private authService: AuthService,
    private router: Router,
    private enderecoService: EnderecoService
  ) {
    this.isAuthenticated$ = this.authService.authenticated$;
    this.idUsuario$ = this.authService.getUserId();
  }

  ngOnInit(): void {
    console.log('[HeaderComponent] Inicialização do componente.');
    this.isAuthenticated$.pipe(take(1)).subscribe((isAuthenticated) => {
      console.log(`[HeaderComponent] Usuário autenticado: ${isAuthenticated}`);
      if (isAuthenticated) {
        this.loadEnderecos();
      }
    });
  }
  
  private loadEnderecos(): void {
    console.log('[HeaderComponent] Carregando endereços...');
    this.idUsuario$.pipe(take(1)).subscribe((idUsuario) => {
      console.log(`[HeaderComponent] User ID: ${idUsuario}`);
      if (idUsuario) {
        this.enderecoService.getEnderecos(idUsuario).subscribe({
          next: (enderecos) => {
            console.log('[HeaderComponent] Endereços carregados:', enderecos);
            this.enderecos = enderecos;
          },
          error: (err) => {
            console.error('[HeaderComponent] Erro ao carregar endereços:', err);
          },
        });
      } else {
        console.warn('[HeaderComponent] User ID não encontrado.');
      }
    });
  }
  
  openModal() {
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
  }

  goToAccount() {
    this.router.navigateByUrl('/minha-conta');
  }

  goToLogin() {
    this.router.navigateByUrl('/login');
  }

  goToOrders() {
    this.isAuthenticated$.pipe(take(1)).subscribe((isAuthenticated) => {
      if (isAuthenticated) {
        this.router.navigateByUrl('/pedidos');
      } else {
        this.router.navigateByUrl('/login');
      }
    });
  }
}