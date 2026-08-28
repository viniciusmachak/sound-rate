import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  BehaviorSubject,
  distinctUntilChanged,
  filter,
  map,
  take
} from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTabChangeEvent, MatTabsModule } from '@angular/material/tabs';
import { AlbumCardComponent } from '../../components/album-card/album-card.component';
import { ReviewDisplayDialogComponent } from '../../components/review-display-dialog/review-display-dialog.component';
import { SkeletonLoaderComponent } from '../../components/skeleton-loader/skeleton-loader.component';
import { StarRatingComponent } from '../../components/star-rating/star-rating.component';
import { UserListComponent } from '../../components/user-list/user-list.component';
import { DeezerAlbum } from '../../models/deezer.model';
import { UserActivity } from '../../models/user-activity.model';
import { UserProfile } from '../../models/user-profile.model';
import { UserRating } from '../../models/user-rating.model';
import { UserReview } from '../../models/user-review.model';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

type ContentState = 'idle' | 'loading' | 'loaded' | 'empty' | 'error';

const EMPTY_ACTIVITY: UserActivity = {
  recentRatings: [],
  recentReviews: [],
  recentLikes: [],
  recentArtists: []
};

@Component({
  selector: 'app-user-profile-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatTabsModule,
    MatIconModule,
    AlbumCardComponent,
    StarRatingComponent,
    MatButtonModule,
    MatPaginatorModule,
    MatDialogModule,
    SkeletonLoaderComponent
  ],
  templateUrl: './user-profile-page.component.html',
  styleUrl: './user-profile-page.component.css'
})
export class UserProfilePageComponent implements OnInit {
  private readonly profileSubject = new BehaviorSubject<UserProfile | null>(null);
  readonly userProfile$ = this.profileSubject.asObservable();
  private username!: string;
  profileState: ContentState = 'idle';

  isOwnProfile = false;
  followRequestPending = false;
  selectedTabIndex = 0;

  private readonly activitySubject = new BehaviorSubject<UserActivity>(EMPTY_ACTIVITY);
  readonly activity$ = this.activitySubject.asObservable();
  totalActivity = 0;
  activityRatingsState: ContentState = 'idle';
  activityReviewsState: ContentState = 'idle';
  activityLikesState: ContentState = 'idle';
  activityArtistsState: ContentState = 'idle';
  private hasLoadedActivity = false;

  private readonly ratingsSubject = new BehaviorSubject<UserRating[]>([]);
  readonly ratings$ = this.ratingsSubject.asObservable();
  totalRatings = 0;
  ratingsPageSize = 12;
  currentRatingsPage = 0;
  ratingsState: ContentState = 'idle';
  isPaginatingRatings = false;

  private readonly reviewsSubject = new BehaviorSubject<UserReview[]>([]);
  readonly reviews$ = this.reviewsSubject.asObservable();
  totalReviews = 0;
  reviewsPageSize = 8;
  currentReviewsPage = 0;
  reviewsState: ContentState = 'idle';
  isPaginatingReviews = false;

  private readonly likedAlbumsSubject = new BehaviorSubject<DeezerAlbum[]>([]);
  readonly likedAlbums$ = this.likedAlbumsSubject.asObservable();
  totalLikedAlbums = 0;
  likedAlbumsPageSize = 12;
  currentLikedAlbumsPage = 0;
  likedAlbumsState: ContentState = 'idle';
  isPaginatingLikedAlbums = false;

