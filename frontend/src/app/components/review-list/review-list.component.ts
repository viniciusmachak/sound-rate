import { Component, Input, Output, EventEmitter } from '@angular/core';
import { AlbumReview } from '../../models/review.model';
import { StarRatingComponent } from '../../components/star-rating/star-rating.component';
import { User } from '../../models/user.model';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-review-list',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatMenuModule, RouterLink, StarRatingComponent],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.css'
})
export class ReviewListComponent {
  @Input() reviews: AlbumReview[] = [];
  @Input() currentUser: User | null = null;
  @Output() edit = new EventEmitter<AlbumReview>();
  @Output() delete = new EventEmitter<number>();
  @Output() ratingChanged = new EventEmitter<number | null>();

  constructor(private apiService: ApiService) { }

  isAuthor(review: AlbumReview): boolean {
    return this.currentUser?.id === review.author.id;
  }

  onRatingChange(newRating: number | null): void {
    this.ratingChanged.emit(newRating);
  }

  toggleLike(review: AlbumReview): void {
    if (!this.currentUser) return;

    const isCurrentlyLiked = review.isLikedByCurrentUser;
    const apiCall = isCurrentlyLiked ? this.apiService.unlikeReview(review.id) : this.apiService.likeReview(review.id);
    review.isLikedByCurrentUser = !isCurrentlyLiked;
    review.likesCount += isCurrentlyLiked ? -1 : 1;

    apiCall.subscribe({ error: () => { review.isLikedByCurrentUser = isCurrentlyLiked; review.likesCount += isCurrentlyLiked ? 1 : -1; } });
  }
}
