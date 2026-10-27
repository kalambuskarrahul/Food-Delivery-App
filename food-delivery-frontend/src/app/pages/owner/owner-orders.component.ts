import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { interval } from 'rxjs';

import { Order, OrderStatus, Page } from '../../core/models';
import { OwnerService } from '../../core/staff.service';
import { ToastService } from '../../core/toast.service';
import { OrderCardComponent } from '../../shared/order-card.component';

@Component({
  selector: 'app-owner-orders',
  standalone: true,
  imports: [FormsModule, OrderCardComponent],
  templateUrl: './owner-orders.component.html',
})
export class OwnerOrdersComponent implements OnInit {
  private api = inject(OwnerService);
  private toast = inject(ToastService);

  filter: OrderStatus | '' = '';
  result = signal<Page<Order> | null>(null);
  loading = signal(true);

  constructor() {
    // pick up new orders without needing to refresh the page
    interval(15000).pipe(takeUntilDestroyed()).subscribe(() => this.load());
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.orders(this.filter).subscribe({
      next: (res) => {
        this.result.set(res);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.fromError(err);
      },
    });
  }

  setStatus(order: Order, status: OrderStatus): void {
    let reason: string | undefined;
    if (status === 'REJECTED') {
      const answer = prompt('Reason for rejecting this order (optional):');
      if (answer === null) {
        return;
      }
      reason = answer;
    }
    this.api.updateOrderStatus(order.id, status, reason).subscribe({
      next: (updated) => {
        this.result.update((page) => page && { ...page, content: page.content.map((o) => (o.id === updated.id ? updated : o)) });
        this.toast.success('Order #' + updated.orderNumber + ' is now ' + status.replace(/_/g, ' ').toLowerCase());
      },
      error: (err) => this.toast.fromError(err),
    });
  }
}
