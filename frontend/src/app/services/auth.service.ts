
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import {
  catchError,
  map,
  Observable,
  of,
  tap,
  throwError
} from 'rxjs';

import { environment } from '../../environments/environment';
import { Usuario } from '../models/api.model';

interface LoginResponse {
  token: string;
  tipo: string;
  expiraEmSegundos: number;
  usuario: Usuario;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);

  private readonly TOKEN_KEY = 'am_portoes_token';

  readonly user = signal<Usuario | null>(null);

  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  clearSession(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
    this.user.set(null);
  }

  login(email: string, senha: string): Observable<Usuario> {
    return this.http
      .post<LoginResponse>(
        `${environment.apiUrl}/auth/login`,
        { email, senha }
      )
      .pipe(
        tap(response => {
          sessionStorage.setItem(
            this.TOKEN_KEY,
            response.token
          );

          this.user.set(response.usuario);
        }),
        map(response => response.usuario)
      );
  }

  restore(): Observable<Usuario | null> {
    const token = this.getToken();

    if (!token) {
      this.user.set(null);
      return of(null);
    }

    return this.http
      .get<Usuario>(
        `${environment.apiUrl}/auth/me`
      )
      .pipe(
        tap(usuario => {
          this.user.set(usuario);
        }),
        catchError((error: HttpErrorResponse) => {
          if (error.status === 401) {
            this.clearSession();
            return of(null);
          }

          return throwError(() => error);
        })
      );
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(
        `${environment.apiUrl}/auth/logout`,
        {}
      )
      .pipe(
        tap(() => {
          this.clearSession();
        }),
        catchError(error => {
          this.clearSession();
          return throwError(() => error);
        })
      );
  }
}
