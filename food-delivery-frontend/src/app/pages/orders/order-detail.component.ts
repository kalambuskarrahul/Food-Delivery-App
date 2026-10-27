import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { interval } from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { Order } from '../../core/models';
import { OrderService } from '../../core/order.service';
import { ToastService } from '../../core/toast.service';
import { ORDER_STEPS, isFinalStatus, statusLabel } from '../../core/util';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [FormsModule, RouterLink, CurrencyPipe, DatePipe, StatusBadgeComponent],
  templateUrl: './order-detail.component.html',
})
export class OrderDetailComponent implements OnInit {
  private api = inject(OrderService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  private id = Number(this.route.snapshot.paramMap.get('id'));

  order = signal<Order | null>(null);
  steps = ORDER_STEPS;
  currentStep = computed(() => {
    const o = this.order();
    return o ? ORDER_STEPS.indexOf(o.status) : -1;
  });

  rating = 5;
  comment = '';
  reviewed = signal(false);

  constructor() {
    // live tracking: refresh every 10 seconds until the order is finished
    interval(10000).pipe(takeUntilDestroyed()).subscribe(() => {
      const o = this.order();
      if (o && !isFinalStatus(o.status)) {
        this.load(false);
      }
    });
  }

  ngOnInit(): void {
    this.load(true);
  }

  backLink(): string {
    switch (this.auth.role()) {
      case 'RESTAURANT_OWNER': return '/owner/orders';
      case 'DELIVERY_PARTNER': return '/delivery';
      case 'ADMIN': return '/admin';
      default: return '/orders';
    }
  }

  label(status: string): string {
    return statusLabel(status);
  }

  timeOf(step: string): string | null {
    return this.order()?.timeline.find((t) => t.status === step)?.at ?? null;
  }

  cancel(): void {
    if (!confirm('Cancel this order?')) {
      return;
    }
    this.api.cancel(this.id, 'Cancelled by customer').subscribe({
      next: (o) => {
        this.order.set(o);
        this.toast.success('Order cancelled');
      },
      error: (err) => this.toast.fromError(err),
    });
  }

  submitReview(): void {
    this.api.review(this.id, this.rating, this.comment.trim()).subscribe({
      next: () => {
        this.reviewed.set(true);
        this.toast.success('Thanks for your review!');
      },
      error: (err) => {
        if (err.status === 409) {
          this.reviewed.set(true);
        }
        this.toast.fromError(err);
      },
    });
  }

  private load(redirectOnError: boolean): void {
    this.api.get(this.id).subscribe({
      next: (o) => this.order.set(o),
      error: (err) => {
        if (redirectOnError) {
          this.toast.fromError(err);
          this.router.navigate([this.backLink()]);
        }
      },
    });
  }
}
