import { User } from './user.model';
import { DeezerAlbum } from './deezer.model';

export interface UserAlbumHighlight {
  album: DeezerAlbum;
  userRating: number;
}

export interface UserProfile {
  user: User;
  bio: string | null;
  joinedAt: string;
  totalReviews: number;
  totalAlbumRatings: number;
  totalTrackRatings: number;
  totalLikes: number;
  totalListenLater: number;
  totalActivity: number;
  followersCount: number;
  followingCount: number;
  isFollowedByCurrentUser: boolean;
  averageRating: number | null;
  featuredAlbum: UserAlbumHighlight | null;
}
