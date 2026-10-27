import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Restaurant } from '../../core/models';
import { OwnerService } from '../../core/staff.service';
import { ToastService } from '../../core/toast.service';
import { StatusBadgeComponent } from '../../shared/status-badge.component';

interface RestaurantForm {
  name: string;
  description: string;
  cuisine: string;
  phone: string;
  address: string;
  city: string;
  pincode: string;
  imageUrl: string;
}

const blankForm = (): RestaurantForm => ({
  name: '', description: '', cuisine: '', phone: '', address: '', city: '', pincode: '', imageUrl: '',
});

@Component({
  selector: 'app-owner-restaurants',
  standalone: true,
  imports: [FormsModule, RouterLink, StatusBadgeComponent],
  templateUrl: './owner-restaurants.component.html',
})
export class OwnerRestaurantsComponent implements OnInit {
  private api = inject(OwnerService);
  private toast = inject(ToastService);

  restaurants = signal<Restaurant[]>([]);
  loading = signal(true);
  showForm = false;
  editingId: number | null = null;
  form: RestaurantForm = blankForm();
  cuisines = ['North Indian', 'South Indian', 'Chinese', 'Italian', 'Cafe', 'Desserts', 'Fast Food'];

  ngOnInit(): void {
    this.load();
  }

  newRestaurant(): void {
    this.editingId = null;
    this.form = blankForm();
    this.showForm = true;
  }

  edit(r: Restaurant): void {
    this.editingId = r.id;
    this.form = {
      name: r.name,
      description: r.description ?? '',
      cuisine: r.cuisine ?? '',
      phone: r.phone ?? '',
      address: r.address,
      city: r.city,
      pincode: r.pincode ?? '',
      imageUrl: r.imageUrl ?? '',
    };
    this.showForm = true;
  }

  cancel(): void {
    this.showForm = false;
    this.editingId = null;
  }

  save(): void {
    const f = this.form;
    if (!f.name.trim() || !f.address.trim() || !f.city.trim()) {
      this.toast.error('Name, address and city are required');
      return;
    }
    const body = {
      name: f.name.trim(),
      description: f.description.trim() || null,
      cuisine: f.cuisine.trim() || null,
      phone: f.phone.trim() || null,
      address: f.address.trim(),
      city: f.city.trim(),
      pincode: f.pincode.trim() || null,
      imageUrl: f.imageUrl.trim() || null,
    };
    const call = this.editingId === null
      ? this.api.createRestaurant(body)
      : this.api.updateRestaurant(this.editingId, body);
    call.subscribe({
      next: () => {
        this.toast.success(this.editingId === null ? 'Restaurant created. It will appear to customers after admin approval.' : 'Restaurant updated');
        this.cancel();
        this.load();
      },
      error: (err) => this.toast.fromError(err),
    });
  }

  toggleOpen(r: Restaurant): void {
    this.api.setOpen(r.id, !r.open).subscribe({
      next: (updated) => this.restaurants.update((list) => list.map((x) => (x.id === updated.id ? updated : x))),
      error: (err) => this.toast.fromError(err),
    });
  }

  private load(): void {
    this.api.restaurants().subscribe({
      next: (list) => {
        this.restaurants.set(list);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.fromError(err);
      },
    });
  }
}
