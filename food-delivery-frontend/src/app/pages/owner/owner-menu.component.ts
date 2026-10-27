import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { Menu, MenuItem, MenuSection } from '../../core/models';
import { OwnerService } from '../../core/staff.service';
import { ToastService } from '../../core/toast.service';

interface ItemForm {
  name: string;
  description: string;
  price: number | null;
  categoryId: number | null;
  veg: boolean;
  available: boolean;
  imageUrl: string;
}

const blankItem = (): ItemForm => ({
  name: '', description: '', price: null, categoryId: null, veg: true, available: true, imageUrl: '',
});

@Component({
  selector: 'app-owner-menu',
  standalone: true,
  imports: [FormsModule, RouterLink, CurrencyPipe],
  templateUrl: './owner-menu.component.html',
})
export class OwnerMenuComponent implements OnInit {
  private api = inject(OwnerService);
  private route = inject(ActivatedRoute);
  private toast = inject(ToastService);

  restaurantId = Number(this.route.snapshot.paramMap.get('id'));
  menu = signal<Menu | null>(null);
  newCategory = '';
  showItemForm = false;
  editingItemId: number | null = null;
  form: ItemForm = blankItem();

  ngOnInit(): void {
    this.load();
  }

  categories(): MenuSection[] {
    return (this.menu()?.sections ?? []).filter((s) => s.categoryId !== null);
  }

  addCategory(): void {
    const name = this.newCategory.trim();
    if (!name) {
      return;
    }
    this.api.createCategory(this.restaurantId, name).subscribe({
      next: () => {
        this.newCategory = '';
        this.load();
      },
      error: (err) => this.toast.fromError(err),
    });
  }

  deleteCategory(section: MenuSection): void {
    if (section.categoryId === null || !confirm(`Delete category "${section.name}"? Its dishes will move to "Other".`)) {
      return;
    }
    this.api.deleteCategory(section.categoryId).subscribe({
      next: () => this.load(),
      error: (err) => this.toast.fromError(err),
    });
  }

  newItem(categoryId: number | null = null): void {
    this.editingItemId = null;
    this.form = { ...blankItem(), categoryId };
    this.showItemForm = true;
  }

  editItem(item: MenuItem): void {
    this.editingItemId = item.id;
    this.form = {
      name: item.name,
      description: item.description ?? '',
      price: item.price,
      categoryId: item.categoryId,
      veg: item.veg,
      available: item.available,
      imageUrl: item.imageUrl ?? '',
    };
    this.showItemForm = true;
  }

  cancelItem(): void {
    this.showItemForm = false;
    this.editingItemId = null;
  }

  saveItem(): void {
    const f = this.form;
    if (!f.name.trim() || f.price === null || f.price <= 0) {
      this.toast.error('Item name and a price greater than 0 are required');
      return;
    }
    const body = {
      categoryId: f.categoryId,
      name: f.name.trim(),
      description: f.description.trim() || null,
      price: f.price,
      veg: f.veg,
      available: f.available,
      imageUrl: f.imageUrl.trim() || null,
    };
    const call = this.editingItemId === null
      ? this.api.createItem(this.restaurantId, body)
      : this.api.updateItem(this.editingItemId, body);
    call.subscribe({
      next: () => {
        this.toast.success('Dish saved');
        this.cancelItem();
        this.load();
      },
      error: (err) => this.toast.fromError(err),
    });
  }

  deleteItem(item: MenuItem): void {
    if (!confirm(`Delete "${item.name}"?`)) {
      return;
    }
    this.api.deleteItem(item.id).subscribe({
      next: () => this.load(),
      error: (err) => this.toast.fromError(err),
    });
  }

  toggleAvailability(item: MenuItem): void {
    this.api.setAvailability(item.id, !item.available).subscribe({
      next: () => this.load(),
      error: (err) => this.toast.fromError(err),
    });
  }

  private load(): void {
    this.api.menu(this.restaurantId).subscribe({
      next: (m) => this.menu.set(m),
      error: (err) => this.toast.fromError(err),
    });
  }
}
