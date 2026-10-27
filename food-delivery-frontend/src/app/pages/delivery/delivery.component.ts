import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { interval } from 'rxjs';

import { Order, OrderStatus } from '../../core/models';
import { DeliveryService } from '../../core/staff.service';
import { ToastService } from '../../core/toast.service';
import { OrderCardComponent } from '../../shared/order-card.component';

@Component({
  selector: 'app-delivery',
  standalone: true,
  imports: [OrderCardComponent],
  templateUrl: './delivery.component.html',
})
export class DeliveryComponent implements OnInit {
  private api = inject(DeliveryService);
  private toast = inject(ToastService);

  online = signal(false);
  tab = signal<'available' | 'mine'>('available');
  available = signal<Order[]>([]);
  mine = signal<Order[]>([]);

  constructor() {
    interval(15000).pipe(takeUntilDestroyed()).subscribe(() => this.refresh());
  }

  ngOnInit(): void {
    this.api.availability().subscribe({ next: (a) => this.online.set(a.available) });
    this.refresh();
  }

  refresh(): void {
    this.api.availableOrders().subscribe({ next: (list) => this.available.set(list) });
    this.api.myOrders().subscribe({ next: (page) => this.mine.set(page.content) });
  }

  toggleOnline(): void {
    this.api.setAvailability(!this.online()).subscribe({
      next: (a) => {
        this.online.set(a.available);
        this.toast.info(a.available ? 'You are online and can accept deliveries' : 'You are offline');
      },
      error: (err) => this.toast.fromError(err),
    });
  }

  claim(order: Order): void {
    this.api.claim(order.id).subscribe({
      next: () => {
        this.toast.success('Order #' + order.orderNumber + ' is yours. Pick it up from the restaurant.');
        this.tab.set('mine');
        this.refresh();
      },
      error: (err) => {
        this.toast.fromError(err);
        this.refresh();
      },
    });
  }

  setStatus(order: Order, status: OrderStatus): void {
    this.api.updateStatus(order.id, status).subscribe({
      next: () => {
        this.toast.success(status === 'DELIVERED' ? 'Great job! Order delivered.' : 'Delivery started');
        this.refresh();
      },
      error: (err) => this.toast.fromError(err),
    });
  }
}
