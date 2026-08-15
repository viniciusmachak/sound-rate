package com.viniciusmcabral.sound_rate.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.request.ReviewRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AlbumReviewDTO;
import com.viniciusmcabral.sound_rate.services.ReviewService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@RequestMapping("/api/v1/reviews")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping
	public ResponseEntity<AlbumReviewDTO> createReview(@RequestBody @Valid ReviewRequestDTO reviewDto) {
		AlbumReviewDTO newReview = reviewService.createReview(reviewDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(newReview);
	}

	@PutMapping("/{id}")
	public AlbumReviewDTO updateReview(@PathVariable @Positive Long id,
			@RequestBody @Valid ReviewRequestDTO reviewDto) {
		return reviewService.updateReview(id, reviewDto);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteReview(@PathVariable @Positive Long id) {
		reviewService.deleteReview(id);
		return ResponseEntity.noContent().build();
	}
}
