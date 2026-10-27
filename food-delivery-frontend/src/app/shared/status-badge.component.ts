import { Component, Input } from '@angular/core';
import { statusLabel } from '../core/util';

@Component({
  selector: 'app-status',
  standalone: true,
  template: `<span [class]="'badge b-' + value.toLowerCase()">{{ label() }}</span>`,
})
export class StatusBadgeComponent {
  @Input() value = '';

  label(): string {
    return statusLabel(this.value);
  }
}
