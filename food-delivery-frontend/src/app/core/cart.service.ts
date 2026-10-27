import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { API_URL } from './config';
import { Cart } from './models';

const EMPTY: Cart = { restaurantId: null, restaurantName: null, items: [], subtotal: 0, totalItems: 0 };

/** Holds the customer's cart so the navbar badge and all pages stay in sync. */
@Injectable({ providedIn: 'root' })
export class CartService {
  private http = inject(HttpClient);

  readonly cart = signal<Cart>(EMPTY);
  readonly count = computed(() => this.cart().totalItems);

  load(): void {
    this.http.get<Cart>(`${API_URL}/cart`).subscribe({
      next: (c) => this.cart.set(c),
      error: () => this.cart.set(EMPTY),
    });
  }

  reset(): void {
    this.cart.set(EMPTY);
  }

  qty(menuItemId: number): number {
    return this.cart().items.find((i) => i.menuItemId === menuItemId)?.quantity ?? 0;
  }

  add(menuItemId: number, quantity = 1): Observable<Cart> {
    return this.http.post<Cart>(`${API_URL}/cart/items`, { menuItemId, quantity }).pipe(tap((c) => this.cart.set(c)));
  }

  setQuantity(menuItemId: number, quantity: number): Observable<Cart> {
    return this.http.put<Cart>(`${API_URL}/cart/items/${menuItemId}`, { quantity }).pipe(tap((c) => this.cart.set(c)));
  }

  remove(menuItemId: number): Observable<Cart> {
    return this.http.delete<Cart>(`${API_URL}/cart/items/${menuItemId}`).pipe(tap((c) => this.cart.set(c)));
  }

  clear(): Observable<void> {
    return this.http.delete<void>(`${API_URL}/cart`).pipe(tap(() => this.reset()));
  }
}
