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
    private durationSubject = new BehaviorSubject<number>(0);
    private queueSubject = new BehaviorSubject<DeezerTrack[]>([]);
    private currentQueueIndexSubject = new BehaviorSubject<number>(-1);
    private volumeSubject = new BehaviorSubject<number>(0.8);
    private mutedSubject = new BehaviorSubject<boolean>(false);
    private isLoadingSubject = new BehaviorSubject<boolean>(false);
    private errorSubject = new BehaviorSubject<string | null>(null);

    public isPlaying$ = this.isPlayingSubject.asObservable();
    public currentTrack$ = this.currentTrackSubject.asObservable();
    public currentTime$ = this.currentTimeSubject.asObservable();
    public duration$ = this.durationSubject.asObservable();
    public queue$ = this.queueSubject.asObservable();
    public currentQueueIndex$ = this.currentQueueIndexSubject.asObservable();
    public volume$ = this.volumeSubject.asObservable();
    public muted$ = this.mutedSubject.asObservable();
    public isLoading$ = this.isLoadingSubject.asObservable();
    public error$ = this.errorSubject.asObservable();

    constructor() {
        this.audio.volume = this.volumeSubject.value;

        this.audio.addEventListener('loadstart', () => {
            if (this.currentTrackSubject.value) {
                this.isLoadingSubject.next(true);
                this.errorSubject.next(null);
            }
        });

        this.audio.addEventListener('waiting', () => {
            if (this.currentTrackSubject.value && !this.audio.paused) {
                this.isLoadingSubject.next(true);
            }
        });

        this.audio.addEventListener('canplay', () => this.isLoadingSubject.next(false));

        this.audio.addEventListener('playing', () => {
            this.isPlayingSubject.next(true);
            this.isLoadingSubject.next(false);
            this.errorSubject.next(null);
        });
        this.audio.addEventListener('pause', () => this.isPlayingSubject.next(false));

        this.audio.addEventListener('timeupdate', () => {
            this.currentTimeSubject.next(this.audio.currentTime);
        });

        this.audio.addEventListener('loadedmetadata', () => {
            this.durationSubject.next(Number.isFinite(this.audio.duration) ? this.audio.duration : 0);
        });

        this.audio.addEventListener('ended', () => {
            if (this.hasNextTrack()) {
                this.playAtIndex(this.currentQueueIndexSubject.value + 1);
            } else {
                this.stop();
            }
        });

        this.audio.addEventListener('error', () => {
            if (!this.currentTrackSubject.value) return;

            this.isPlayingSubject.next(false);
            this.isLoadingSubject.next(false);
            this.errorSubject.next('This preview could not be loaded.');
        });
    }

    togglePlay(track: DeezerTrack): void {
        if (!track.preview || track.readable === false) return;

        const isSameTrack = this.currentTrackSubject.value?.id === track.id;

        if (isSameTrack) {
            if (this.audio.paused) {
                this.playCurrentTrack();
            } else {
                this.audio.pause();
                this.isLoadingSubject.next(false);
            }
        } else {
            this.queueSubject.next([track]);
            this.playAtIndex(0);
        }
    }

    toggleQueue(tracks: DeezerTrack[]): void {
        const playableTracks = this.getPlayableTracks(tracks);
        if (playableTracks.length === 0) return;

        if (this.isCurrentQueue(playableTracks) && this.currentTrackSubject.value) {
            if (this.audio.paused) {
                this.playCurrentTrack();
            } else {
                this.audio.pause();
                this.isLoadingSubject.next(false);
            }
            return;
        }

        this.queueSubject.next(playableTracks);
        this.playAtIndex(0);
    }

    toggleTrackInQueue(tracks: DeezerTrack[], trackId: number): void {
        const playableTracks = this.getPlayableTracks(tracks);
        const trackIndex = playableTracks.findIndex(track => track.id === trackId);
        if (trackIndex === -1) return;

        if (this.currentTrackSubject.value?.id === trackId && this.isCurrentQueue(playableTracks)) {
            this.togglePlay(this.currentTrackSubject.value);
            return;
        }

        this.queueSubject.next(playableTracks);
        this.playAtIndex(trackIndex);
    }

    previous(): void {
        if (this.audio.currentTime > 3) {
            this.audio.currentTime = 0;
            this.currentTimeSubject.next(0);
            return;
        }

        const previousIndex = this.currentQueueIndexSubject.value - 1;
        if (previousIndex >= 0) {
            this.playAtIndex(previousIndex);
            return;
        }

        this.audio.currentTime = 0;
        this.currentTimeSubject.next(0);
    }

    next(): void {
        if (this.hasNextTrack()) {
            this.playAtIndex(this.currentQueueIndexSubject.value + 1);
        } else {
            this.stop();
        }
    }

    playQueueIndex(index: number): void {
        if (index < 0 || index >= this.queueSubject.value.length) return;
        this.playAtIndex(index);
    }

    seekTo(time: number): void {
        if (!Number.isFinite(time) || !Number.isFinite(this.audio.duration)) return;

        const nextTime = Math.min(Math.max(time, 0), this.audio.duration);
        this.audio.currentTime = nextTime;
        this.currentTimeSubject.next(nextTime);
    }

    setVolume(volume: number): void {
        if (!Number.isFinite(volume)) return;

        const normalizedVolume = Math.min(Math.max(volume, 0), 1);
        this.audio.volume = normalizedVolume;
        this.volumeSubject.next(normalizedVolume);

        if (this.audio.muted && normalizedVolume > 0) {
            this.audio.muted = false;
            this.mutedSubject.next(false);
        }
    }

    toggleMute(): void {
        this.audio.muted = !this.audio.muted;
        this.mutedSubject.next(this.audio.muted);
    }

    retry(): void {
        const currentIndex = this.currentQueueIndexSubject.value;
        if (currentIndex >= 0) {
            this.playAtIndex(currentIndex);
        }
    }

    stop(): void {
        this.audio.pause();
        this.audio.src = '';
        this.currentTrackSubject.next(null);
        this.currentTimeSubject.next(0);
        this.durationSubject.next(0);
        this.queueSubject.next([]);
        this.currentQueueIndexSubject.next(-1);
        this.isLoadingSubject.next(false);
        this.errorSubject.next(null);
    }

    hasNextTrack(): boolean {
        const currentIndex = this.currentQueueIndexSubject.value;
        return currentIndex >= 0 && currentIndex < this.queueSubject.value.length - 1;
    }

    private playAtIndex(index: number): void {
        const track = this.queueSubject.value[index];
        if (!track?.preview) return;

        this.audio.src = track.preview;
        this.currentTrackSubject.next(track);
        this.currentQueueIndexSubject.next(index);
        this.currentTimeSubject.next(0);
        this.durationSubject.next(0);
        this.isLoadingSubject.next(true);
        this.errorSubject.next(null);
        this.playCurrentTrack();
    }

    private getPlayableTracks(tracks: DeezerTrack[]): DeezerTrack[] {
        return tracks.filter(track => track.readable !== false && Boolean(track.preview));
    }

    private isCurrentQueue(tracks: DeezerTrack[]): boolean {
        const currentQueue = this.queueSubject.value;
        return currentQueue.length === tracks.length
            && currentQueue.every((track, index) => track.id === tracks[index].id);
    }

    private playCurrentTrack(): void {
        this.errorSubject.next(null);
        this.isLoadingSubject.next(this.audio.readyState < HTMLMediaElement.HAVE_FUTURE_DATA);
        this.audio.play().catch(() => {
            this.isPlayingSubject.next(false);
            this.isLoadingSubject.next(false);
            this.errorSubject.next('Playback could not be started.');
        });
    }
}
