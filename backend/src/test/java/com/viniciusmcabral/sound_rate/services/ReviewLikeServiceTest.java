package com.viniciusmcabral.sound_rate.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.ReviewLikeRepository;
import com.viniciusmcabral.sound_rate.support.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class ReviewLikeServiceTest {

	@Mock
	private ReviewLikeRepository reviewLikeRepository;

	@Mock
	private AlbumReviewRepository albumReviewRepository;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@InjectMocks
	private ReviewLikeService reviewLikeService;

	@Test
	void likeReviewCreatesLikeWhenReviewExistsAndWasNotLiked() {
		var currentUser = TestDataFactory.user(1L, "listener");
		var review = TestDataFactory.review(2L, "album-1", TestDataFactory.user(3L, "author"), 4.0);
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(albumReviewRepository.findById(2L)).thenReturn(Optional.of(review));
		when(reviewLikeRepository.findByUserAndAlbumReview(currentUser, review)).thenReturn(Optional.empty());

		reviewLikeService.likeReview(2L);

		verify(reviewLikeRepository).save(any());
	}

	@Test
	void likeReviewRejectsMissingReview() {
		when(authenticatedUserService.requireCurrentUser()).thenReturn(TestDataFactory.user(1L, "listener"));
		when(albumReviewRepository.findById(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reviewLikeService.likeReview(9L)).isInstanceOf(java.util.NoSuchElementException.class);
		verify(reviewLikeRepository, never()).save(any());
	}

	@Test
	void unlikeReviewDeletesExistingLike() {
		var currentUser = TestDataFactory.user(1L, "listener");
		var review = TestDataFactory.review(2L, "album-1", TestDataFactory.user(3L, "author"), 4.0);
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(albumReviewRepository.findById(2L)).thenReturn(Optional.of(review));

		reviewLikeService.unlikeReview(2L);

		verify(reviewLikeRepository).deleteByUserAndAlbumReview(currentUser, review);
	}
}
