
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main>
      <section class="hero">
        <div>
          <b>AM</b>
          <h1>Gestão que abre caminhos.</h1>
          <p>Organize tarefas, produtos e orçamentos em um só lugar.</p>
        </div>
      </section>

      <section class="area">
        <form [formGroup]="form" (ngSubmit)="submit()">
          <span>ACESSO AO SISTEMA</span>
          <h2>Bem-vindo</h2>
          <p>Informe suas credenciais para continuar.</p>

          @if (error()) {
            <div class="alert">{{ error() }}</div>
          }

          <label>
            E-mail
            <input
              type="email"
              formControlName="email"
              autocomplete="username"
              placeholder="voce@empresa.com.br"
            >

            @if (form.controls.email.touched && form.controls.email.invalid) {
              <small>Informe um e-mail válido.</small>
            }
          </label>

          <label>
            Senha
            <input
              type="password"
              formControlName="senha"
              autocomplete="current-password"
              placeholder="Sua senha"
            >

            @if (form.controls.senha.touched && form.controls.senha.invalid) {
              <small>A senha é obrigatória.</small>
            }
          </label>

          <button [disabled]="loading()">
            {{ loading() ? 'Entrando…' : 'Entrar' }}
          </button>

          <p class="alternar">Ainda não tem conta? <a routerLink="/cadastro">Criar conta</a></p>

          <footer>AM Portões • Sistema de gestão</footer>
        </form>
      </section>
    </main>
  `,
  styleUrl: './login.scss'
})

export class LoginPage {

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly error = signal('');

  readonly form = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email]
    }),

    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    })
  });

  submit(): void {

    if (this.loading()) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.auth.login(
      this.form.controls.email.value.trim(),
      this.form.controls.senha.value
    ).subscribe({

      next: () => {
        const requested = this.route.snapshot.queryParamMap.get('returnUrl');

        const destination =
          requested?.startsWith('/') && !requested.startsWith('//')
            ? requested
            : '/dashboard';

        void this.router.navigateByUrl(destination);
      },

      error: (e: HttpErrorResponse) => {
        this.loading.set(false);

        const body = e.error as {
          message?: string;
          mensagem?: string;
        } | null;

        this.error.set(
          e.status === 401
            ? 'E-mail ou senha inválidos.'
            : body?.mensagem ??
              body?.message ??
              'Não foi possível conectar ao servidor. Tente novamente.'
        );
      }

    });
  }
}
