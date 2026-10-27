import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../core/auth.service';
import { AdminRestaurant, AdminStats, AdminUser, Order, OrderStatus, RestaurantStatus } from '../../core/models';
import { AdminService } from '../../core/staff.service';
import { ToastService } from '../../core/toast.service';
import { OrderCardComponent } from '../../shared/order-card.component';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

type Tab = 'restaurants' | 'users' | 'orders';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, DatePipe, OrderCardComponent, StatusBadgeComponent],
  templateUrl: './admin.component.html',
})
export class AdminComponent implements OnInit {
  private api = inject(AdminService);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  tab = signal<Tab>('restaurants');
  stats = signal<AdminStats | null>(null);
  restaurants = signal<AdminRestaurant[]>([]);
  users = signal<AdminUser[]>([]);
  orders = signal<Order[]>([]);

  restaurantFilter: RestaurantStatus | '' = '';
  orderFilter: OrderStatus | '' = '';

  ngOnInit(): void {
    this.loadStats();
    this.loadRestaurants();
  }

  select(tab: Tab): void {
    this.tab.set(tab);
    if (tab === 'restaurants') this.loadRestaurants();
    if (tab === 'users') this.loadUsers();
    if (tab === 'orders') this.loadOrders();
  }

  loadStats(): void {
    this.api.stats().subscribe({ next: (s) => this.stats.set(s), error: (err) => this.toast.fromError(err) });
  }

  loadRestaurants(): void {
    this.api.restaurants(this.restaurantFilter).subscribe({
      next: (p) => this.restaurants.set(p.content),
      error: (err) => this.toast.fromError(err),
    });
  }

  loadUsers(): void {
    this.api.users().subscribe({ next: (p) => this.users.set(p.content), error: (err) => this.toast.fromError(err) });
  }

  loadOrders(): void {
    this.api.orders(this.orderFilter).subscribe({ next: (p) => this.orders.set(p.content), error: (err) => this.toast.fromError(err) });
  }

  setRestaurantStatus(item: AdminRestaurant, status: RestaurantStatus): void {
    this.api.setRestaurantStatus(item.restaurant.id, status).subscribe({
      next: () => {
        this.toast.success(item.restaurant.name + ' is now ' + status.toLowerCase());
        this.loadRestaurants();
        this.loadStats();
      },
      error: (err) => this.toast.fromError(err),
    });
  }

  toggleUser(user: AdminUser): void {
    this.api.setUserEnabled(user.id, !user.enabled).subscribe({
      next: (updated) => this.users.update((list) => list.map((u) => (u.id === updated.id ? updated : u))),
      error: (err) => this.toast.fromError(err),
    });
  }
}
