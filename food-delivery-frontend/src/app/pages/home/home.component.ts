import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Page, Restaurant } from '../../core/models';
import { RestaurantService } from '../../core/restaurant.service';
import { ToastService } from '../../core/toast.service';
import { cuisineEmoji } from '../../core/util';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [FormsModule, RouterLink, DecimalPipe],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  private api = inject(RestaurantService);
  private toast = inject(ToastService);

  q = '';
  city = '';
  cuisine = '';
  sort = 'newest';
  page = 0;

  cuisines = ['North Indian', 'South Indian', 'Chinese', 'Italian', 'Cafe', 'Desserts', 'Fast Food'];
  result = signal<Page<Restaurant> | null>(null);
  loading = signal(true);
  emoji = cuisineEmoji;

  ngOnInit(): void {
    this.load();
  }

  search(): void {
    this.page = 0;
    this.load();
  }

  clear(): void {
    this.q = '';
    this.city = '';
    this.cuisine = '';
    this.sort = 'newest';
    this.search();
  }

  go(page: number): void {
    this.page = page;
    this.load();
  }

  pages(): number[] {
    return Array.from({ length: this.result()?.totalPages ?? 0 }, (_, i) => i);
  }

  private load(): void {
    this.loading.set(true);
    this.api.search({ q: this.q, city: this.city, cuisine: this.cuisine, sort: this.sort, page: this.page, size: 9 }).subscribe({
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
}
