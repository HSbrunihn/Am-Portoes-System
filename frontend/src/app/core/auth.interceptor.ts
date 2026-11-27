
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth.service';

function isApiRequest(url: string): boolean {
  const baseUrl = environment.apiUrl.replace(/\/$/, '');

  return url === baseUrl || url.startsWith(`${baseUrl}/`);
}

function isAuthRequest(url: string): boolean {
  return /\/auth\/(?:login|cadastro|me|logout)(?:\?|$)/.test(url);
}

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const auth = inject(AuthService);

  const apiRequest = isApiRequest(request.url);
  const token = auth.getToken();

  let outgoingRequest = request;

  if (
    apiRequest &&
    token &&
    !request.headers.has('Authorization')
  ) {
    outgoingRequest = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(outgoingRequest).pipe(
    catchError(error => {
      if (
        apiRequest &&
        error.status === 401 &&
        !isAuthRequest(request.url)
      ) {
        auth.clearSession();

        void router.navigate(['/login'], {
          queryParams: {
            returnUrl: router.url
          }
        });
      }

      return throwError(() => error);
    })
  );
};
