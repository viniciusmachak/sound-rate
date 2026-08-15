import { User } from './user.model';

export interface ReviewRequest {
  albumId: string;
  text: string;
  rating: number;
}

export interface AlbumReview {
  id: number;
  text: string;
  createdAt: string;
  updatedAt: string;
  rating: number;
  author: User;
  likesCount: number;
  isLikedByCurrentUser: boolean;
}

