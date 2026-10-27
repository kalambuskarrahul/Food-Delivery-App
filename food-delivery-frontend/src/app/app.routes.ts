import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent) },
  { path: 'login', loadComponent: () => import('./pages/auth/login.component').then((m) => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./pages/auth/register.component').then((m) => m.RegisterComponent) },
  {
    path: 'restaurants/:id',
    loadComponent: () => import('./pages/restaurant/restaurant-detail.component').then((m) => m.RestaurantDetailComponent),
  },

  // customer
  {
    path: 'cart',
    canActivate: [roleGuard('CUSTOMER')],
    loadComponent: () => import('./pages/cart/cart.component').then((m) => m.CartComponent),
  },
  {
    path: 'orders',
    canActivate: [roleGuard('CUSTOMER')],
    loadComponent: () => import('./pages/orders/orders.component').then((m) => m.OrdersComponent),
  },
  {
    path: 'orders/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/orders/order-detail.component').then((m) => m.OrderDetailComponent),
  },

  // restaurant owner
  {
    path: 'owner/restaurants',
    canActivate: [roleGuard('RESTAURANT_OWNER')],
    loadComponent: () => import('./pages/owner/owner-restaurants.component').then((m) => m.OwnerRestaurantsComponent),
  },
  {
    path: 'owner/restaurants/:id/menu',
    canActivate: [roleGuard('RESTAURANT_OWNER')],
    loadComponent: () => import('./pages/owner/owner-menu.component').then((m) => m.OwnerMenuComponent),
  },
  {
    path: 'owner/orders',
    canActivate: [roleGuard('RESTAURANT_OWNER')],
    loadComponent: () => import('./pages/owner/owner-orders.component').then((m) => m.OwnerOrdersComponent),
  },

  // delivery partner and admin
  {
    path: 'delivery',
    canActivate: [roleGuard('DELIVERY_PARTNER')],
    loadComponent: () => import('./pages/delivery/delivery.component').then((m) => m.DeliveryComponent),
  },
  {
    path: 'admin',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () => import('./pages/admin/admin.component').then((m) => m.AdminComponent),
  },

  { path: '**', redirectTo: '' },
];
