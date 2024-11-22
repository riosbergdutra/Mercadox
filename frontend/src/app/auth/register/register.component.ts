import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService, UsuarioRequestDto } from '../auth.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router'; // Importando o Router

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  registerError: boolean = false;

  constructor(
    private fb: FormBuilder, 
    private authService: AuthService,
    private router: Router // Injete o Router
  ) {
    this.registerForm = this.fb.group({
      nome: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      senha: ['', Validators.required],
      role: ['', Validators.required],
    });
  }

  onSubmit() {
    if (this.registerForm.valid) {
      const usuarioRequest: UsuarioRequestDto = this.registerForm.value;

      this.authService.register(usuarioRequest).subscribe({
        next: () => {
          console.log('Usuário registrado com sucesso!');
          this.router.navigate(['/login']); 
        },
        error: () => {
          this.registerError = true;
        },
      });
    }
  }
}
