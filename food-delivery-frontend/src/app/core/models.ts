export type Role = 'CUSTOMER' | 'RESTAURANT_OWNER' | 'DELIVERY_PARTNER' | 'ADMIN';
export type OrderStatus =
  | 'PLACED' | 'ACCEPTED' | 'REJECTED' | 'PREPARING'
  | 'READY_FOR_PICKUP' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';
export type PaymentMethod = 'COD' | 'UPI' | 'CARD' | 'WALLET';
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
export type RestaurantStatus = 'PENDING' | 'APPROVED' | 'SUSPENDED';

export interface User {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  role: Role;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: User;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Restaurant {
  id: number;
  name: string;
  description: string | null;
  cuisine: string | null;
  phone: string | null;
  address: string;
  city: string;
  pincode: string | null;
  imageUrl: string | null;
  open: boolean;
  status: RestaurantStatus;
  avgRating: number;
  createdAt: string;
}

export interface RestaurantRequest {
  name: string;
  description: string | null;
  cuisine: string | null;
  phone: string | null;
  address: string;
  city: string;
  pincode: string | null;
  imageUrl: string | null;
}

export interface MenuItem {
  id: number;
  categoryId: number | null;
  categoryName: string | null;
  name: string;
  description: string | null;
  price: number;
  veg: boolean;
  available: boolean;
  imageUrl: string | null;
}

export interface MenuItemRequest {
  categoryId: number | null;
  name: string;
  description: string | null;
  price: number;
  veg: boolean;
  available: boolean;
  imageUrl: string | null;
}

export interface MenuSection {
  categoryId: number | null;
  name: string;
  items: MenuItem[];
}

export interface Menu {
  restaurantId: number;
  restaurantName: string;
  open: boolean;
  sections: MenuSection[];
}

export interface Review {
  id: number;
  rating: number;
  comment: string | null;
  customerName: string;
  createdAt: string;
}

export interface CartItem {
  menuItemId: number;
  name: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
  veg: boolean;
  available: boolean;
  imageUrl: string | null;
}

export interface Cart {
  restaurantId: number | null;
  restaurantName: string | null;
  items: CartItem[];
  subtotal: number;
  totalItems: number;
}

export interface Address {
  id: number;
  label: string | null;
  line1: string;
  line2: string | null;
  city: string;
  state: string;
  pincode: string;
  defaultAddress: boolean;
  fullAddress: string;
}

export interface AddressRequest {
  label: string | null;
  line1: string;
  line2: string | null;
  city: string;
  state: string;
  pincode: string;
}

export interface OrderItem {
  name: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface StatusEntry {
  status: OrderStatus;
  at: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  restaurantId: number;
  restaurantName: string;
  restaurantAddress: string;
  customerName: string;
  customerPhone: string | null;
  deliveryPartnerName: string | null;
  deliveryAddress: string;
  items: OrderItem[];
  subtotal: number;
  deliveryFee: number;
  tax: number;
  total: number;
  paymentMethod: PaymentMethod | null;
  paymentStatus: PaymentStatus | null;
  cancelReason: string | null;
  timeline: StatusEntry[];
  placedAt: string;
  updatedAt: string;
}

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  role: Role;
  enabled: boolean;
  createdAt: string;
}

export interface AdminRestaurant {
  restaurant: Restaurant;
  ownerName: string;
  ownerEmail: string;
}

export interface AdminStats {
  totalUsers: number;
  totalRestaurants: number;
  pendingRestaurants: number;
  totalOrders: number;
  deliveredOrders: number;
  cancelledOrders: number;
  totalRevenue: number;
}
