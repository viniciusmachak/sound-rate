package com.viniciusmcabral.sound_rate.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.response.ArtistPageDTO;
import com.viniciusmcabral.sound_rate.services.ArtistService;

import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/v1/artists")
public class ArtistController {

	private final ArtistService artistService;

	public ArtistController(ArtistService artistService) {
		this.artistService = artistService;
	}

	@GetMapping("/{artistId}")
	public ArtistPageDTO getArtistPage(@PathVariable @NotBlank String artistId,
			@PageableDefault(size = 20) Pageable pageable) {
		return artistService.getArtistPageDetails(artistId, pageable);
	}
}
