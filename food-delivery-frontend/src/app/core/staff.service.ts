import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_URL } from './config';
import {
  AdminRestaurant, AdminStats, AdminUser, Menu, MenuItem, MenuItemRequest, MenuSection, Order, OrderStatus,
  Page, Restaurant, RestaurantRequest, RestaurantStatus,
} from './models';
import { toParams } from './util';

/** Restaurant owner endpoints. */
@Injectable({ providedIn: 'root' })
export class OwnerService {
  private http = inject(HttpClient);
  private base = `${API_URL}/owner`;

  restaurants(): Observable<Restaurant[]> {
    return this.http.get<Restaurant[]>(`${this.base}/restaurants`);
  }
  createRestaurant(body: RestaurantRequest): Observable<Restaurant> {
    return this.http.post<Restaurant>(`${this.base}/restaurants`, body);
  }
  updateRestaurant(id: number, body: RestaurantRequest): Observable<Restaurant> {
    return this.http.put<Restaurant>(`${this.base}/restaurants/${id}`, body);
  }
  setOpen(id: number, open: boolean): Observable<Restaurant> {
    return this.http.patch<Restaurant>(`${this.base}/restaurants/${id}/open`, null, { params: toParams({ open }) });
  }

  menu(restaurantId: number): Observable<Menu> {
    return this.http.get<Menu>(`${this.base}/restaurants/${restaurantId}/menu`);
  }
  createCategory(restaurantId: number, name: string): Observable<MenuSection> {
    return this.http.post<MenuSection>(`${this.base}/restaurants/${restaurantId}/categories`, { name });
  }
  deleteCategory(categoryId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/categories/${categoryId}`);
  }
  createItem(restaurantId: number, body: MenuItemRequest): Observable<MenuItem> {
    return this.http.post<MenuItem>(`${this.base}/restaurants/${restaurantId}/menu-items`, body);
  }
  updateItem(itemId: number, body: MenuItemRequest): Observable<MenuItem> {
    return this.http.put<MenuItem>(`${this.base}/menu-items/${itemId}`, body);
  }
  deleteItem(itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/menu-items/${itemId}`);
  }
  setAvailability(itemId: number, available: boolean): Observable<MenuItem> {
    return this.http.patch<MenuItem>(`${this.base}/menu-items/${itemId}/availability`, null, { params: toParams({ available }) });
  }

  orders(status: OrderStatus | '', page = 0): Observable<Page<Order>> {
    return this.http.get<Page<Order>>(`${this.base}/orders`, { params: toParams({ status, page, size: 20 }) });
  }
  updateOrderStatus(orderId: number, status: OrderStatus, reason?: string): Observable<Order> {
    return this.http.patch<Order>(`${this.base}/orders/${orderId}/status`, { status, reason });
  }
}

/** Delivery partner endpoints. */
@Injectable({ providedIn: 'root' })
export class DeliveryService {
  private http = inject(HttpClient);
  private base = `${API_URL}/delivery`;

  availability(): Observable<{ available: boolean }> {
    return this.http.get<{ available: boolean }>(`${this.base}/availability`);
  }
  setAvailability(available: boolean): Observable<{ available: boolean }> {
    return this.http.put<{ available: boolean }>(`${this.base}/availability`, null, { params: toParams({ available }) });
  }
  availableOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.base}/available-orders`);
  }
  claim(orderId: number): Observable<Order> {
    return this.http.post<Order>(`${this.base}/orders/${orderId}/claim`, null);
  }
  updateStatus(orderId: number, status: OrderStatus): Observable<Order> {
    return this.http.patch<Order>(`${this.base}/orders/${orderId}/status`, { status });
  }
  myOrders(): Observable<Page<Order>> {
    return this.http.get<Page<Order>>(`${this.base}/orders`, { params: toParams({ size: 20 }) });
  }
}

/** Admin endpoints. */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private base = `${API_URL}/admin`;

  stats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.base}/stats`);
  }
  users(): Observable<Page<AdminUser>> {
    return this.http.get<Page<AdminUser>>(`${this.base}/users`, { params: toParams({ size: 50 }) });
  }
  setUserEnabled(id: number, enabled: boolean): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.base}/users/${id}/enabled`, null, { params: toParams({ enabled }) });
  }
  restaurants(status: RestaurantStatus | ''): Observable<Page<AdminRestaurant>> {
    return this.http.get<Page<AdminRestaurant>>(`${this.base}/restaurants`, { params: toParams({ status, size: 50 }) });
  }
  setRestaurantStatus(id: number, status: RestaurantStatus): Observable<Restaurant> {
    return this.http.patch<Restaurant>(`${this.base}/restaurants/${id}/status`, null, { params: toParams({ status }) });
  }
  orders(status: OrderStatus | ''): Observable<Page<Order>> {
    return this.http.get<Page<Order>>(`${this.base}/orders`, { params: toParams({ status, size: 50 }) });
  }
}
