import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { Menu, MenuItem, Restaurant, Review } from '../../core/models';
import { RestaurantService } from '../../core/restaurant.service';
import { ToastService } from '../../core/toast.service';
import { cuisineEmoji } from '../../core/util';

@Component({
  selector: 'app-restaurant-detail',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './restaurant-detail.component.html',
})
export class RestaurantDetailComponent implements OnInit {
  private api = inject(RestaurantService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private toast = inject(ToastService);
  auth = inject(AuthService);
  cart = inject(CartService);

  restaurant = signal<Restaurant | null>(null);
  menu = signal<Menu | null>(null);
  reviews = signal<Review[]>([]);
  loading = signal(true);
  emoji = cuisineEmoji;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.get(id).subscribe({
      next: (r) => {
        this.restaurant.set(r);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.fromError(err);
        this.router.navigate(['/']);
      },
    });
    this.api.menu(id).subscribe({ next: (m) => this.menu.set(m), error: (err) => this.toast.fromError(err) });
    this.api.reviews(id).subscribe({ next: (p) => this.reviews.set(p.content) });
  }

  inc(item: MenuItem): void {
    const role = this.auth.role();
    if (role === null) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    if (role !== 'CUSTOMER') {
      this.toast.info('Only customer accounts can place orders');
      return;
    }
    this.cart.add(item.id, 1).subscribe({ error: (err: HttpErrorResponse) => this.onAddError(err, item) });
  }

  dec(item: MenuItem): void {
    const qty = this.cart.qty(item.id);
    const call = qty <= 1 ? this.cart.remove(item.id) : this.cart.setQuantity(item.id, qty - 1);
    call.subscribe({ error: (err) => this.toast.fromError(err) });
  }

  stars(rating: number): string {
    return '★'.repeat(rating) + '☆'.repeat(5 - rating);
  }

  private onAddError(err: HttpErrorResponse, item: MenuItem): void {
    if (err.status !== 409) {
      this.toast.fromError(err);
      return;
    }
    const text = (err.error?.message ?? 'Your cart has items from another restaurant.') + '\n\nClear your cart and add this item?';
    if (confirm(text)) {
      this.cart.clear().pipe(switchMap(() => this.cart.add(item.id, 1))).subscribe({
        error: (e) => this.toast.fromError(e),
      });
    }
  }
}
