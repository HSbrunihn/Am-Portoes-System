import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

function safeReturnUrl(value: string | null): string {
  return value?.startsWith('/') && !value.startsWith('//')
    ? value
    : '/dashboard';
}

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.user()) return true;

  return auth.restore().pipe(
    map((user) => user ? true : router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    })),
    catchError(() => of(router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    }))),
  );
};

export const guestGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const destination = safeReturnUrl(route.queryParamMap.get('returnUrl'));
  const redirect = () => router.parseUrl(destination);

  if (auth.user()) return redirect();

  return auth.restore().pipe(
    map((user) => user ? redirect() : true),
    catchError(() => of(true)),
  );
};
