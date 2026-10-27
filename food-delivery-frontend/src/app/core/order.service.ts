import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_URL } from './config';
import { Address, AddressRequest, Order, Page, PaymentMethod, Review } from './models';
import { toParams } from './util';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);

  place(body: { addressId: number | null; deliveryAddress?: string; paymentMethod: PaymentMethod }): Observable<Order> {
    return this.http.post<Order>(`${API_URL}/orders`, body);
  }

  mine(page = 0): Observable<Page<Order>> {
    return this.http.get<Page<Order>>(`${API_URL}/orders`, { params: toParams({ page, size: 10 }) });
  }

  get(id: number): Observable<Order> {
    return this.http.get<Order>(`${API_URL}/orders/${id}`);
  }

  cancel(id: number, reason?: string): Observable<Order> {
    return this.http.post<Order>(`${API_URL}/orders/${id}/cancel`, null, { params: toParams({ reason }) });
  }

  review(id: number, rating: number, comment: string): Observable<Review> {
    return this.http.post<Review>(`${API_URL}/orders/${id}/review`, { rating, comment });
  }
}

@Injectable({ providedIn: 'root' })
export class AddressService {
  private http = inject(HttpClient);

  list(): Observable<Address[]> {
    return this.http.get<Address[]>(`${API_URL}/addresses`);
  }

  create(body: AddressRequest): Observable<Address> {
    return this.http.post<Address>(`${API_URL}/addresses`, body);
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/addresses/${id}`);
  }
}
