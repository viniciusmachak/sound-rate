package com.viniciusmcabral.sound_rate.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.response.FollowedArtistDTO;
import com.viniciusmcabral.sound_rate.dtos.response.SocialUserDTO;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.services.ArtistFollowService;
import com.viniciusmcabral.sound_rate.services.FollowService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/v1/users")
public class FollowController {

	private final FollowService followService;
	private final ArtistFollowService artistFollowService;

	public FollowController(FollowService followService, ArtistFollowService artistFollowService) {
		this.followService = followService;
		this.artistFollowService = artistFollowService;
	}

	@PostMapping("/{username}/follow")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<Void> followUser(@PathVariable @NotBlank String username,
			@AuthenticationPrincipal UserModel currentUser) {
		followService.followUser(username, currentUser);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{username}/follow")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<Void> unfollowUser(@PathVariable @NotBlank String username,
			@AuthenticationPrincipal UserModel currentUser) {
		followService.unfollowUser(username, currentUser);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{username}/followers")
	public Page<SocialUserDTO> getFollowers(@PathVariable @NotBlank String username,
			@RequestParam(required = false) String query,
			@PageableDefault(size = 20) Pageable pageable) {
		return followService.getFollowers(username, query, pageable);
	}

	@GetMapping("/{username}/following")
	public Page<SocialUserDTO> getFollowing(@PathVariable @NotBlank String username,
			@RequestParam(required = false) String query,
			@PageableDefault(size = 20) Pageable pageable) {
		return followService.getFollowing(username, query, pageable);
	}

	@GetMapping("/{username}/following/artists")
	public Page<FollowedArtistDTO> getFollowingArtists(@PathVariable @NotBlank String username,
			@RequestParam(required = false) String query,
			@PageableDefault(size = 20) Pageable pageable) {
		return artistFollowService.getFollowingArtists(username, query, pageable);
	}
}
