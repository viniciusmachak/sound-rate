package com.viniciusmcabral.sound_rate.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.response.ArtistPageDTO;
import com.viniciusmcabral.sound_rate.services.ArtistService;
import com.viniciusmcabral.sound_rate.services.ArtistFollowService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/v1/artists")
public class ArtistController {

	private final ArtistService artistService;
	private final ArtistFollowService artistFollowService;

	public ArtistController(ArtistService artistService, ArtistFollowService artistFollowService) {
		this.artistService = artistService;
		this.artistFollowService = artistFollowService;
	}

	@GetMapping("/{artistId}")
	public ArtistPageDTO getArtistPage(@PathVariable @NotBlank String artistId,
			@PageableDefault(size = 20) Pageable pageable) {
		return artistService.getArtistPageDetails(artistId, pageable);
	}

	@PostMapping("/{artistId}/follow")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<Void> followArtist(@PathVariable @NotBlank String artistId) {
		artistFollowService.followArtist(artistId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{artistId}/follow")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<Void> unfollowArtist(@PathVariable @NotBlank String artistId) {
		artistFollowService.unfollowArtist(artistId);
		return ResponseEntity.noContent().build();
	}
}
