import { Component, Input } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { Order } from '../core/models';
import { StatusBadgeComponent } from './status-badge.component';

/** One order summary. Buttons for each role are projected into the card with <ng-content>. */
@Component({
  selector: 'app-order-card',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe, StatusBadgeComponent],
  templateUrl: './order-card.component.html',
})
export class OrderCardComponent {
  @Input({ required: true }) order!: Order;
  @Input() showCustomer = false;

  summary(): string {
    return this.order.items.map((i) => `${i.quantity} x ${i.name}`).join(', ');
  }
}
