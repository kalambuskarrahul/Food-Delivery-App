import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private auth = inject(AuthService);
  private cart = inject(CartService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toast = inject(ToastService);

  email = '';
  password = '';
  loading = signal(false);

  demoAccounts = [
    { label: 'Customer', email: 'customer@demo.com', password: 'Demo@1234' },
    { label: 'Restaurant owner', email: 'owner@demo.com', password: 'Demo@1234' },
    { label: 'Delivery partner', email: 'rider@demo.com', password: 'Demo@1234' },
    { label: 'Admin', email: 'admin@fooddelivery.com', password: 'Admin@12345' },
  ];

  fill(account: { email: string; password: string }): void {
    this.email = account.email;
    this.password = account.password;
  }

  submit(): void {
    if (!this.email.trim() || !this.password) {
      this.toast.error('Please enter your email and password');
      return;
    }
    this.loading.set(true);
    this.auth.login(this.email.trim(), this.password).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.user.role === 'CUSTOMER') {
          this.cart.load();
        }
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        this.router.navigateByUrl(returnUrl && returnUrl.startsWith('/') ? returnUrl : this.auth.homeRoute());
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.fromError(err);
      },
    });
  }
}
