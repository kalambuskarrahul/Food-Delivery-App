import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

import { API_URL } from './config';
import { AuthService } from './auth.service';

/** Adds the JWT to API calls and silently renews it once when the server answers 401. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const isApi = req.url.startsWith(API_URL);
  const isAuthCall = req.url.startsWith(`${API_URL}/auth/`);
  const token = auth.accessToken;

  const authReq = isApi && !isAuthCall && token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && isApi && !isAuthCall) {
        if (auth.refreshToken) {
          return auth.refreshOnce().pipe(
            switchMap(() => next(req.clone({ setHeaders: { Authorization: `Bearer ${auth.accessToken}` } }))),
            catchError((refreshErr) => {
              auth.logout();
              return throwError(() => refreshErr);
            }),
          );
        }
        auth.logout();
      }
      return throwError(() => err);
    }),
  );
};
