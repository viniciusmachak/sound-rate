package com.viniciusmcabral.sound_rate.dtos.response;

import java.time.LocalDateTime;

public record UserProfileDTO(UserDTO user, String bio, LocalDateTime joinedAt, long totalReviews,
		long totalAlbumRatings, long totalTrackRatings, long totalLikes, long totalListenLater, long totalActivity,
		long followersCount, long followingCount, boolean isFollowedByCurrentUser, Double averageRating,
		UserAlbumHighlightDTO featuredAlbum) {
}
