import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService, UsuarioRequestDto } from '../auth.service';
import { CommonModule } from '@angular/common'; // Adicione esta linha

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // Adicione o CommonModule aqui
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'] // Corrigido de 'styleUrl' para 'styleUrls'
})
export class RegisterComponent {
  registerForm: FormGroup;
  isStepOne: boolean = true;
  isVendedor: boolean = false;

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.registerForm = this.fb.group({
      nome: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      senha: ['', Validators.required],
      role: ['', Validators.required],
      endereco: this.fb.group({
        rua: ['', Validators.required],
        numero: ['', Validators.required],
        cidade: ['', Validators.required],
        estado: ['', Validators.required],
        cep: ['', Validators.required],
      }),
      imagem: [null],
    });
  }

  onRoleChange(): void {
    const selectedRole = this.registerForm.get('role')?.value;
    this.isVendedor = selectedRole === 'VENDEDOR';
  }

  nextStep(): void {
    if (this.registerForm.get('nome')?.valid && 
        this.registerForm.get('email')?.valid && 
        this.registerForm.get('senha')?.valid && 
        this.registerForm.get('role')?.valid) {
      this.isStepOne = false;
    }
  }

  prevStep(): void {
    this.isStepOne = true; // Retorna para a primeira etapa
  }

  onSubmit() {
    if (this.registerForm.valid) {
      const formData = new FormData();
  
      // Adiciona os campos do formulário
      formData.append('nome', this.registerForm.get('nome')?.value);
      formData.append('email', this.registerForm.get('email')?.value);
      formData.append('senha', this.registerForm.get('senha')?.value);
      formData.append('role', this.registerForm.get('role')?.value);
  
      // Adiciona os campos do endereço
      const endereco = this.registerForm.get('endereco');
      if (endereco) {
        formData.append('endereco.rua', endereco.get('rua')?.value);
        formData.append('endereco.numero', endereco.get('numero')?.value);
        formData.append('endereco.cidade', endereco.get('cidade')?.value);
        formData.append('endereco.estado', endereco.get('estado')?.value);
        formData.append('endereco.cep', endereco.get('cep')?.value);
      }
  
      // Adiciona a imagem se existir
      const imagem = this.registerForm.get('imagem')?.value;
      if (imagem) {
        formData.append('imagem', imagem);
      }
  
      this.authService.register(formData).subscribe({
        next: () => {
          console.log('Usuário registrado com sucesso!');
        },
        error: (err) => {
          console.error('Erro ao registrar usuário:', err);
        },
      });
    }
  }
  
  

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    this.registerForm.patchValue({ imagem: file });
  }
}
