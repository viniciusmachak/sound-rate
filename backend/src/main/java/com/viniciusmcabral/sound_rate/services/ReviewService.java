package com.viniciusmcabral.sound_rate.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viniciusmcabral.sound_rate.dtos.request.ReviewRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AlbumReviewDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AlbumReviewReferenceDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserDTO;
import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.ReviewLikeRepository;

@Service
public class ReviewService {

	private final AlbumReviewRepository albumReviewRepository;
	private final ReviewLikeRepository reviewLikeRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public ReviewService(AlbumReviewRepository albumReviewRepository, ReviewLikeRepository reviewLikeRepository,
			AuthenticatedUserService authenticatedUserService) {
		this.albumReviewRepository = albumReviewRepository;
		this.reviewLikeRepository = reviewLikeRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional
	public AlbumReviewDTO createReview(ReviewRequestDTO reviewDTO) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();

		albumReviewRepository.findByUserAndAlbumId(currentUser, reviewDTO.albumId()).ifPresent(review -> {
			throw new IllegalStateException("User has already reviewed this album.");
		});
		
		AlbumReviewModel newReview = new AlbumReviewModel(reviewDTO.albumId(), reviewDTO.text(), currentUser, reviewDTO.rating());
		AlbumReviewModel savedReview = albumReviewRepository.save(newReview);
		
		return buildReviewMapper(List.of(savedReview)).apply(savedReview);
	}

	@Transactional
	public AlbumReviewDTO updateReview(Long reviewId, ReviewRequestDTO reviewDTO) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		AlbumReviewModel existingReview = albumReviewRepository.findById(reviewId)
				.orElseThrow(() -> new NoSuchElementException("Review not found with id: " + reviewId));

		if (!existingReview.getUser().getId().equals(currentUser.getId()))
			throw new AccessDeniedException("User is not the author of this review.");

		existingReview.setText(reviewDTO.text());
		existingReview.setRating(reviewDTO.rating());
		AlbumReviewModel updatedReview = albumReviewRepository.save(existingReview);

		return buildReviewMapper(List.of(updatedReview)).apply(updatedReview);
	}

	@Transactional
	public void deleteReview(Long reviewId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		AlbumReviewModel reviewToDelete = albumReviewRepository.findById(reviewId)
				.orElseThrow(() -> new NoSuchElementException("Review not found with id: " + reviewId));

		if (!reviewToDelete.getUser().getId().equals(currentUser.getId()))
			throw new AccessDeniedException("User is not the author of this review.");

		albumReviewRepository.delete(reviewToDelete);
	}

	public Page<AlbumReviewDTO> getReviewsForAlbum(String albumId, Pageable pageable) {
		Page<AlbumReviewModel> reviewPage = albumReviewRepository.findActiveReviewsByAlbumId(albumId, pageable);
		return reviewPage.map(buildReviewMapper(reviewPage.getContent()));
	}

	public List<AlbumReviewReferenceDTO> getRecentReviewsForAlbums(List<String> albumIds, int limit) {
		if (albumIds.isEmpty() || limit <= 0) return Collections.emptyList();

		List<AlbumReviewModel> reviews = albumReviewRepository.findRecentActiveReviewsByAlbumIds(albumIds,
				PageRequest.of(0, limit));
		Function<AlbumReviewModel, AlbumReviewDTO> mapper = buildReviewMapper(reviews);
		return reviews.stream().map(review -> new AlbumReviewReferenceDTO(review.getAlbumId(), mapper.apply(review)))
				.toList();
	}

	private Function<AlbumReviewModel, AlbumReviewDTO> buildReviewMapper(List<AlbumReviewModel> reviews) {
		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();
		List<Long> reviewIds = reviews.stream().map(AlbumReviewModel::getId).toList();
		Map<Long, Long> likesCountByReviewId = reviewIds.isEmpty() ? Collections.emptyMap()
				: reviewLikeRepository.countByAlbumReviewIds(reviewIds).stream().collect(Collectors.toMap(
						ReviewLikeRepository.ReviewLikeCountProjection::getReviewId,
						ReviewLikeRepository.ReviewLikeCountProjection::getLikesCount));
		Set<Long> likedReviewIds = (currentUser == null || reviewIds.isEmpty()) ? Collections.emptySet()
				: reviewLikeRepository.findLikedReviewIdsByUserAndAlbumReviewIds(currentUser, reviewIds).stream()
						.collect(Collectors.toSet());

		return review -> {
			UserDTO author = new UserDTO(review.getUser().getId(), review.getUser().getUsername(),
					review.getUser().getAvatarUrl());
			return new AlbumReviewDTO(review.getId(), review.getText(), review.getRating(), review.getCreatedAt(),
					review.getUpdatedAt(), author, likesCountByReviewId.getOrDefault(review.getId(), 0L),
					likedReviewIds.contains(review.getId()));
		};
	}
}
