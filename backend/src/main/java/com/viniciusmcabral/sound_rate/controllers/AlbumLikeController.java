package com.viniciusmcabral.sound_rate.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viniciusmcabral.sound_rate.services.AlbumLikeService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/v1/albums")
@SecurityRequirement(name = "bearerAuth")
public class AlbumLikeController {

	private final AlbumLikeService albumLikeService;

	public AlbumLikeController(AlbumLikeService albumLikeService) {
		this.albumLikeService = albumLikeService;
	}

	@PostMapping("/{albumId}/like")
	public ResponseEntity<Void> likeAlbum(@PathVariable @NotBlank String albumId) {
		albumLikeService.likeAlbum(albumId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{albumId}/like")
	public ResponseEntity<Void> unlikeAlbum(@PathVariable @NotBlank String albumId) {
		albumLikeService.unlikeAlbum(albumId);
		return ResponseEntity.noContent().build();
	}
}
