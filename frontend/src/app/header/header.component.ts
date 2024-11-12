import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  isModalOpen = false;
  isAuthenticated: boolean = false;

  constructor(private authService: AuthService,  private router: Router) {}

  ngOnInit(): void {
    // Verificar se o usuário está logado ao inicializar o componente
    this.isAuthenticated = this.authService.isAuthenticated();
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
}
