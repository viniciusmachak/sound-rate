import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AlbumDashboard } from '../../models/album-details.model';
import { DeezerAlbum } from '../../models/deezer.model';
import { AlbumCoverDialogComponent } from '../album-cover-dialog/album-cover-dialog.component';

@Component({
  selector: 'app-album-card',
  standalone: true,
  imports: [RouterLink, MatDialogModule, MatIconModule, MatButtonModule],
  templateUrl: './album-card.component.html',
  styleUrl: './album-card.component.css'
})
export class AlbumCardComponent {
  @Input() album!: DeezerAlbum | AlbumDashboard;
  @Input() artistNameOverride?: string;

  constructor(private dialog: MatDialog) {}

  get coverUrl(): string {
    if ('cover_medium' in this.album) {
      return this.album.cover_medium;
    }
    return this.album.coverUrl;
  }

  get fullCoverUrl(): string {
    if ('cover_xl' in this.album) {
      return this.album.cover_xl || this.album.cover_medium;
    }
    return this.album.coverUrl;
  }

  get title(): string {
    return this.album.title;
  }

  get artistName(): string {
    if (this.artistNameOverride) {
      return this.artistNameOverride;
    }

    if ('artist' in this.album && this.album.artist) {
      return this.album.artist.name;
    }

    if ('artistName' in this.album) {
      return this.album.artistName;
    }

    return 'Unknown Artist';
  }

  get id(): string | number {
    return this.album.id;
  }

  get averageRating(): number | null {
    if ('averageRating' in this.album) {
      return this.album.averageRating;
    }
    return null;
  }

  openCover(): void {
    this.dialog.open(AlbumCoverDialogComponent, {
      data: {
        imageUrl: this.fullCoverUrl,
        title: this.title,
        artistName: this.artistName
      },
      panelClass: 'image-dialog-panel',
      maxWidth: '95vw',
      maxHeight: '95vh',
      autoFocus: false
    });
  }
}
