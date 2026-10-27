import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth.service';
import { CartService } from '../../core/cart.service';
import { Role } from '../../core/models';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private auth = inject(AuthService);
  private cart = inject(CartService);
  private router = inject(Router);
  private toast = inject(ToastService);

  name = '';
  email = '';
  phone = '';
  password = '';
  role: Role = 'CUSTOMER';
  loading = signal(false);

  submit(): void {
    if (!this.name.trim() || !this.email.trim() || !this.password) {
      this.toast.error('Name, email and password are required');
      return;
    }
    this.loading.set(true);
    this.auth.register({
      name: this.name.trim(),
      email: this.email.trim(),
      phone: this.phone.trim() || null,
      password: this.password,
      role: this.role,
    }).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.toast.success('Account created. Welcome, ' + res.user.name + '!');
        if (res.user.role === 'CUSTOMER') {
          this.cart.load();
        }
        this.router.navigateByUrl(this.auth.homeRoute());
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.fromError(err);
      },
    });
  }
}
