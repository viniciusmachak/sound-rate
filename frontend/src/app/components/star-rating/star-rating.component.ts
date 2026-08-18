import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './star-rating.component.html',
  styleUrl: './star-rating.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StarRatingComponent {
  @Input() rating: number = 0;
  @Input() readonly: boolean = false;
  @Output() ratingChange = new EventEmitter<number | null>();

  readonly stars = [1, 2, 3, 4, 5];
  hoverRating: number = 0;

  getStarIcon(star: number): string {
    const ratingToShow = this.hoverRating || this.rating;

    if (ratingToShow >= star) {
      return 'star';
    }

    if (ratingToShow >= star - 0.5) {
      return 'star_half';
    }

    return 'star_border';
  }

  rate(star: number, event: MouseEvent): void {
    if (this.readonly) return;

    const target = event.target as HTMLElement;
    const rect = target.getBoundingClientRect();
    const isHalf = (event.clientX - rect.left) < (rect.width / 2);
    const newRating = isHalf ? star - 0.5 : star;

    if (newRating === this.rating) {
      this.ratingChange.emit(null);
    } else {
      this.ratingChange.emit(newRating);
    }
  }

  setHoverRating(star: number, event: MouseEvent): void {
    if (this.readonly) return;

    const target = event.target as HTMLElement;
    const rect = target.getBoundingClientRect();
    const isHalf = (event.clientX - rect.left) < (rect.width / 2);
    this.hoverRating = isHalf ? star - 0.5 : star;
  }

  clearHoverRating(): void {
    if (this.readonly) return;
    this.hoverRating = 0;
  }
}
