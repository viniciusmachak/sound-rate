import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Observable, combineLatest, concat, of } from 'rxjs';
import { catchError, distinctUntilChanged, map, shareReplay, startWith, switchMap, tap } from 'rxjs/operators';
import { SearchResult } from '../../models/search-result.model';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { AlbumCardComponent } from '../../components/album-card/album-card.component';
import { ArtistCardComponent } from '../../components/artist-card/artist-card.component';
import { HomeFeaturesComponent } from '../../components/home-features/home-features.component';
import { UserCardComponent } from '../../components/user-card/user-card.component';
import { AlbumDashboard } from '../../models/album-details.model';
import { SkeletonLoaderComponent } from '../../components/skeleton-loader/skeleton-loader.component';
import { ActivatedRoute, RouterLink } from '@angular/router';

interface SearchState {
  isLoading: boolean;
  results: SearchResult[];
  query: string;
  error: string | null;
}

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatIconModule, AlbumCardComponent, ArtistCardComponent, UserCardComponent,
    HomeFeaturesComponent, MatButtonModule, MatButtonToggleModule, SkeletonLoaderComponent,
  ],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent implements OnInit {
  filterControl = new FormControl<'all' | 'album' | 'artist' | 'user'>('all');

  resultsData$!: Observable<{
    isLoading: boolean;
    results: SearchResult[];
    filteredResults: SearchResult[];
    query: string;
    error: string | null;
  }>;

  highestRatedAlbums$!: Observable<AlbumDashboard[]>;

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute
  ) { }

  resultKey(result: SearchResult): string {
    const id = result.album?.id ?? result.artist?.id ?? result.user?.id;
    return `${result.type}:${id}`;
  }

  ngOnInit(): void {
    this.highestRatedAlbums$ = this.apiService.getHighestRatedAlbums().pipe(
      catchError(() => of([])),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    const resultsState$ = this.route.queryParamMap.pipe(
      map(params => (params.get('q') ?? '').trim()),
      distinctUntilChanged(),
      tap(query => {
        if (query.length >= 3) {
          this.filterControl.setValue('all');
        }
      }),
      switchMap(query => {
        if (query.length < 3) {
          return of<SearchState>({ isLoading: false, results: [], query, error: null });
        }

        return concat(
          of<SearchState>({ isLoading: true, results: [], query, error: null }),
          this.apiService.search(query).pipe(
            map(results => ({ isLoading: false, results, query, error: null })),
            catchError(() => of<SearchState>({
              isLoading: false,
              results: [],
              query,
              error: 'Search is temporarily unavailable. Please try again.'
            }))
          )
        );
      }),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    this.resultsData$ = combineLatest({
      state: resultsState$,
      filter: this.filterControl.valueChanges.pipe(startWith('all' as const))
    }).pipe(
      map(({ state, filter }) => {
        const filteredResults = filter === 'all'
          ? state.results
          : state.results.filter(result => result.type === filter);

        return { ...state, filteredResults };
      })
    );
  }
}
