package com.viniciusmcabral.sound_rate.services;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.models.ReviewLikeModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.ReviewLikeRepository;

@Service
public class ReviewLikeService {

	private final ReviewLikeRepository reviewLikeRepository;
	private final AlbumReviewRepository albumReviewRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public ReviewLikeService(ReviewLikeRepository reviewLikeRepository, AlbumReviewRepository albumReviewRepository,
			AuthenticatedUserService authenticatedUserService) {
		this.reviewLikeRepository = reviewLikeRepository;
		this.albumReviewRepository = albumReviewRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional
	public void likeReview(Long reviewId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		AlbumReviewModel review = albumReviewRepository.findById(reviewId)
				.orElseThrow(() -> new NoSuchElementException("Review not found with id: " + reviewId));

		if (reviewLikeRepository.findByUserAndAlbumReview(currentUser, review).isEmpty()) {
			ReviewLikeModel newLike = new ReviewLikeModel(currentUser, review);
			reviewLikeRepository.save(newLike);
		}
	}

	@Transactional
	public void unlikeReview(Long reviewId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		AlbumReviewModel review = albumReviewRepository.findById(reviewId)
				.orElseThrow(() -> new NoSuchElementException("Review not found with id: " + reviewId));
		reviewLikeRepository.deleteByUserAndAlbumReview(currentUser, review);
	}
}
