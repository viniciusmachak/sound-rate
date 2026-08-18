import { Component, Input } from '@angular/core';
import { DeezerArtist } from '../../models/deezer.model';

import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-artist-card',
  standalone: true,
  imports: [RouterLink, MatCardModule],
  templateUrl: './artist-card.component.html',
  styleUrl: '../entity-card.shared.css'
})
export class ArtistCardComponent {
  @Input() artist!: DeezerArtist;
}
