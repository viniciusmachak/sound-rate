package com.viniciusmcabral.sound_rate.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.viniciusmcabral.sound_rate.dtos.request.RatingRequestDTO;
import com.viniciusmcabral.sound_rate.models.AlbumRatingModel;
import com.viniciusmcabral.sound_rate.models.TrackRatingModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.TrackRatingRepository;
import com.viniciusmcabral.sound_rate.support.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

	@Mock
	private AlbumRatingRepository albumRatingRepository;

	@Mock
	private TrackRatingRepository trackRatingRepository;

	@Mock
	private AlbumReviewRepository albumReviewRepository;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@InjectMocks
	private RatingService ratingService;

	@Test
	void rateAlbumReplacesTrackRatingsAndSavesAlbumRating() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(albumRatingRepository.findByUserAndAlbumId(currentUser, "album-1")).thenReturn(Optional.empty());

		ratingService.rateAlbumOrTrack(new RatingRequestDTO("album-1", null, 4.5));

		verify(trackRatingRepository).deleteAllByUserAndAlbumId(currentUser, "album-1");
		verify(albumRatingRepository).save(any(AlbumRatingModel.class));
	}

	@Test
	void rateTrackUpdatesAlbumAverageFromTrackRatings() {
		var currentUser = TestDataFactory.user(1L, "listener");
		var existingTrack = new TrackRatingModel("album-1", "track-1", 4.0, currentUser);
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(trackRatingRepository.findByUserAndAlbumIdAndTrackId(currentUser, "album-1", "track-1"))
				.thenReturn(Optional.of(existingTrack));
		when(trackRatingRepository.findByUserAndAlbumId(currentUser, "album-1"))
				.thenReturn(List.of(existingTrack, new TrackRatingModel("album-1", "track-2", 2.0, currentUser)));
		when(albumRatingRepository.findByUserAndAlbumId(currentUser, "album-1")).thenReturn(Optional.empty());

		ratingService.rateAlbumOrTrack(new RatingRequestDTO("album-1", "track-1", 5.0));

		verify(trackRatingRepository).save(existingTrack);
		verify(albumRatingRepository).save(any(AlbumRatingModel.class));
	}

	@Test
	void deleteAlbumRatingRemovesAlbumReviewAlbumRatingAndTrackRatings() {
		var currentUser = TestDataFactory.user(1L, "listener");
		var review = TestDataFactory.review(10L, "album-1", currentUser, 4.0);
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(albumReviewRepository.findByUserAndAlbumId(currentUser, "album-1")).thenReturn(Optional.of(review));

		ratingService.deleteRating("album-1", null);

		verify(albumReviewRepository).delete(review);
		verify(albumRatingRepository).deleteByUserAndAlbumId(currentUser, "album-1");
		verify(trackRatingRepository).deleteAllByUserAndAlbumId(currentUser, "album-1");
	}

	@Test
	void deleteRatingRejectsMissingIdentifiers() {
		assertThatThrownBy(() -> ratingService.deleteRating(null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("albumId or trackId");

		verify(albumRatingRepository, never()).deleteByUserAndAlbumId(any(), any());
	}
}
