package com.viniciusmcabral.sound_rate.services;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.security.access.AccessDeniedException;

import com.viniciusmcabral.sound_rate.dtos.request.ReviewRequestDTO;
import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.ReviewLikeRepository;
import com.viniciusmcabral.sound_rate.support.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private AlbumReviewRepository albumReviewRepository;

	@Mock
	private ReviewLikeRepository reviewLikeRepository;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@InjectMocks
	private ReviewService reviewService;

	@Test
	void createReviewRejectsDuplicateReviewPerAlbum() {
		var currentUser = TestDataFactory.user(1L, "listener");
		var existingReview = TestDataFactory.review(2L, "album-1", currentUser, 4.0);
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(albumReviewRepository.findByUserAndAlbumId(currentUser, "album-1")).thenReturn(Optional.of(existingReview));

		assertThatThrownBy(() -> reviewService.createReview(new ReviewRequestDTO("album-1", "0123456789", 4.0)))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("already reviewed");
	}

	@Test
	void updateReviewRejectsNonAuthor() {
		var currentUser = TestDataFactory.user(1L, "listener");
		var review = TestDataFactory.review(2L, "album-1", TestDataFactory.user(3L, "author"), 4.0);
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(albumReviewRepository.findById(2L)).thenReturn(Optional.of(review));

		assertThatThrownBy(() -> reviewService.updateReview(2L, new ReviewRequestDTO("album-1", "0123456789", 3.0)))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("not the author");

		verify(albumReviewRepository, never()).save(any());
	}

	@Test
	void deleteReviewDeletesReviewOwnedByCurrentUser() {
		var currentUser = TestDataFactory.user(1L, "listener");
		var review = TestDataFactory.review(2L, "album-1", currentUser, 4.0);
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(albumReviewRepository.findById(2L)).thenReturn(Optional.of(review));

		reviewService.deleteReview(2L);

		verify(albumReviewRepository).delete(review);
	}

	@Test
	void getReviewsForAlbumIncludesLikeCountsAndCurrentUsersLikeState() {
		var author = TestDataFactory.user(2L, "author");
		var currentUser = TestDataFactory.user(1L, "listener");
		var review = TestDataFactory.review(2L, "album-1", author, 4.0);
		when(authenticatedUserService.getCurrentUserOrNull()).thenReturn(currentUser);
		when(reviewLikeRepository.countByAlbumReviewIds(List.of(2L)))
				.thenReturn(List.of(new ReviewLikeRepository.ReviewLikeCountProjection() {
					@Override
					public Long getReviewId() {
						return 2L;
					}

					@Override
					public long getLikesCount() {
						return 7L;
					}
				}));
		when(reviewLikeRepository.findLikedReviewIdsByUserAndAlbumReviewIds(currentUser, List.of(2L))).thenReturn(List.of(2L));
		when(albumReviewRepository.findActiveReviewsByAlbumId(any(), any()))
				.thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(review)));

		var page = reviewService.getReviewsForAlbum("album-1", org.springframework.data.domain.PageRequest.of(0, 20));

		assertThat(page.getContent()).singleElement().satisfies(dto -> {
			assertThat(dto.likesCount()).isEqualTo(7L);
			assertThat(dto.isLikedByCurrentUser()).isTrue();
		});
	}

	@Test
	void getRecentReviewsForAlbumsPreservesTheAlbumReference() {
		var author = TestDataFactory.user(2L, "author");
		var review = TestDataFactory.review(5L, "album-42", author, 4.5);
		when(albumReviewRepository.findRecentActiveReviewsByAlbumIds(any(), any())).thenReturn(List.of(review));

		var recentReviews = reviewService.getRecentReviewsForAlbums(List.of("album-42"), 5);

		assertThat(recentReviews).singleElement().satisfies(dto -> {
			assertThat(dto.albumId()).isEqualTo("album-42");
			assertThat(dto.review().author().username()).isEqualTo("author");
			assertThat(dto.review().rating()).isEqualTo(4.5);
		});
	}
}
