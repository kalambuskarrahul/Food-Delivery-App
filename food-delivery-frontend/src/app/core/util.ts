import { HttpParams } from '@angular/common/http';
import { OrderStatus } from './models';

/** Builds query params and skips empty values. */
export function toParams(obj: Record<string, string | number | boolean | null | undefined>): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(obj)) {
    if (value !== null && value !== undefined && value !== '') {
      params = params.set(key, String(value));
    }
  }
  return params;
}

export function cuisineEmoji(cuisine: string | null | undefined): string {
  const c = (cuisine ?? '').toLowerCase();
  if (c.includes('italian') || c.includes('pizza')) return '🍕';
  if (c.includes('chinese') || c.includes('asian')) return '🥡';
  if (c.includes('south')) return '🥘';
  if (c.includes('north') || c.includes('indian')) return '🍛';
  if (c.includes('cafe') || c.includes('coffee')) return '☕';
  if (c.includes('dessert') || c.includes('sweet')) return '🍰';
  if (c.includes('burger') || c.includes('fast')) return '🍔';
  return '🍽️';
}

export function statusLabel(status: string): string {
  const text = status.replace(/_/g, ' ').toLowerCase();
  return text.charAt(0).toUpperCase() + text.slice(1);
}

/** Happy-path steps shown in the order tracker. */
export const ORDER_STEPS: OrderStatus[] = [
  'PLACED', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED',
];

export function isFinalStatus(status: OrderStatus): boolean {
  return status === 'DELIVERED' || status === 'CANCELLED' || status === 'REJECTED';
}
