import { ChangeDetectorRef, Component, DestroyRef, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BehaviorSubject, distinctUntilChanged, map, Observable, shareReplay, switchMap, tap } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { MatPaginatorModule } from '@angular/material/paginator';
import { AlbumCardComponent } from '../../components/album-card/album-card.component';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { ArtistPage } from '../../models/artist-page.model';
import { SkeletonLoaderComponent } from '../../components/skeleton-loader/skeleton-loader.component';
import { DeezerButtonComponent } from '../../components/deezer-button/deezer-button.component';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { AudioService } from '../../services/audio.service';
import { DeezerTrack } from '../../models/deezer.model';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { StarRatingComponent } from '../../components/star-rating/star-rating.component';

interface Pageable {
  page: number;
  size: number;
  category: DiscographyCategory;
  sort: DiscographySort;
  direction: DiscographyDirection;
}

type DiscographyCategory = 'popular' | 'albums' | 'singles' | 'compilations';
type DiscographySort = 'release' | 'popularity' | 'community';
type DiscographyDirection = 'asc' | 'desc';

@Component({
  selector: 'app-artist-details-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    AlbumCardComponent,
    StarRatingComponent,
    MatIconModule,
    MatPaginatorModule,
    MatButtonModule,
    MatSnackBarModule,
    SkeletonLoaderComponent,
    DeezerButtonComponent
  ],
  templateUrl: './artist-details-page.component.html',
  styleUrl: './artist-details-page.component.css'
})
export class ArtistDetailsPageComponent implements OnInit {
  artistPage$!: Observable<ArtistPage>;
  private pageableSubject = new BehaviorSubject<Pageable>({
    page: 0,
    size: 12,
    category: 'popular',
    sort: 'popularity',
    direction: 'desc'
  });
  private accentColorRequest = 0;
  artistAccentColor = '#5e1c7c';
  isFollowed = false;
  followersCount = 0;
  followRequestPending = false;
  selectedCategory: DiscographyCategory = 'popular';
  selectedSort: DiscographySort = 'popularity';
  selectedDirection: DiscographyDirection = 'desc';

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService,
    public authService: AuthService,
    public audioService: AudioService,
    private snackBar: MatSnackBar,
    private destroyRef: DestroyRef,
    private changeDetectorRef: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.artistPage$ = this.route.paramMap.pipe(
      map(params => Number(params.get('id'))),
      distinctUntilChanged(),
      switchMap(artistId => {
        this.selectedCategory = 'popular';
        this.selectedSort = 'popularity';
        this.selectedDirection = 'desc';
        this.pageableSubject.next({
          page: 0,
          size: 12,
          category: 'popular',
          sort: 'popularity',
          direction: 'desc'
        });
        return this.pageableSubject.pipe(
          switchMap(pageable => this.apiService.getArtistPage(
            artistId,
            pageable.page,
            pageable.size,
            pageable.category,
            pageable.sort,
            pageable.direction
          )),
          tap(page => {
            this.isFollowed = page.isFollowedByCurrentUser;
            this.followersCount = page.followersCount;
            this.updateArtistAccent(page.artistDetails.picture_xl);
          })
        );
      }),
      shareReplay({ bufferSize: 1, refCount: true })
    );
  }

  onPageChange(event: PageEvent): void {
    const newPageable: Pageable = {
      page: event.pageIndex,
      size: event.pageSize,
      category: this.selectedCategory,
      sort: this.selectedSort,
      direction: this.selectedDirection
    };
    this.pageableSubject.next(newPageable);
  }

  selectCategory(category: DiscographyCategory): void {
    if (category === this.selectedCategory) return;
    this.selectedCategory = category;
    this.updateDiscographyQuery();
  }

  onSortChange(sort: DiscographySort): void {
    if (sort === this.selectedSort) return;
    this.selectedSort = sort;
    this.updateDiscographyQuery();
  }

  toggleSortDirection(): void {
    this.selectedDirection = this.selectedDirection === 'desc' ? 'asc' : 'desc';
    this.updateDiscographyQuery();
  }

  get categoryTitle(): string {
    return {
      popular: 'Popular',
      albums: 'Albums',
      singles: 'Singles & EPs',
      compilations: 'Compilations'
    }[this.selectedCategory];
  }

  releaseYear(releaseDate?: string): string | null {
    return releaseDate?.slice(0, 4) || null;
  }

  formatRecordType(recordType?: string): string {
    const labels: Record<string, string> = {
      album: 'Album',
      single: 'Single',
      ep: 'EP',
      compile: 'Compilation',
      compilation: 'Compilation'
    };
    return labels[recordType?.toLowerCase() ?? ''] ?? 'Release';
  }

  togglePopularPlayback(tracks: DeezerTrack[]): void {
    this.audioService.toggleQueue(tracks);
  }

  hasPlayableTracks(tracks: DeezerTrack[]): boolean {
    return tracks.some(track => track.readable !== false && Boolean(track.preview));
  }

  toggleFollow(artistId: number): void {
    if (!this.authService.isAuthenticated()) {
      this.snackBar.open('Sign in to follow artists.', 'Close', { duration: 3000 });
      return;
    }
    if (this.followRequestPending) return;

    const previousState = this.isFollowed;
    this.followRequestPending = true;
    this.isFollowed = !previousState;
    this.followersCount = Math.max(0, this.followersCount + (previousState ? -1 : 1));

    const request = previousState
      ? this.apiService.unfollowArtist(artistId)
      : this.apiService.followArtist(artistId);

    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.followRequestPending = false;
        this.snackBar.open(previousState ? 'Artist unfollowed.' : 'Artist followed.', 'Close', { duration: 2200 });
      },
      error: () => {
        this.isFollowed = previousState;
        this.followersCount = Math.max(0, this.followersCount + (previousState ? 1 : -1));
        this.followRequestPending = false;
        this.snackBar.open('Could not update the artist follow status.', 'Close', { duration: 3000 });
      }
    });
  }

  async shareArtist(name: string): Promise<void> {
    const shareData = {
      title: `${name} on Soundrate`,
      text: `See ${name} on Soundrate`,
      url: window.location.href
    };

    try {
      if (navigator.share) {
        await navigator.share(shareData);
        return;
      }

      await navigator.clipboard.writeText(shareData.url);
      this.snackBar.open('Artist link copied.', 'Close', { duration: 2200 });
    } catch (error) {
      if ((error as DOMException)?.name !== 'AbortError') {
        this.snackBar.open('Could not share this artist.', 'Close', { duration: 3000 });
      }
    }
  }

  formatCompact(value: number): string {
    return new Intl.NumberFormat('en', {
      notation: 'compact',
      maximumFractionDigits: 1
    }).format(value);
  }

  private updateDiscographyQuery(): void {
    const current = this.pageableSubject.value;
    this.pageableSubject.next({
      page: 0,
      size: current.size,
      category: this.selectedCategory,
      sort: this.selectedSort,
      direction: this.selectedDirection
    });
  }

  private updateArtistAccent(imageUrl: string): void {
    const request = ++this.accentColorRequest;
    this.artistAccentColor = '#5e1c7c';
    if (!imageUrl) return;

    const image = new Image();
    image.crossOrigin = 'anonymous';
    image.decoding = 'async';

    image.onload = () => {
      if (request !== this.accentColorRequest) return;

      try {
        const canvas = document.createElement('canvas');
        canvas.width = 40;
        canvas.height = 40;
        const context = canvas.getContext('2d', { willReadFrequently: true });
        if (!context) return;

        context.drawImage(image, 0, 0, canvas.width, canvas.height);
        const pixels = context.getImageData(0, 0, canvas.width, canvas.height).data;
        const buckets = new Map<number, { count: number; red: number; green: number; blue: number }>();

        for (let index = 0; index < pixels.length; index += 16) {
          const red = pixels[index];
          const green = pixels[index + 1];
          const blue = pixels[index + 2];
          const alpha = pixels[index + 3];
          const brightness = (red * 299 + green * 587 + blue * 114) / 1000;
          if (alpha < 180 || brightness < 24 || brightness > 232) continue;

          const key = (red >> 5) << 6 | (green >> 5) << 3 | (blue >> 5);
          const bucket = buckets.get(key) ?? { count: 0, red: 0, green: 0, blue: 0 };
          bucket.count++;
          bucket.red += red;
          bucket.green += green;
          bucket.blue += blue;
          buckets.set(key, bucket);
        }

        const dominant = [...buckets.values()].sort((left, right) => right.count - left.count)[0];
        if (!dominant) return;

        this.artistAccentColor = `rgb(${Math.round(dominant.red / dominant.count)}, ${Math.round(dominant.green / dominant.count)}, ${Math.round(dominant.blue / dominant.count)})`;
        this.changeDetectorRef.markForCheck();
      } catch {
        this.artistAccentColor = '#5e1c7c';
      }
    };

    image.onerror = () => {
      if (request === this.accentColorRequest) this.artistAccentColor = '#5e1c7c';
    };
    image.src = imageUrl;
  }
}
