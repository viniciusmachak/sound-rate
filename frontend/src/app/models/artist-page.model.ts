import { DeezerAlbum, DeezerArtistDetails, DeezerTrack } from './deezer.model';
import { Page } from './page.model';
import { AlbumReview } from './review.model';

export interface ArtistRecentReview {
  album: DeezerAlbum;
  review: AlbumReview;
}

export interface ArtistCommunity {
  highestRatedAlbum: DeezerAlbum | null;
  discographyAverage: number | null;
  ratingsCount: number;
  reviewsCount: number;
  communityFavorites: DeezerAlbum[];
  recentReviews: ArtistRecentReview[];
}

export interface ArtistPage {
  artistDetails: DeezerArtistDetails;
  albums: Page<DeezerAlbum>;
  popularTracks: DeezerTrack[];
  followersCount: number;
  isFollowedByCurrentUser: boolean;
  community: ArtistCommunity;
}
