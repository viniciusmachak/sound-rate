package com.viniciusmcabral.sound_rate.dtos.response;

import java.util.List;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;

public record ArtistCommunityDTO(DeezerAlbumDTO highestRatedAlbum, Double discographyAverage,
		long ratingsCount, long reviewsCount, List<DeezerAlbumDTO> communityFavorites,
		List<ArtistRecentReviewDTO> recentReviews) {
}
