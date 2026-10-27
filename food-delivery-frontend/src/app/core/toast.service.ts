import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  text: string;
  type: 'success' | 'error' | 'info';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private nextId = 1;

  success(text: string): void { this.show(text, 'success'); }
  info(text: string): void { this.show(text, 'info'); }
  error(text: string): void { this.show(text, 'error'); }

  /** Turns an HTTP error from the backend into a readable message. */
  fromError(err: any): void {
    const body = err?.error;
    let text: string;
    if (err?.status === 0) {
      text = 'Cannot reach the server. Is the Spring Boot backend running on port 8080?';
    } else {
      text = body?.message ?? 'Something went wrong. Please try again.';
      if (body?.fieldErrors) {
        text += ': ' + Object.values(body.fieldErrors).join(', ');
      }
    }
    this.show(text, 'error');
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }

  private show(text: string, type: Toast['type']): void {
    const id = this.nextId++;
    this.toasts.update((list) => [...list, { id, text, type }]);
    setTimeout(() => this.dismiss(id), 5000);
  }
}
