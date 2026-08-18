import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Observable } from 'rxjs';
import { DeezerTrack } from '../../models/deezer.model';
import { AudioService } from '../../services/audio.service';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-audio-player',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatProgressBarModule],
  templateUrl: './audio-player.component.html',
  styleUrl: './audio-player.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AudioPlayerComponent {
  currentTrack$: Observable<DeezerTrack | null>;
  isPlaying$: Observable<boolean>;
  currentTime$: Observable<number>;

  constructor(public audioService: AudioService) {
    this.currentTrack$ = this.audioService.currentTrack$;
    this.isPlaying$ = this.audioService.isPlaying$;
    this.currentTime$ = this.audioService.currentTime$;
  }

  togglePlay(track: DeezerTrack): void {
    this.audioService.togglePlay(track);
  }

  closePlayer(): void {
    this.audioService.stop();
  }
}
