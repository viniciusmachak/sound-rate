import { DeezerAlbum, DeezerArtistDetails, DeezerTrack } from './deezer.model';
import { Page } from './page.model';

export interface ArtistPage {
  artistDetails: DeezerArtistDetails;
  albums: Page<DeezerAlbum>;
  popularTracks: DeezerTrack[];
  followersCount: number;
  isFollowedByCurrentUser: boolean;
}
