package com.viniciusmcabral.sound_rate.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.services.ReviewLikeService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@RequestMapping("/api/v1/reviews")
@SecurityRequirement(name = "bearerAuth")
public class ReviewLikeController {

	private final ReviewLikeService reviewLikeService;

	public ReviewLikeController(ReviewLikeService reviewLikeService) {
		this.reviewLikeService = reviewLikeService;
	}

	@PostMapping("/{reviewId}/like")
	public ResponseEntity<Void> likeReview(@PathVariable @Positive Long reviewId) {
		reviewLikeService.likeReview(reviewId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{reviewId}/like")
	public ResponseEntity<Void> unlikeReview(@PathVariable @Positive Long reviewId) {
		reviewLikeService.unlikeReview(reviewId);
		return ResponseEntity.noContent().build();
	}
}
