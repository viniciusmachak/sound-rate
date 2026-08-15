package com.viniciusmcabral.sound_rate.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.response.AlbumDashboardDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AlbumDetailsDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AlbumReviewDTO;
import com.viniciusmcabral.sound_rate.services.AlbumService;
import com.viniciusmcabral.sound_rate.services.ReviewService;

import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/v1/albums")
public class AlbumController {

	private final AlbumService albumService;
	private final ReviewService reviewService;

	public AlbumController(AlbumService albumService, ReviewService reviewService) {
		this.albumService = albumService;
		this.reviewService = reviewService;
	}

	@GetMapping("/{id}")
	public AlbumDetailsDTO getAlbumById(@PathVariable("id") @NotBlank String albumId) {
		return albumService.getAlbumDetails(albumId);
	}

	@GetMapping("/{albumId}/reviews")
	public Page<AlbumReviewDTO> getReviewsForAlbum(@PathVariable @NotBlank String albumId,
			@PageableDefault(size = 20) Pageable pageable) {
		return reviewService.getReviewsForAlbum(albumId, pageable);
	}

	@GetMapping("/highest-rated")
	public List<AlbumDashboardDTO> getHighestRatedAlbums() {
		return albumService.getHighestRatedAlbums();
	}
}
