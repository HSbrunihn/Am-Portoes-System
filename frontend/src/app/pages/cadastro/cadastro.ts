import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

// A confirmação precisa ser igual à senha
function senhasIguais(grupo: AbstractControl): ValidationErrors | null {
  const senha = grupo.get('senha')?.value;
  const confirmacao = grupo.get('confirmacao')?.value;
  return senha && confirmacao && senha !== confirmacao ? { senhasDiferentes: true } : null;
}

@Component({
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main>
      <section class="hero">
        <div>
          <b>AM</b>
          <h1>Gestão que abre caminhos.</h1>
          <p>Crie sua conta e comece a organizar tarefas, produtos e orçamentos.</p>
        </div>
      </section>

      <section class="area">
        <form [formGroup]="form" (ngSubmit)="submit()">
          <span>NOVA CONTA</span>
          <h2>Criar conta</h2>
          <p>Preencha seus dados para acessar o sistema.</p>

          @if (error()) {
            <div class="alert">{{ error() }}</div>
          }

          <label>
            Nome
            <input type="text" formControlName="nome" autocomplete="name" placeholder="Seu nome">
            @if (form.controls.nome.touched && form.controls.nome.invalid) {
              <small>O nome é obrigatório.</small>
            }
          </label>

          <label>
            E-mail
            <input type="email" formControlName="email" autocomplete="email" placeholder="voce@empresa.com.br">
            @if (form.controls.email.touched && form.controls.email.invalid) {
              <small>Informe um e-mail válido.</small>
            }
          </label>

          <label>
            Senha
            <input type="password" formControlName="senha" autocomplete="new-password" placeholder="Mínimo de 6 caracteres">
            @if (form.controls.senha.touched && form.controls.senha.invalid) {
              <small>A senha deve ter no mínimo 6 caracteres.</small>
            }
          </label>

          <label>
            Confirmar senha
            <input type="password" formControlName="confirmacao" autocomplete="new-password" placeholder="Repita a senha">
            @if (form.controls.confirmacao.touched && form.hasError('senhasDiferentes')) {
              <small>As senhas não conferem.</small>
            }
          </label>

          <button [disabled]="loading()">
            {{ loading() ? 'Criando conta…' : 'Criar conta' }}
          </button>

          <p class="alternar">Já tem conta? <a routerLink="/login">Entrar</a></p>

          <footer>AM Portões • Sistema de gestão</footer>
        </form>
      </section>
    </main>
  `,
  styleUrl: '../login/login.scss'
})
export class CadastroPage {

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal('');

  readonly form = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email]
    }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6)]
    }),
    confirmacao: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    })
  }, { validators: senhasIguais });

  submit(): void {
    if (this.loading()) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');

    const { nome, email, senha } = this.form.getRawValue();

    this.auth.cadastrar(nome.trim(), email.trim(), senha).subscribe({
      next: () => void this.router.navigateByUrl('/dashboard'),

      error: (e: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(this.mensagemDeErro(e));
      }
    });
  }

  // 409 = e-mail já cadastrado; 400 = validação do back-end (campos); 0/5xx = API fora do ar
  private mensagemDeErro(e: HttpErrorResponse): string {
    if (e.status === 0 || e.status >= 500) {
      return 'Não foi possível conectar ao servidor. Tente novamente.';
    }

    const body = e.error as { mensagem?: string; campos?: Record<string, string> } | null;
    if (body?.campos) return Object.values(body.campos).join(' ');
    return body?.mensagem ?? 'Não foi possível criar a conta. Revise os dados.';
  }
}
