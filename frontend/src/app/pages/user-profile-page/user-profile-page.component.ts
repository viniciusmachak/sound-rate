import { Component, DestroyRef, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BehaviorSubject, distinctUntilChanged, filter, forkJoin, map, Observable, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { UserProfile } from '../../models/user-profile.model';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { UserRating } from '../../models/user-rating.model';
import { DeezerAlbum } from '../../models/deezer.model';
import { PageEvent, MatPaginatorModule } from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ReviewDisplayDialogComponent } from '../../components/review-display-dialog/review-display-dialog.component';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { AlbumCardComponent } from '../../components/album-card/album-card.component';
import { StarRatingComponent } from '../../components/star-rating/star-rating.component';
import { UserListComponent } from '../../components/user-list/user-list.component';

@Component({
  selector: 'app-user-profile-page',
  standalone: true,
  imports: [
    CommonModule, RouterLink, MatCardModule, MatTabsModule, MatIconModule,
    AlbumCardComponent, StarRatingComponent, MatProgressSpinnerModule, MatButtonModule,
    MatPaginatorModule, MatDialogModule
  ],
  templateUrl: './user-profile-page.component.html',
  styleUrl: './user-profile-page.component.css'
})
export class UserProfilePageComponent implements OnInit {
  private profileSubject = new BehaviorSubject<UserProfile | null>(null);
  userProfile$ = this.profileSubject.asObservable();
  private username!: string;
  isOwnProfile: boolean = false;

  ratingsSubject = new BehaviorSubject<UserRating[]>([]);
  ratings$ = this.ratingsSubject.asObservable();
  totalRatings = 0;
  ratingsPageSize = 12;
  currentRatingsPage = 0;
  isLoadingRatings = true;

  likedAlbumsSubject = new BehaviorSubject<DeezerAlbum[]>([]);
  likedAlbums$ = this.likedAlbumsSubject.asObservable();
  totalLikedAlbums = 0;
  likedAlbumsPageSize = 12;
  currentLikedAlbumsPage = 0;
  isLoadingLikedAlbums = true;

