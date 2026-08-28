import { DeezerAlbum } from './deezer.model';
import { FollowedArtist } from './followed-artist.model';
import { UserRating } from './user-rating.model';
import { UserReview } from './user-review.model';

export interface UserActivity {
  recentRatings: UserRating[];
  recentReviews: UserReview[];
  recentLikes: DeezerAlbum[];
  recentArtists: FollowedArtist[];
}
