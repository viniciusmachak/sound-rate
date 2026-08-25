import { ChangeDetectionStrategy, Component, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Observable } from 'rxjs';
import { DeezerTrack } from '../../models/deezer.model';
import { AudioService } from '../../services/audio.service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormatDurationPipe } from '../../pipes/format-duration.pipe';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-audio-player',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    FormatDurationPipe
  ],
  templateUrl: './audio-player.component.html',
  styleUrl: './audio-player.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AudioPlayerComponent {
  currentTrack$: Observable<DeezerTrack | null>;
  isPlaying$: Observable<boolean>;
  currentTime$: Observable<number>;
  duration$: Observable<number>;
  queue$: Observable<DeezerTrack[]>;
  currentQueueIndex$: Observable<number>;
  volume$: Observable<number>;
  muted$: Observable<boolean>;
  isLoading$: Observable<boolean>;
  error$: Observable<string | null>;
  queueExpanded = false;

  constructor(public audioService: AudioService, destroyRef: DestroyRef) {
    this.currentTrack$ = this.audioService.currentTrack$;
    this.isPlaying$ = this.audioService.isPlaying$;
    this.currentTime$ = this.audioService.currentTime$;
    this.duration$ = this.audioService.duration$;
    this.queue$ = this.audioService.queue$;
    this.currentQueueIndex$ = this.audioService.currentQueueIndex$;
    this.volume$ = this.audioService.volume$;
    this.muted$ = this.audioService.muted$;
    this.isLoading$ = this.audioService.isLoading$;
    this.error$ = this.audioService.error$;

    this.currentTrack$.pipe(takeUntilDestroyed(destroyRef)).subscribe(track => {
      if (!track) this.queueExpanded = false;
    });
  }

  togglePlay(track: DeezerTrack): void {
    this.audioService.togglePlay(track);
  }

  closePlayer(): void {
    this.queueExpanded = false;
    this.audioService.stop();
  }

  playPrevious(): void {
    this.audioService.previous();
  }

  playNext(): void {
    this.audioService.next();
  }

  seek(event: Event): void {
    const input = event.target;
    if (input instanceof HTMLInputElement) {
      this.audioService.seekTo(Number(input.value));
    }
  }

  changeVolume(event: Event): void {
    const input = event.target;
    if (input instanceof HTMLInputElement) {
      this.audioService.setVolume(Number(input.value));
    }
  }

  toggleMute(): void {
    this.audioService.toggleMute();
  }

  toggleQueue(): void {
    this.queueExpanded = !this.queueExpanded;
  }

  playQueueTrack(index: number): void {
    this.audioService.playQueueIndex(index);
  }

  retry(): void {
    this.audioService.retry();
  }

  getVolumeIcon(volume: number, muted: boolean): string {
    if (muted || volume === 0) return 'volume_off';
    if (volume < 0.5) return 'volume_down';
    return 'volume_up';
  }
}
