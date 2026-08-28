import { DeezerAlbum } from './deezer.model';

export interface UserReview {
  id: number;
  album: DeezerAlbum;
  text: string;
  rating: number;
  reviewDate: string;
}
