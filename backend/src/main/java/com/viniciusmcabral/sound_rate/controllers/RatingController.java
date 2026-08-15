package com.viniciusmcabral.sound_rate.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.request.RatingRequestDTO;
import com.viniciusmcabral.sound_rate.services.RatingService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@RestController
@Validated
@RequestMapping("/api/v1/ratings")
@SecurityRequirement(name = "bearerAuth")
public class RatingController {

	private final RatingService ratingService;

	public RatingController(RatingService ratingService) {
		this.ratingService = ratingService;
	}

	@PostMapping
	public ResponseEntity<Void> createOrUpdateRating(@RequestBody @Valid RatingRequestDTO ratingDto) {
		ratingService.rateAlbumOrTrack(ratingDto);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public Map<String, Object> getCurrentUserRatings() {
		return ratingService.getUserRatings();
	}

	@DeleteMapping
	public ResponseEntity<Void> deleteRating(@RequestParam(required = false) @Size(max = 255) String albumId,
			@RequestParam(required = false) @Size(max = 255) String trackId) {
		if (!StringUtils.hasText(albumId) && !StringUtils.hasText(trackId)) {
			throw new IllegalArgumentException("You must provide either an albumId or a trackId.");
		}

		ratingService.deleteRating(albumId, trackId);
		return ResponseEntity.noContent().build();
	}
}
