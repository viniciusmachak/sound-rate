import { ChangeDetectionStrategy, Component, DestroyRef, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BehaviorSubject, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AlbumDetails } from '../../models/album-details.model';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { AlbumReview } from '../../models/review.model';
import { AudioService } from '../../services/audio.service';
import { CommonModule, DecimalPipe, SlicePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StarRatingComponent } from '../../components/star-rating/star-rating.component';
import { ReviewDialogComponent } from '../../components/review-dialog/review-dialog.component';
import { ReviewListComponent } from '../../components/review-list/review-list.component';
import { FormatDurationPipe } from '../../pipes/format-duration.pipe';
import { RatingRequest } from '../../models/rating.model';

@Component({
  selector: 'app-album-details-page',
  standalone: true,
  imports: [
    CommonModule, RouterLink, MatCardModule, MatTabsModule, MatIconModule, MatListModule,
    StarRatingComponent, MatSnackBarModule, MatButtonModule, MatDialogModule,
    ReviewListComponent, MatProgressSpinnerModule, FormatDurationPipe,
    DecimalPipe, SlicePipe
  ],
  templateUrl: './album-details-page.component.html',
  styleUrl: './album-details-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlbumDetailsPageComponent implements OnInit {
  private albumDetailsSubject = new BehaviorSubject<AlbumDetails | null>(null);
  private trackRatingsById = new Map<string, number>();
  albumDetails$ = this.albumDetailsSubject.asObservable();
  albumId!: string;

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    private snackBar: MatSnackBar,
    public authService: AuthService,
    public audioService: AudioService,
    private dialog: MatDialog,
    private destroyRef: DestroyRef
  ) { }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      switchMap(params => {
        const id = params.get('id');
        if (!id) throw new Error('Album ID not found');
        this.albumId = id;
        return this.apiService.getAlbumDetails(this.albumId);
      })
    ).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(details => this.setAlbumDetails(details));
  }

  loadAlbumDetails(): void {
    this.apiService.getAlbumDetails(this.albumId).subscribe(details => {
      this.setAlbumDetails(details);
    });
  }

  toggleLike(): void {
    const currentDetails = this.albumDetailsSubject.getValue();
    if (!currentDetails) return;

    const isCurrentlyLiked = currentDetails.isLikedByCurrentUser;

    this.albumDetailsSubject.next({
      ...currentDetails,
      isLikedByCurrentUser: !isCurrentlyLiked,
      likesCount: isCurrentlyLiked ? currentDetails.likesCount - 1 : currentDetails.likesCount + 1
    });

    const apiCall = isCurrentlyLiked
      ? this.apiService.unlikeAlbum(this.albumId)
      : this.apiService.likeAlbum(this.albumId);

    apiCall.subscribe({
      error: () => {
        this.albumDetailsSubject.next(currentDetails);
        this.snackBar.open('Error updating like status.', 'Close', { duration: 3000 });
      }
    });
  }

  toggleListenLater(): void {
    const currentDetails = this.albumDetailsSubject.getValue();
    if (!currentDetails) return;

    const isOnList = currentDetails.isOnListenLaterList;
    const apiCall = isOnList ? this.apiService.removeFromListenLater(this.albumId) : this.apiService.addToListenLater(this.albumId);

    this.albumDetailsSubject.next({
      ...currentDetails,
      isOnListenLaterList: !isOnList,
    });

    apiCall.subscribe({
      error: () => {
        this.albumDetailsSubject.next(currentDetails);
        this.snackBar.open('Error updating your Listen Later list.', 'Close', { duration: 3000 });
      }
    });
  }

  onAlbumRatingChanged(newRating: number | null): void {
    const currentDetails = this.albumDetailsSubject.getValue();
    if (!currentDetails) return;

    const oldRating = currentDetails.currentUserRating;

    this.albumDetailsSubject.next({
      ...currentDetails,
      currentUserRating: newRating,
    });

    if (newRating === null) {
      this.apiService.deleteRating(this.albumId).subscribe({
        next: () => {
          this.snackBar.open('Rating removed successfully!', 'Close', { duration: 3000 });
          this.loadAlbumDetails();
        },
        error: (err) => {
          this.albumDetailsSubject.next({ ...currentDetails, currentUserRating: oldRating });
          this.snackBar.open('Error removing your rating.', 'Close', { duration: 3000 });
        }
      });
    } else {
      const ratingDto: RatingRequest = { albumId: this.albumId, rating: newRating };
      this.apiService.rateAlbumOrTrack(ratingDto).subscribe({
        next: () => {
          this.snackBar.open('Your rating has been saved!', 'Close', { duration: 3000 });
          this.loadAlbumDetails();
        },
        error: (err) => {
          this.albumDetailsSubject.next({ ...currentDetails, currentUserRating: oldRating });
          this.snackBar.open('Error saving your rating.', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onTrackRatingChanged(newRating: number | null, trackId: string): void {
    const currentDetails = this.albumDetailsSubject.getValue();
    if (!currentDetails) return;

    if (newRating === null) {
      this.apiService.deleteRating(undefined, trackId).subscribe({
        next: () => {
          this.snackBar.open('Track rating removed!', 'Close', { duration: 2000 });
          this.loadAlbumDetails();
        },
        error: (err) => {
          this.snackBar.open('Error removing track rating.', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.apiService.rateAlbumOrTrack({ albumId: this.albumId, trackId: trackId, rating: newRating }).subscribe({
        next: () => {
          this.snackBar.open('Track rating saved!', 'Close', { duration: 2000 });
          this.loadAlbumDetails();
        },
        error: (err) => this.snackBar.open('Error saving track rating.', 'Close', { duration: 3000 })
      });
    }
  }

  openReviewDialog(reviewToEdit?: AlbumReview): void {
    const currentDetails = this.albumDetailsSubject.getValue();
    if (!currentDetails) return;

    if (!currentDetails.currentUserRating || currentDetails.currentUserRating === 0) {
      this.snackBar.open('You must rate the album before writing a review.', 'Close', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(ReviewDialogComponent, {
      width: '600px',
      panelClass: 'review-dialog-container',
      data: {
        existingText: reviewToEdit?.text,
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && result.text !== undefined) {

        const request = {
          albumId: this.albumId,
          text: result.text,
          rating: currentDetails.currentUserRating!
        };

        const apiCall = reviewToEdit ? this.apiService.updateReview(reviewToEdit.id, request) : this.apiService.createReview(request);

        apiCall.subscribe({
          next: () => {
            this.snackBar.open('Review saved successfully!', 'Close', { duration: 3000 });
            this.loadAlbumDetails();
          },
          error: (err) => this.snackBar.open('Error saving the review.', 'Close', { duration: 3000 })
        });
      }
    });
  }

  onDeleteReview(reviewId: number): void {
    this.apiService.deleteReview(reviewId).subscribe({
      next: () => {
        this.snackBar.open('Review deleted successfully!', 'Close', { duration: 3000 });
        this.loadAlbumDetails();
      },
      error: (err) => this.snackBar.open('Error deleting the review.', 'Close', { duration: 3000 })
    });
  }

  getAlbumCover(albumDetails: AlbumDetails | null): string {
    return albumDetails?.deezerDetails.cover_xl
      || albumDetails?.deezerDetails?.cover_medium
      || 'https://placehold.co/300x300?text=No+Image';
  }

  getUserRatingForTrack(trackId: string): number {
    return this.trackRatingsById.get(trackId) ?? 0;
  }

  private setAlbumDetails(details: AlbumDetails): void {
    this.trackRatingsById = new Map(
      details.currentUserTrackRatings.map(rating => [rating.trackId, rating.rating])
    );
    this.albumDetailsSubject.next(details);
  }
}
