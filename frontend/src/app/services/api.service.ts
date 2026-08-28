import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AlbumDetails } from '../models/album-details.model';
import { RatingRequest } from '../models/rating.model';
import { AlbumReview, ReviewRequest } from '../models/review.model';
import { UserProfile } from '../models/user-profile.model';
import { UserRating } from '../models/user-rating.model';
import { Page } from '../models/page.model';
import { DeezerAlbum } from '../models/deezer.model';
import { User } from '../models/user.model';
import { SearchResult } from '../models/search-result.model';
import { ArtistPage } from '../models/artist-page.model';
import { AlbumDashboard } from '../models/album-details.model';
import { environment } from '../../environments/environment';
import { FollowedArtist } from '../models/followed-artist.model';
import { SocialUser } from '../models/social-user.model';
import { UserReview } from '../models/user-review.model';

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}
export interface ForgotPasswordRequest {
  email: string;
}
@Injectable({ providedIn: 'root' })
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  search(query: string): Observable<SearchResult[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<SearchResult[]>(`${this.apiUrl}/search`, { params });
  }

  getAlbumDetails(albumId: string): Observable<AlbumDetails> {
    return this.http.get<AlbumDetails>(`${this.apiUrl}/albums/${albumId}`);
  }

  getHighestRatedAlbums(): Observable<AlbumDashboard[]> {
    return this.http.get<AlbumDashboard[]>(`${this.apiUrl}/albums/highest-rated`);
  }

  getArtistPage(
    artistId: number,
    page: number,
    size: number,
    category: string,
    sort: string,
    direction: string
  ): Observable<ArtistPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('category', category)
      .set('order', sort)
      .set('direction', direction);
    return this.http.get<ArtistPage>(`${this.apiUrl}/artists/${artistId}`, { params });
  }

  followArtist(artistId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/artists/${artistId}/follow`, {});
  }

  unfollowArtist(artistId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/artists/${artistId}/follow`);
  }

  getFollowers(
    username: string,
    page: number,
    size: number,
    query = ''
  ): Observable<Page<SocialUser>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (query) params = params.set('query', query);
    return this.http.get<Page<SocialUser>>(`${this.apiUrl}/users/${username}/followers`, { params });
  }

  getFollowing(
    username: string,
    page: number,
    size: number,
    query = ''
  ): Observable<Page<SocialUser>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (query) params = params.set('query', query);
    return this.http.get<Page<SocialUser>>(`${this.apiUrl}/users/${username}/following`, { params });
  }

  getFollowingArtists(
    username: string,
    page: number,
    size: number,
    query = ''
  ): Observable<Page<FollowedArtist>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');
    if (query) params = params.set('query', query);
    return this.http.get<Page<FollowedArtist>>(
      `${this.apiUrl}/users/${username}/following/artists`,
      { params }
    );
  }

  rateAlbumOrTrack(ratingData: RatingRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/ratings`, ratingData);
  }

  deleteRating(albumId?: string, trackId?: string): Observable<any> {
    let params: { [param: string]: string } = {};

    if (albumId) {
      params['albumId'] = albumId;
    }

    if (trackId) {
      params['trackId'] = trackId;
    }

    return this.http.delete(`${this.apiUrl}/ratings`, { params });
  }

  likeAlbum(albumId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/albums/${albumId}/like`, {});
  }

  unlikeAlbum(albumId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/albums/${albumId}/like`);
  }

  likeReview(reviewId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reviews/${reviewId}/like`, {});
  }

  unlikeReview(reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/reviews/${reviewId}/like`);
  }

  createReview(reviewData: ReviewRequest): Observable<AlbumReview> {
    return this.http.post<AlbumReview>(`${this.apiUrl}/reviews`, reviewData);
  }

  updateReview(reviewId: number, reviewData: ReviewRequest): Observable<AlbumReview> {
    return this.http.put<AlbumReview>(`${this.apiUrl}/reviews/${reviewId}`, reviewData);
  }

  deleteReview(reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/reviews/${reviewId}`);
  }

  getUserProfile(username: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/users/${username}`);
  }

  getRatedAlbums(username: string, page: number, size: number): Observable<Page<UserRating>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<UserRating>>(`${this.apiUrl}/users/${username}/ratings`, { params });
  }

  getUserReviews(username: string, page: number, size: number): Observable<Page<UserReview>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<UserReview>>(`${this.apiUrl}/users/${username}/reviews`, { params });
  }

  getLikedAlbums(username: string, page: number, size: number): Observable<Page<DeezerAlbum>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'id,desc');
    return this.http.get<Page<DeezerAlbum>>(`${this.apiUrl}/users/${username}/likes`, { params });
  }

  getListenLaterList(page: number, size: number): Observable<Page<DeezerAlbum>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<DeezerAlbum>>(`${this.apiUrl}/listen-later`, { params });
  }

  addToListenLater(albumId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/listen-later/${albumId}`, {});
  }

  removeFromListenLater(albumId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/listen-later/${albumId}`);
  }

  followUser(username: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users/${username}/follow`, {});
  }

  unfollowUser(username: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${username}/follow`);
  }

  deleteCurrentUser(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/me`);
  }

  updateProfile(data: { email: string; bio: string | null }): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/users/me/profile`, data);
  }

  updatePassword(data: any): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/users/me/password`, data);
  }

  updateAvatar(file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.put<User>(`${this.apiUrl}/users/me/avatar`, formData);
  }

  resetAvatar(): Observable<User> {
    return this.http.delete<User>(`${this.apiUrl}/users/me/avatar`);
  }

  requestPasswordReset(email: string): Observable<void> {
    const payload: ForgotPasswordRequest = { email };
    return this.http.post<void>(`${this.apiUrl}/auth/forgot-password`, payload);
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    const payload: ResetPasswordRequest = { token, newPassword };
    return this.http.post<void>(`${this.apiUrl}/auth/reset-password`, payload);
  }
}
