import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface AlbumCoverDialogData {
  imageUrl: string;
  title: string;
  artistName?: string;
}

@Component({
  selector: 'app-album-cover-dialog',
  standalone: true,
  imports: [MatButtonModule, MatDialogModule, MatIconModule],
  templateUrl: './album-cover-dialog.component.html',
  styleUrl: './album-cover-dialog.component.css'
})
export class AlbumCoverDialogComponent {
  readonly data = inject<AlbumCoverDialogData>(MAT_DIALOG_DATA);
}
