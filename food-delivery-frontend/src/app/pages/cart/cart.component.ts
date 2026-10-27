import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { CartService } from '../../core/cart.service';
import { Address, CartItem, PaymentMethod } from '../../core/models';
import { AddressService, OrderService } from '../../core/order.service';
import { ToastService } from '../../core/toast.service';

// Must match app.order.* in the backend application.yml (the server calculates the real amounts)
const FREE_DELIVERY_ABOVE = 500;
const DELIVERY_FEE = 40;
const TAX_PERCENT = 5;

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [FormsModule, RouterLink, CurrencyPipe],
  templateUrl: './cart.component.html',
})
export class CartComponent implements OnInit {
  private addressApi = inject(AddressService);
  private orderApi = inject(OrderService);
  private router = inject(Router);
  private toast = inject(ToastService);
  cartService = inject(CartService);

  addresses = signal<Address[]>([]);
  selectedAddressId: number | null = null;
  showNewAddress = false;
  payment: PaymentMethod = 'COD';
  placing = signal(false);

  newAddress = { label: 'Home', line1: '', line2: '', city: '', state: '', pincode: '' };

  paymentOptions: { value: PaymentMethod; label: string }[] = [
    { value: 'COD', label: 'Cash on delivery' },
    { value: 'UPI', label: 'UPI (simulated)' },
    { value: 'CARD', label: 'Credit / debit card (simulated)' },
    { value: 'WALLET', label: 'Wallet (simulated)' },
  ];

  ngOnInit(): void {
    this.cartService.load();
    this.addressApi.list().subscribe({
      next: (list) => {
        this.addresses.set(list);
        this.selectedAddressId = list.find((a) => a.defaultAddress)?.id ?? list[0]?.id ?? null;
        this.showNewAddress = list.length === 0;
      },
      error: (err) => this.toast.fromError(err),
    });
  }

  subtotal(): number {
    return this.cartService.cart().subtotal;
  }
  deliveryFee(): number {
    return this.subtotal() >= FREE_DELIVERY_ABOVE ? 0 : DELIVERY_FEE;
  }
  tax(): number {
    return Math.round(this.subtotal() * TAX_PERCENT) / 100;
  }
  total(): number {
    return this.subtotal() + this.deliveryFee() + this.tax();
  }

  changeQty(item: CartItem, delta: number): void {
    const qty = item.quantity + delta;
    const call = qty <= 0 ? this.cartService.remove(item.menuItemId) : this.cartService.setQuantity(item.menuItemId, qty);
    call.subscribe({ error: (err) => this.toast.fromError(err) });
  }

  clearCart(): void {
    if (confirm('Remove all items from your cart?')) {
      this.cartService.clear().subscribe({ error: (err) => this.toast.fromError(err) });
    }
  }

  saveAddress(): void {
    const a = this.newAddress;
    if (!a.line1.trim() || !a.city.trim() || !a.state.trim() || !a.pincode.trim()) {
      this.toast.error('Please fill address line 1, city, state and pincode');
      return;
    }
    this.addressApi.create({
      label: a.label.trim() || null,
      line1: a.line1.trim(),
      line2: a.line2.trim() || null,
      city: a.city.trim(),
      state: a.state.trim(),
      pincode: a.pincode.trim(),
    }).subscribe({
      next: (created) => {
        this.addresses.update((list) => [created, ...list]);
        this.selectedAddressId = created.id;
        this.showNewAddress = false;
        this.newAddress = { label: 'Home', line1: '', line2: '', city: '', state: '', pincode: '' };
        this.toast.success('Address saved');
      },
      error: (err) => this.toast.fromError(err),
    });
  }

  placeOrder(): void {
    if (this.selectedAddressId === null) {
      this.toast.error('Please select or add a delivery address');
      return;
    }
    this.placing.set(true);
    this.orderApi.place({ addressId: this.selectedAddressId, paymentMethod: this.payment }).subscribe({
      next: (order) => {
        this.placing.set(false);
        this.cartService.reset();
        this.toast.success('Order placed! Order #' + order.orderNumber);
        this.router.navigate(['/orders', order.id]);
      },
      error: (err) => {
        this.placing.set(false);
        this.toast.fromError(err);
      },
    });
  }
}
