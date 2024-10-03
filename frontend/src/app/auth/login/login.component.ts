import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService, LoginRequest } from '../auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';  // Import CommonModule

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // Adicione ReactiveFormsModule aqui
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  loginError: boolean = false;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    // Inicializando o formulário
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onLogin(): void {
    if (this.loginForm.valid) {
      const loginRequest: LoginRequest = this.loginForm.value;

      this.authService.login(loginRequest).subscribe({
        next: () => {
          // Redireciona para a página principal ou dashboard
          this.router.navigate(['/']);
        },
        error: () => {
          // Exibe uma mensagem de erro se o login falhar
          this.loginError = true;
        }
      });
    }
  }
}