  listenLaterSubject = new BehaviorSubject<DeezerAlbum[]>([]);
  listenLater$ = this.listenLaterSubject.asObservable();
  totalListenLater = 0;
  listenLaterPageSize = 12;
  currentListenLaterPage = 0;
  isLoadingListenLater = true;

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    public authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private destroyRef: DestroyRef
  ) { }

  ngOnInit(): void {
    this.route.paramMap.pipe(
      map(params => params.get('username')),
      filter((username): username is string => !!username),
      distinctUntilChanged(),
      tap(username => {
        this.username = username;
        this.resetFeeds();
      }),
      switchMap(username => this.apiService.getUserProfile(username).pipe(
        tap(profile => {
          this.profileSubject.next(profile);
          this.isOwnProfile = this.authService.currentUserValue?.username === profile.user.username;
        }),
        switchMap(profile => forkJoin({
          profile: of(profile),
          ratings: this.apiService.getRatedAlbums(username, 0, this.ratingsPageSize),
          likedAlbums: this.apiService.getLikedAlbums(username, 0, this.likedAlbumsPageSize),
          listenLater: this.isOwnProfile
            ? this.apiService.getListenLaterList(0, this.listenLaterPageSize)
            : of(null)
        }))
      )),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ({ ratings, likedAlbums, listenLater }) => {
        this.ratingsSubject.next(ratings.content);
        this.totalRatings = ratings.totalElements;
        this.isLoadingRatings = false;

        this.likedAlbumsSubject.next(likedAlbums.content);
        this.totalLikedAlbums = likedAlbums.totalElements;
        this.isLoadingLikedAlbums = false;

        if (listenLater) {
          this.listenLaterSubject.next(listenLater.content);
          this.totalListenLater = listenLater.totalElements;
        }
        this.isLoadingListenLater = false;
      },
      error: () => {
        this.isLoadingRatings = false;
        this.isLoadingLikedAlbums = false;
        this.isLoadingListenLater = false;
        this.snackBar.open('Error loading this profile.', 'Close', { duration: 3000 });
      }
    });
  }

  toggleFollow(): void {
    const currentProfile = this.profileSubject.getValue();
    if (!currentProfile || this.isOwnProfile) return;

    const isCurrentlyFollowed = currentProfile.isFollowedByCurrentUser;
    const apiCall = isCurrentlyFollowed
      ? this.apiService.unfollowUser(this.username)
      : this.apiService.followUser(this.username);

    this.profileSubject.next({
      ...currentProfile,
      isFollowedByCurrentUser: !isCurrentlyFollowed,
      followersCount: isCurrentlyFollowed ? currentProfile.followersCount - 1 : currentProfile.followersCount + 1
    });

    apiCall.subscribe({
      error: () => {
        this.profileSubject.next(currentProfile);
      }
    });
  }

  unlikeAlbum(albumId: number, event: MouseEvent): void {
    event.stopPropagation();
    event.preventDefault();
    const currentLikedAlbums = this.likedAlbumsSubject.getValue();
    const updatedAlbums = currentLikedAlbums.filter(album => album.id !== albumId);
    this.likedAlbumsSubject.next(updatedAlbums);
    this.totalLikedAlbums--;

    this.apiService.unlikeAlbum(albumId.toString()).subscribe({
      next: () => {
        this.snackBar.open('Album removed from likes.', 'Close', { duration: 2000 });
      },
      error: () => {
        this.likedAlbumsSubject.next(currentLikedAlbums);
        this.totalLikedAlbums++;
        this.snackBar.open('Error removing like. Please try again.', 'Close', { duration: 3000 });
      }
    });
  }

  removeFromListenLater(albumId: number, event: MouseEvent): void {
    event.stopPropagation();
    event.preventDefault();

    const currentList = this.listenLaterSubject.getValue();
    const updatedList = currentList.filter(album => album.id !== albumId);
    this.listenLaterSubject.next(updatedList);
    this.totalListenLater--;

    if (updatedList.length === 0 && this.currentListenLaterPage > 0) {
      this.currentListenLaterPage--;
      this.loadListenLater();
    }

    this.apiService.removeFromListenLater(albumId.toString()).subscribe({
      next: () => {
        this.snackBar.open('Album removed from Listen Later list.', 'Close', { duration: 2000 });
      },
      error: () => {
        this.listenLaterSubject.next(currentList);
        this.totalListenLater++;
        this.snackBar.open('Error removing album. Please try again.', 'Close', { duration: 3000 });
      }
    });
  }

  openFollowListDialog(listType: 'followers' | 'following'): void {
    this.dialog.open(UserListComponent, {
      width: '400px',
      height: '60vh',
      panelClass: 'user-list-dialog',
      data: {
        username: this.username,
        listType: listType
      }
    });
  }

  loadRatedAlbums(): void {
    this.isLoadingRatings = true;
    this.apiService.getRatedAlbums(this.username, this.currentRatingsPage, this.ratingsPageSize)
      .subscribe(page => {
        this.ratingsSubject.next(page.content);
        this.totalRatings = page.totalElements;
        this.isLoadingRatings = false;
      });
  }

  loadLikedAlbums(): void {
    this.isLoadingLikedAlbums = true;
    this.apiService.getLikedAlbums(this.username, this.currentLikedAlbumsPage, this.likedAlbumsPageSize)
      .subscribe(page => {
        this.likedAlbumsSubject.next(page.content);
        this.totalLikedAlbums = page.totalElements;
        this.isLoadingLikedAlbums = false;
      });
  }

  loadListenLater(): void {
    this.isLoadingListenLater = true;
    this.apiService.getListenLaterList(this.currentListenLaterPage, this.listenLaterPageSize)
      .subscribe(page => {
        this.listenLaterSubject.next(page.content);
        this.totalListenLater = page.totalElements;
        this.isLoadingListenLater = false;
      });
  }

  showReview(rating: UserRating): void {
    if (!rating.reviewText) return;

    this.dialog.open(ReviewDisplayDialogComponent, {
      width: '600px',
      panelClass: 'review-dialog-container',
      data: {
        albumTitle: rating.album.title,
        reviewText: rating.reviewText
      }
    });
  }

  onRatingsPageChange(event: PageEvent): void {
    this.currentRatingsPage = event.pageIndex;
    this.ratingsPageSize = event.pageSize;
    this.loadRatedAlbums();
  }

  onLikedAlbumsPageChange(event: PageEvent): void {
    this.currentLikedAlbumsPage = event.pageIndex;
    this.likedAlbumsPageSize = event.pageSize;
    this.loadLikedAlbums();
  }

  onListenLaterPageChange(event: PageEvent): void {
    this.currentListenLaterPage = event.pageIndex;
    this.listenLaterPageSize = event.pageSize;
    this.loadListenLater();
  }

  private resetFeeds(): void {
    this.profileSubject.next(null);
    this.ratingsSubject.next([]);
    this.likedAlbumsSubject.next([]);
    this.listenLaterSubject.next([]);
    this.totalRatings = 0;
    this.totalLikedAlbums = 0;
    this.totalListenLater = 0;
    this.currentRatingsPage = 0;
    this.currentLikedAlbumsPage = 0;
    this.currentListenLaterPage = 0;
    this.isLoadingRatings = true;
    this.isLoadingLikedAlbums = true;
    this.isLoadingListenLater = true;
  }
}
