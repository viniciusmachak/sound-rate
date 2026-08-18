import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { DeezerTrack } from '../models/deezer.model';

@Injectable({
    providedIn: 'root'
})
export class AudioService {
    private audio = new Audio();
    private isPlayingSubject = new BehaviorSubject<boolean>(false);
    private currentTrackSubject = new BehaviorSubject<DeezerTrack | null>(null);
    private currentTimeSubject = new BehaviorSubject<number>(0);

    public isPlaying$ = this.isPlayingSubject.asObservable();
    public currentTrack$ = this.currentTrackSubject.asObservable();
    public currentTime$ = this.currentTimeSubject.asObservable();

    constructor() {
        this.audio.addEventListener('playing', () => this.isPlayingSubject.next(true));
        this.audio.addEventListener('pause', () => this.isPlayingSubject.next(false));

        this.audio.addEventListener('timeupdate', () => {
            this.currentTimeSubject.next(this.audio.currentTime);
        });

        this.audio.addEventListener('ended', () => {
            this.isPlayingSubject.next(false);
            this.currentTrackSubject.next(null);
            this.currentTimeSubject.next(0);
        });
    }

    togglePlay(track: DeezerTrack): void {
        if (!track.preview) return;

        const isSameTrack = this.currentTrackSubject.value?.id === track.id;

        if (isSameTrack) {
            if (this.audio.paused) {
                this.playCurrentTrack();
            } else {
                this.audio.pause();
            }
        } else {
            this.currentTimeSubject.next(0);
            this.audio.src = track.preview;
            this.currentTrackSubject.next(track);

            this.playCurrentTrack();
        }
    }

    stop(): void {
        this.audio.pause();
        this.audio.src = '';
        this.currentTrackSubject.next(null);
        this.currentTimeSubject.next(0);
    }

    private playCurrentTrack(): void {
        this.audio.play().catch(() => {
            this.isPlayingSubject.next(false);
            this.currentTrackSubject.next(null);
        });
    }
}
