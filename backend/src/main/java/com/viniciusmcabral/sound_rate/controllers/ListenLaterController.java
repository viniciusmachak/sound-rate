package com.viniciusmcabral.sound_rate.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.services.ListenLaterService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/v1/listen-later")
@SecurityRequirement(name = "bearerAuth")
public class ListenLaterController {

	private final ListenLaterService listenLaterService;

	public ListenLaterController(ListenLaterService listenLaterService) {
		this.listenLaterService = listenLaterService;
	}

	@GetMapping
	public Page<DeezerAlbumDTO> getListenLaterList(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return listenLaterService.getListenLaterList(pageable);
	}

	@PostMapping("/{albumId}")
	public ResponseEntity<Void> addAlbumToList(@PathVariable @NotBlank String albumId) {
		listenLaterService.addAlbum(albumId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{albumId}")
	public ResponseEntity<Void> removeAlbumFromList(@PathVariable @NotBlank String albumId) {
		listenLaterService.removeAlbum(albumId);
		return ResponseEntity.noContent().build();
	}
}
