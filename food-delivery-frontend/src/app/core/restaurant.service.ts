import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_URL } from './config';
import { Menu, Page, Restaurant, Review } from './models';
import { toParams } from './util';

export interface RestaurantQuery {
  q?: string;
  city?: string;
  cuisine?: string;
  sort?: string;
  page?: number;
  size?: number;
}

/** Public browsing endpoints. */
@Injectable({ providedIn: 'root' })
export class RestaurantService {
  private http = inject(HttpClient);

  search(query: RestaurantQuery): Observable<Page<Restaurant>> {
    return this.http.get<Page<Restaurant>>(`${API_URL}/restaurants`, { params: toParams({ ...query }) });
  }

  get(id: number): Observable<Restaurant> {
    return this.http.get<Restaurant>(`${API_URL}/restaurants/${id}`);
  }

  menu(id: number): Observable<Menu> {
    return this.http.get<Menu>(`${API_URL}/restaurants/${id}/menu`);
  }

  reviews(id: number, page = 0): Observable<Page<Review>> {
    return this.http.get<Page<Review>>(`${API_URL}/restaurants/${id}/reviews`, { params: toParams({ page, size: 10 }) });
  }
}
