import { CommonModule } from '@angular/common';
import { Component, HostListener } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink } from '@angular/router';
import { concat, Observable, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, map, shareReplay, startWith, switchMap } from 'rxjs/operators';
import { SearchResult } from '../../models/search-result.model';
import { User } from '../../models/user.model';
import { UserRating } from '../../models/user-rating.model';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

interface HeaderSearchState {
  isLoading: boolean;
  query: string;
  results: SearchResult[];
  error: boolean;
}

interface RecentRatingsState {
  isLoading: boolean;
  ratings: UserRating[];
  error: boolean;
}

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  currentUser$: Observable<User | null>;
  recentRatingsState$: Observable<RecentRatingsState>;
  searchControl = new FormControl('', { nonNullable: true });
  searchState$: Observable<HeaderSearchState>;
  readonly ratingStars = [1, 2, 3, 4, 5];
  isSearchFocused = false;
  isSidebarOpen = false;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) {
    this.currentUser$ = this.authService.currentUser$;
    this.recentRatingsState$ = this.currentUser$.pipe(
      switchMap(user => user
        ? concat(
          of<RecentRatingsState>({ isLoading: true, ratings: [], error: false }),
          this.apiService.getRatedAlbums(user.username, 0, 5).pipe(
            map(page => ({
              isLoading: false,
              ratings: page.content
                .slice()
                .sort((a, b) => Date.parse(b.ratingDate) - Date.parse(a.ratingDate))
                .slice(0, 5),
              error: false
            })),
            catchError(() => of<RecentRatingsState>({
              isLoading: false,
              ratings: [],
              error: true
            }))
          )
        )
        : of<RecentRatingsState>({ isLoading: false, ratings: [], error: false })
      ),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    this.searchState$ = this.searchControl.valueChanges.pipe(
      startWith(''),
      map(query => query.trim()),
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(query => {
        if (query.length < 3) {
          return of<HeaderSearchState>({ isLoading: false, query, results: [], error: false });
        }

        return concat(
          of<HeaderSearchState>({ isLoading: true, query, results: [], error: false }),
          this.apiService.search(query).pipe(
            map(results => ({ isLoading: false, query, results: results.slice(0, 6), error: false })),
            catchError(() => of<HeaderSearchState>({
              isLoading: false,
              query,
              results: [],
              error: true
            }))
          )
        );
      }),
      shareReplay({ bufferSize: 1, refCount: true })
    );
  }

  logout(): void {
    this.closeSidebar();
    this.authService.logout();
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
    this.isSearchFocused = false;
  }

  closeSidebar(): void {
    this.isSidebarOpen = false;
  }

  @HostListener('document:keydown.escape')
  closeOverlays(): void {
    this.closeSidebar();
    this.isSearchFocused = false;
  }

  closeSearch(): void {
    window.setTimeout(() => {
      this.isSearchFocused = false;
    }, 150);
  }

  submitSearch(event?: Event): void {
    event?.preventDefault();
    const query = this.searchControl.value.trim();

    if (query.length < 3) return;

    this.isSearchFocused = false;
    void this.router.navigate(['/'], { queryParams: { q: query } });
  }

  selectResult(result: SearchResult): void {
    if (result.type === 'album' && result.album) {
      void this.router.navigate(['/album', result.album.id]);
    } else if (result.type === 'artist' && result.artist) {
      void this.router.navigate(['/artist', result.artist.id]);
    } else if (result.type === 'user' && result.user) {
      void this.router.navigate(['/user', result.user.username]);
    }

    this.searchControl.setValue('');
    this.isSearchFocused = false;
  }

  resultImage(result: SearchResult): string {
    if (result.album) return result.album.cover_medium || result.album.cover_xl;
    if (result.artist) return result.artist.picture_medium || result.artist.picture;
    return result.user?.avatarUrl || '/favicon.ico';
  }

  resultTitle(result: SearchResult): string {
    return result.album?.title || result.artist?.name || result.user?.username || '';
  }

  resultSubtitle(result: SearchResult): string {
    if (result.album) return result.album.artist.name;
    if (result.artist) return 'Artist';
    return 'User';
  }

  resultKey(result: SearchResult): string {
    const id = result.album?.id ?? result.artist?.id ?? result.user?.id;
    return `${result.type}:${id}`;
  }
}
