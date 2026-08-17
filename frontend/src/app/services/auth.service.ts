import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, Observable, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Usuario } from '../models/api.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  readonly user = signal<Usuario | null>(null);

  login(email: string, senha: string): Observable<Usuario> {
    if (environment.mockAuth) {
      const usuarioFake: Usuario = { id: 1, nome: 'Usuário Demo', email };
      this.user.set(usuarioFake);
      return of(usuarioFake);
    }

    return this.http
      .post<Usuario>(`${environment.apiUrl}/auth/login`, { email, senha })
      .pipe(tap(u => this.user.set(u)));
  }

  restore(): Observable<Usuario | null> {
    if (environment.mockAuth) {
      return of(this.user());
    }

    return this.http
      .get<Usuario>(`${environment.apiUrl}/auth/me`)
      .pipe(
        tap(u => this.user.set(u)),
        catchError(() => of(null))
      );
  }

  logout(): Observable<void> {
    if (environment.mockAuth) {
      this.user.set(null);
      return of(undefined);
    }

    return this.http
      .post<void>(`${environment.apiUrl}/auth/logout`, {})
      .pipe(tap(() => this.user.set(null)));
  }
}