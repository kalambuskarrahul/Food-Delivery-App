import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, finalize, shareReplay, tap } from 'rxjs';

import { API_URL } from './config';
import { AuthResponse, Role, User } from './models';

const KEY_USER = 'fd_user';
const KEY_ACCESS = 'fd_access';
const KEY_REFRESH = 'fd_refresh';

export interface RegisterBody {
  name: string;
  email: string;
  phone: string | null;
  password: string;
  role: Role;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private refreshing$: Observable<AuthResponse> | null = null;

  readonly user = signal<User | null>(this.readUser());
  readonly isLoggedIn = computed(() => this.user() !== null);
  readonly role = computed<Role | null>(() => this.user()?.role ?? null);

  get accessToken(): string | null {
    return localStorage.getItem(KEY_ACCESS);
  }

  get refreshToken(): string | null {
    return localStorage.getItem(KEY_REFRESH);
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, { email, password }).pipe(tap((r) => this.store(r)));
  }

  register(body: RegisterBody): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/register`, body).pipe(tap((r) => this.store(r)));
  }

  /** Several requests may fail with 401 at once; they all share one refresh call. */
  refreshOnce(): Observable<AuthResponse> {
    if (!this.refreshing$) {
      this.refreshing$ = this.http
        .post<AuthResponse>(`${API_URL}/auth/refresh`, { refreshToken: this.refreshToken })
        .pipe(
          tap((r) => this.store(r)),
          finalize(() => (this.refreshing$ = null)),
          shareReplay(1),
        );
    }
    return this.refreshing$;
  }

  logout(): void {
    localStorage.removeItem(KEY_USER);
    localStorage.removeItem(KEY_ACCESS);
    localStorage.removeItem(KEY_REFRESH);
    this.user.set(null);
    this.router.navigate(['/login']);
  }

  /** Where each role lands after login. */
  homeRoute(): string {
    switch (this.role()) {
      case 'RESTAURANT_OWNER': return '/owner/restaurants';
      case 'DELIVERY_PARTNER': return '/delivery';
      case 'ADMIN': return '/admin';
      default: return '/';
    }
  }

  private store(r: AuthResponse): void {
    localStorage.setItem(KEY_USER, JSON.stringify(r.user));
    localStorage.setItem(KEY_ACCESS, r.accessToken);
    localStorage.setItem(KEY_REFRESH, r.refreshToken);
    this.user.set(r.user);
  }

  private readUser(): User | null {
    try {
      const raw = localStorage.getItem(KEY_USER);
      return raw ? (JSON.parse(raw) as User) : null;
    } catch {
      return null;
    }
  }
}
