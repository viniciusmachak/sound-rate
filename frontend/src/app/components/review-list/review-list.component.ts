import { Component, Input, Output, EventEmitter } from '@angular/core';
import { AlbumReview } from '../../models/review.model';
import { StarRatingComponent } from '../../components/star-rating/star-rating.component';
import { User } from '../../models/user.model';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { ApiService } from '../../services/api.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-review-list',
  standalone: true,
  imports: [
    CommonModule, MatButtonModule, MatIconModule, MatMenuModule,
    MatSnackBarModule, RouterLink, StarRatingComponent
  ],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.css'
})
export class ReviewListComponent {
  @Input() reviews: AlbumReview[] = [];
  @Input() currentUser: User | null = null;
  @Input() deletingReviewIds: ReadonlySet<number> = new Set<number>();
  @Input() reviewMutationPending = false;
  @Output() edit = new EventEmitter<AlbumReview>();
  @Output() delete = new EventEmitter<number>();
  readonly updatingLikeIds = new Set<number>();

  constructor(
    private apiService: ApiService,
    private snackBar: MatSnackBar
  ) { }

  isAuthor(review: AlbumReview): boolean {
    return this.currentUser?.id === review.author.id;
  }

  toggleLike(review: AlbumReview): void {
    if (!this.currentUser || this.updatingLikeIds.has(review.id)) return;

    const isCurrentlyLiked = review.isLikedByCurrentUser;
    const apiCall = isCurrentlyLiked ? this.apiService.unlikeReview(review.id) : this.apiService.likeReview(review.id);
    this.updatingLikeIds.add(review.id);
    review.isLikedByCurrentUser = !isCurrentlyLiked;
    review.likesCount += isCurrentlyLiked ? -1 : 1;

    apiCall.pipe(
      finalize(() => this.updatingLikeIds.delete(review.id))
    ).subscribe({
      error: () => {
        review.isLikedByCurrentUser = isCurrentlyLiked;
        review.likesCount += isCurrentlyLiked ? 1 : -1;
        this.snackBar.open('Could not update the review like. Please try again.', 'Close', {
          duration: 3000
        });
      }
    });
  }
}
