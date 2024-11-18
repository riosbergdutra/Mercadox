import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';
import {MatBadgeModule} from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { Observable, of, take } from 'rxjs';
import { Endereco } from '../models/Endereco';
import { EnderecoService } from '../enderecoService/endereco.service';




@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, MatBadgeModule, MatIconModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  cartItemCount = 0;
  isModalOpen = false;
  enderecos: Endereco[] = [];
  userId: string = '';  
  isAuthenticated$: Observable<boolean>;
  constructor(private authService: AuthService,  private router: Router, private enderecoService: EnderecoService) {
    this.isAuthenticated$ = this.authService.isAuthenticated();
  }

  addToCart() {
    this.cartItemCount++;
  }

  ngOnInit(): void {
    // Verificar se o usuário está autenticado e carregar o ID
    this.isAuthenticated$.subscribe((isAuthenticated) => {
      if (isAuthenticated) {
        // Busca o ID do usuário do token JWT
        this.userId = this.authService.getUserId() ?? '';
        if (this.userId) {
          this.loadEnderecos();
        }
      }
    });
  }
// Método para carregar os endereços do usuário
  // Método para carregar os endereços do usuário
  loadEnderecos(): void {
    if (this.userId) {
      this.enderecoService.getEnderecos(this.userId).subscribe({
        next: (enderecos) => {
          this.enderecos = enderecos;
        },
        error: (err) => {
          console.error('Erro ao carregar endereços', err);
        }
      });
    }
  }


  openModal() {
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
  }

  goToAccount() {
    // Navegar para a página "Minha Conta"
    this.router.navigateByUrl('/minha-conta')
  }

  // Redirecionar para a página de login
  goToLogin() {
    // Navegar para a página de login
    this.router.navigateByUrl('/login');
  }

  // Redirecionar para a página de pedidos ou login
  goToOrders() {
    this.isAuthenticated$.pipe(take(1)).subscribe((isAuthenticated) => {
      if (isAuthenticated) {
        this.router.navigateByUrl('/pedidos'); // Página de pedidos
      } else {
        this.router.navigateByUrl('/login'); // Página de login
      }
    });
}
}