import { Component, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Observable, combineLatest, concat, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, map, shareReplay, startWith, switchMap, tap } from 'rxjs/operators';
import { SearchResult } from '../../models/search-result.model';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AlbumCardComponent } from '../../components/album-card/album-card.component';
import { ArtistCardComponent } from '../../components/artist-card/artist-card.component';
import { UserCardComponent } from '../../components/user-card/user-card.component';
import { AlbumDashboard } from '../../models/album-details.model';
import { SkeletonLoaderComponent } from '../../components/skeleton-loader/skeleton-loader.component';

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
    CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule,
    MatIconModule, AlbumCardComponent, ArtistCardComponent, UserCardComponent,
    MatProgressSpinnerModule, MatButtonToggleModule, SkeletonLoaderComponent,
  ],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent implements OnInit {
  searchControl = new FormControl('');
  filterControl = new FormControl<'all' | 'album' | 'artist' | 'user'>('all');

  resultsData$!: Observable<{
    isLoading: boolean;
    results: SearchResult[];
    filteredResults: SearchResult[];
    query: string;
    error: string | null;
  }>;

  highestRatedAlbums$!: Observable<AlbumDashboard[]>;

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.highestRatedAlbums$ = this.apiService.getHighestRatedAlbums().pipe(
      catchError(() => of([])),
      shareReplay({ bufferSize: 1, refCount: true })
    );

    const resultsState$ = this.searchControl.valueChanges.pipe(
      startWith(''),
      map(query => (query ?? '').trim()),
      debounceTime(400),
      distinctUntilChanged(),
      tap(query => {
        if (query.length >= 3) {
          this.filterControl.setValue('all', { emitEvent: false });
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
