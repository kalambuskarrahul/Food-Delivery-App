import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Order, Page } from '../../core/models';
import { OrderService } from '../../core/order.service';
import { ToastService } from '../../core/toast.service';
import { OrderCardComponent } from '../../shared/order-card.component';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [RouterLink, OrderCardComponent],
  templateUrl: './orders.component.html',
})
export class OrdersComponent implements OnInit {
  private api = inject(OrderService);
  private toast = inject(ToastService);

  result = signal<Page<Order> | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    this.load(0);
  }

  load(page: number): void {
    this.loading.set(true);
    this.api.mine(page).subscribe({
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

  pages(): number[] {
    return Array.from({ length: this.result()?.totalPages ?? 0 }, (_, i) => i);
  }
}
