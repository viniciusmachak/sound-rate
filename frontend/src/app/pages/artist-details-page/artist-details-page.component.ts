import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, distinctUntilChanged, map, Observable, shareReplay, switchMap } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule } from '@angular/material/paginator';
import { AlbumCardComponent } from '../../components/album-card/album-card.component';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { ArtistPage } from '../../models/artist-page.model';

interface Pageable {
  page: number;
  size: number;
}

@Component({
  selector: 'app-artist-details-page',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule, AlbumCardComponent, MatIconModule, MatPaginatorModule],
  templateUrl: './artist-details-page.component.html',
  styleUrl: './artist-details-page.component.css'
})
export class ArtistDetailsPageComponent implements OnInit {
  artistPage$!: Observable<ArtistPage>;
  private pageableSubject = new BehaviorSubject<Pageable>({ page: 0, size: 12 });

  constructor(
    private route: ActivatedRoute,
    private apiService: ApiService
  ) { }

  ngOnInit(): void {
    this.artistPage$ = this.route.paramMap.pipe(
      map(params => Number(params.get('id'))),
      distinctUntilChanged(),
      switchMap(artistId => {
        this.pageableSubject.next({ page: 0, size: 12 });
        return this.pageableSubject.pipe(
          switchMap(pageable => this.apiService.getArtistPage(artistId, pageable.page, pageable.size))
        );
      }),
      shareReplay({ bufferSize: 1, refCount: true })
    );
  }

  onPageChange(event: PageEvent): void {
    const newPageable: Pageable = {
      page: event.pageIndex,
      size: event.pageSize
    };
    this.pageableSubject.next(newPageable);
  }
}