  private readonly listenLaterSubject = new BehaviorSubject<DeezerAlbum[]>([]);
  readonly listenLater$ = this.listenLaterSubject.asObservable();
  totalListenLater = 0;
  listenLaterPageSize = 12;
  currentListenLaterPage = 0;
  listenLaterState: ContentState = 'idle';
  isPaginatingListenLater = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly apiService: ApiService,
    public readonly authService: AuthService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly destroyRef: DestroyRef
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        map(params => params.get('username')),
        filter((username): username is string => !!username),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(username => {
        this.username = username;
        this.resetFeeds();
        this.loadProfile();
      });
  }

  loadProfile(): void {
    if (this.profileState === 'loading') return;

    const requestedUsername = this.username;
    this.profileState = 'loading';
    this.apiService.getUserProfile(requestedUsername)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: profile => {
          if (requestedUsername !== this.username) return;
          this.profileSubject.next(profile);
          this.profileState = 'loaded';
          this.isOwnProfile = this.authService.currentUserValue?.username === profile.user.username;
          this.totalActivity = profile.totalActivity;
          this.totalRatings = profile.totalAlbumRatings;
          this.totalReviews = profile.totalReviews;
          this.totalLikedAlbums = profile.totalLikes;
          this.totalListenLater = profile.totalListenLater;
          this.loadActivity();
        },
        error: () => {
          if (requestedUsername !== this.username) return;
          this.profileState = 'error';
          this.snackBar.open('Error loading this profile.', 'Close', { duration: 3000 });
        }
      });
  }

  onProfileTabChange(event: MatTabChangeEvent): void {
    this.selectedTabIndex = event.index;
    this.loadTab(event.index);
  }

  selectTab(index: number): void {
    this.selectedTabIndex = index;
    this.loadTab(index);
  }

  private loadTab(index: number): void {
    if (index === 0 && !this.hasLoadedActivity) this.loadActivity();
    if (index === 1 && this.ratingsState === 'idle') this.loadRatedAlbums();
    if (index === 2 && this.reviewsState === 'idle') this.loadReviews();
    if (index === 3 && this.likedAlbumsState === 'idle') this.loadLikedAlbums();
    if (index === 4 && this.isOwnProfile && this.listenLaterState === 'idle') this.loadListenLater();
  }

  loadActivity(): void {
    if (this.hasLoadedActivity) return;
    this.hasLoadedActivity = true;
    this.loadRecentRatings();
    this.loadRecentReviews();
    this.loadRecentLikes();
    this.loadRecentArtists();
  }

  loadRecentRatings(): void {
    if (this.activityRatingsState === 'loading') return;

    const requestedUsername = this.username;
    this.activityRatingsState = 'loading';
    this.apiService.getRatedAlbums(requestedUsername, 0, 3)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          if (requestedUsername !== this.username) return;
          this.updateActivity({ recentRatings: page.content });
          this.activityRatingsState = page.content.length ? 'loaded' : 'empty';
        },
        error: () => {
          if (requestedUsername !== this.username) return;
          this.activityRatingsState = 'error';
        }
      });
  }

  loadRecentReviews(): void {
    if (this.activityReviewsState === 'loading') return;

    const requestedUsername = this.username;
    this.activityReviewsState = 'loading';
    this.apiService.getUserReviews(requestedUsername, 0, 3)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          if (requestedUsername !== this.username) return;
          this.updateActivity({ recentReviews: page.content });
          this.activityReviewsState = page.content.length ? 'loaded' : 'empty';
        },
        error: () => {
          if (requestedUsername !== this.username) return;
          this.activityReviewsState = 'error';
        }
      });
  }

  loadRecentLikes(): void {
    if (this.activityLikesState === 'loading') return;

    const requestedUsername = this.username;
    this.activityLikesState = 'loading';
    this.apiService.getLikedAlbums(requestedUsername, 0, 3)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          if (requestedUsername !== this.username) return;
          this.updateActivity({ recentLikes: page.content });
          this.activityLikesState = page.content.length ? 'loaded' : 'empty';
        },
        error: () => {
          if (requestedUsername !== this.username) return;
          this.activityLikesState = 'error';
        }
      });
  }

  loadRecentArtists(): void {
    if (this.activityArtistsState === 'loading') return;

    const requestedUsername = this.username;
    this.activityArtistsState = 'loading';
    this.apiService.getFollowingArtists(requestedUsername, 0, 3)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          if (requestedUsername !== this.username) return;
          this.updateActivity({ recentArtists: page.content });
          this.activityArtistsState = page.content.length ? 'loaded' : 'empty';
        },
        error: () => {
          if (requestedUsername !== this.username) return;
          this.activityArtistsState = 'error';
        }
      });
  }

  toggleFollow(): void {
    const currentProfile = this.profileSubject.getValue();
    if (!currentProfile || this.isOwnProfile || this.followRequestPending) return;

    const isCurrentlyFollowed = currentProfile.isFollowedByCurrentUser;
    const apiCall = isCurrentlyFollowed
      ? this.apiService.unfollowUser(this.username)
      : this.apiService.followUser(this.username);

    this.followRequestPending = true;
    this.profileSubject.next({
      ...currentProfile,
      isFollowedByCurrentUser: !isCurrentlyFollowed,
      followersCount: Math.max(
        0,
        isCurrentlyFollowed ? currentProfile.followersCount - 1 : currentProfile.followersCount + 1
      )
    });

    apiCall.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.followRequestPending = false;
        this.snackBar.open(isCurrentlyFollowed ? 'User unfollowed.' : 'User followed.', 'Close', {
          duration: 2200
        });
      },
      error: () => {
        this.followRequestPending = false;
        this.profileSubject.next(currentProfile);
        this.snackBar.open('Could not update follow status.', 'Close', { duration: 3000 });
      }
    });
  }

  async shareProfile(username: string): Promise<void> {
    const shareData = {
      title: `${username} on Soundrate`,
      text: `See ${username}'s music profile on Soundrate`,
      url: window.location.href
    };

    try {
      if (navigator.share) {
        await navigator.share(shareData);
        return;
      }

      await navigator.clipboard.writeText(shareData.url);
      this.snackBar.open('Profile link copied.', 'Close', { duration: 2200 });
    } catch (error) {
      if ((error as DOMException)?.name !== 'AbortError') {
        this.snackBar.open('Could not share this profile.', 'Close', { duration: 3000 });
      }
    }
  }

  unlikeAlbum(albumId: number, event: MouseEvent): void {
    event.stopPropagation();
    event.preventDefault();
    const currentLikedAlbums = this.likedAlbumsSubject.getValue();
    const currentActivity = this.activitySubject.getValue();
    this.likedAlbumsSubject.next(currentLikedAlbums.filter(album => album.id !== albumId));
    if (currentActivity) {
      this.activitySubject.next({
        ...currentActivity,
        recentLikes: currentActivity.recentLikes.filter(album => album.id !== albumId)
      });
    }
    this.totalLikedAlbums = Math.max(0, this.totalLikedAlbums - 1);
    this.totalActivity = Math.max(0, this.totalActivity - 1);

    this.apiService
      .unlikeAlbum(albumId.toString())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.snackBar.open('Album removed from likes.', 'Close', { duration: 2000 });
        },
        error: () => {
          this.likedAlbumsSubject.next(currentLikedAlbums);
          if (currentActivity) this.activitySubject.next(currentActivity);
          this.totalLikedAlbums++;
          this.totalActivity++;
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
    this.totalListenLater = Math.max(0, this.totalListenLater - 1);

    if (updatedList.length === 0 && this.currentListenLaterPage > 0) {
      this.currentListenLaterPage--;
      this.loadListenLater();
    }

    this.apiService
      .removeFromListenLater(albumId.toString())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
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

  openFollowListDialog(listType: 'followers' | 'following', totalCount: number): void {
    this.dialog.open(UserListComponent, {
      width: 'calc(100vw - 2rem)',
      maxWidth: '32rem',
      maxHeight: '80vh',
      panelClass: 'user-list-dialog',
      data: {
        username: this.username,
        listType,
        totalCount
      }
    });
  }

  loadRatedAlbums(pageIndex = this.currentRatingsPage, pageSize = this.ratingsPageSize): void {
    if (this.ratingsState === 'loading' || this.isPaginatingRatings) return;

    const isInitialLoad = this.ratingsState === 'idle' || this.ratingsState === 'error';
    const requestedUsername = this.username;
    const previousPageIndex = this.currentRatingsPage;
    const previousPageSize = this.ratingsPageSize;
    if (isInitialLoad) this.ratingsState = 'loading';
    else {
      this.isPaginatingRatings = true;
      this.currentRatingsPage = pageIndex;
      this.ratingsPageSize = pageSize;
    }
    this.apiService
      .getRatedAlbums(requestedUsername, pageIndex, pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          if (requestedUsername !== this.username) return;
          this.ratingsSubject.next(page.content);
          this.totalRatings = page.totalElements;
          this.currentRatingsPage = pageIndex;
          this.ratingsPageSize = pageSize;
          this.ratingsState = page.content.length ? 'loaded' : 'empty';
          this.isPaginatingRatings = false;
        },
        error: () => {
          if (requestedUsername !== this.username) return;
          if (isInitialLoad) this.ratingsState = 'error';
          else {
            this.currentRatingsPage = previousPageIndex;
            this.ratingsPageSize = previousPageSize;
            this.showPaginationError(
              'ratings',
              () => this.loadRatedAlbums(pageIndex, pageSize)
            );
          }
          this.isPaginatingRatings = false;
        }
      });
  }

  loadReviews(pageIndex = this.currentReviewsPage, pageSize = this.reviewsPageSize): void {
    if (this.reviewsState === 'loading' || this.isPaginatingReviews) return;

    const isInitialLoad = this.reviewsState === 'idle' || this.reviewsState === 'error';
    const requestedUsername = this.username;
    const previousPageIndex = this.currentReviewsPage;
    const previousPageSize = this.reviewsPageSize;
    if (isInitialLoad) this.reviewsState = 'loading';
    else {
      this.isPaginatingReviews = true;
      this.currentReviewsPage = pageIndex;
      this.reviewsPageSize = pageSize;
    }
    this.apiService
      .getUserReviews(requestedUsername, pageIndex, pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          if (requestedUsername !== this.username) return;
          this.reviewsSubject.next(page.content);
          this.totalReviews = page.totalElements;
          this.currentReviewsPage = pageIndex;
          this.reviewsPageSize = pageSize;
          this.reviewsState = page.content.length ? 'loaded' : 'empty';
          this.isPaginatingReviews = false;
        },
        error: () => {
          if (requestedUsername !== this.username) return;
          if (isInitialLoad) this.reviewsState = 'error';
          else {
            this.currentReviewsPage = previousPageIndex;
            this.reviewsPageSize = previousPageSize;
            this.showPaginationError(
              'reviews',
              () => this.loadReviews(pageIndex, pageSize)
            );
          }
          this.isPaginatingReviews = false;
        }
      });
  }

  loadLikedAlbums(pageIndex = this.currentLikedAlbumsPage, pageSize = this.likedAlbumsPageSize): void {
    if (this.likedAlbumsState === 'loading' || this.isPaginatingLikedAlbums) return;

    const isInitialLoad = this.likedAlbumsState === 'idle' || this.likedAlbumsState === 'error';
    const requestedUsername = this.username;
    const previousPageIndex = this.currentLikedAlbumsPage;
    const previousPageSize = this.likedAlbumsPageSize;
    if (isInitialLoad) this.likedAlbumsState = 'loading';
    else {
      this.isPaginatingLikedAlbums = true;
      this.currentLikedAlbumsPage = pageIndex;
      this.likedAlbumsPageSize = pageSize;
    }
    this.apiService
      .getLikedAlbums(requestedUsername, pageIndex, pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          if (requestedUsername !== this.username) return;
          this.likedAlbumsSubject.next(page.content);
          this.totalLikedAlbums = page.totalElements;
          this.currentLikedAlbumsPage = pageIndex;
          this.likedAlbumsPageSize = pageSize;
          this.likedAlbumsState = page.content.length ? 'loaded' : 'empty';
          this.isPaginatingLikedAlbums = false;
        },
        error: () => {
          if (requestedUsername !== this.username) return;
          if (isInitialLoad) this.likedAlbumsState = 'error';
          else {
            this.currentLikedAlbumsPage = previousPageIndex;
            this.likedAlbumsPageSize = previousPageSize;
            this.showPaginationError(
              'liked albums',
              () => this.loadLikedAlbums(pageIndex, pageSize)
            );
          }
          this.isPaginatingLikedAlbums = false;
        }
      });
  }

  loadListenLater(pageIndex = this.currentListenLaterPage, pageSize = this.listenLaterPageSize): void {
    if (this.listenLaterState === 'loading' || this.isPaginatingListenLater) return;

    const isInitialLoad = this.listenLaterState === 'idle' || this.listenLaterState === 'error';
    const requestedUsername = this.username;
    const previousPageIndex = this.currentListenLaterPage;
    const previousPageSize = this.listenLaterPageSize;
    if (isInitialLoad) this.listenLaterState = 'loading';
    else {
      this.isPaginatingListenLater = true;
      this.currentListenLaterPage = pageIndex;
      this.listenLaterPageSize = pageSize;
    }
    this.apiService
      .getListenLaterList(pageIndex, pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          if (requestedUsername !== this.username) return;
          this.listenLaterSubject.next(page.content);
          this.totalListenLater = page.totalElements;
          this.currentListenLaterPage = pageIndex;
          this.listenLaterPageSize = pageSize;
          this.listenLaterState = page.content.length ? 'loaded' : 'empty';
          this.isPaginatingListenLater = false;
        },
        error: () => {
          if (requestedUsername !== this.username) return;
          if (isInitialLoad) this.listenLaterState = 'error';
          else {
            this.currentListenLaterPage = previousPageIndex;
            this.listenLaterPageSize = previousPageSize;
            this.showPaginationError(
              'Listen Later albums',
              () => this.loadListenLater(pageIndex, pageSize)
            );
          }
          this.isPaginatingListenLater = false;
        }
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
    this.loadRatedAlbums(event.pageIndex, event.pageSize);
  }

  onReviewsPageChange(event: PageEvent): void {
    this.loadReviews(event.pageIndex, event.pageSize);
  }

  onLikedAlbumsPageChange(event: PageEvent): void {
    this.loadLikedAlbums(event.pageIndex, event.pageSize);
  }

  onListenLaterPageChange(event: PageEvent): void {
    this.loadListenLater(event.pageIndex, event.pageSize);
  }

  private resetFeeds(): void {
    this.profileSubject.next(null);
    this.profileState = 'idle';
    this.activitySubject.next(EMPTY_ACTIVITY);
    this.ratingsSubject.next([]);
    this.reviewsSubject.next([]);
    this.likedAlbumsSubject.next([]);
    this.listenLaterSubject.next([]);

    this.isOwnProfile = false;
    this.selectedTabIndex = 0;
    this.totalActivity = 0;
    this.totalRatings = 0;
    this.totalReviews = 0;
    this.totalLikedAlbums = 0;
    this.totalListenLater = 0;
    this.currentRatingsPage = 0;
    this.currentReviewsPage = 0;
    this.currentLikedAlbumsPage = 0;
    this.currentListenLaterPage = 0;

    this.activityRatingsState = 'idle';
    this.activityReviewsState = 'idle';
    this.activityLikesState = 'idle';
    this.activityArtistsState = 'idle';
    this.ratingsState = 'idle';
    this.reviewsState = 'idle';
    this.likedAlbumsState = 'idle';
    this.listenLaterState = 'idle';
    this.isPaginatingRatings = false;
    this.isPaginatingReviews = false;
    this.isPaginatingLikedAlbums = false;
    this.isPaginatingListenLater = false;

    this.hasLoadedActivity = false;
    this.followRequestPending = false;
  }

  private updateActivity(update: Partial<UserActivity>): void {
    this.activitySubject.next({ ...this.activitySubject.getValue(), ...update });
  }

  private showPaginationError(contentName: string, retry: () => void): void {
    this.snackBar
      .open(`Couldn't load the next page of ${contentName}.`, 'Try again', { duration: 5000 })
      .onAction()
      .pipe(take(1), takeUntilDestroyed(this.destroyRef))
      .subscribe(retry);
  }
}
