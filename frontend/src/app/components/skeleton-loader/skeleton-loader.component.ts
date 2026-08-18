import { Component, Input } from '@angular/core';

export type SkeletonVariant = 'cards' | 'detail' | 'profile' | 'list';

@Component({
  selector: 'app-skeleton-loader',
  standalone: true,
  templateUrl: './skeleton-loader.component.html',
  styleUrl: './skeleton-loader.component.css'
})
export class SkeletonLoaderComponent {
  @Input() variant: SkeletonVariant = 'cards';
  @Input() count = 6;

  get items(): number[] {
    return Array.from({ length: this.count }, (_, index) => index);
  }
}
