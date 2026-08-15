package com.viniciusmcabral.sound_rate.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdatePasswordDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdateProfileDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserProfileDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserRatingDTO;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.services.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/{username}")
	public UserProfileDTO getUserProfile(@PathVariable @NotBlank String username) {
		return userService.getUserProfile(username);
	}

	@GetMapping("/{username}/likes")
	public Page<DeezerAlbumDTO> getLikedAlbums(@PathVariable @NotBlank String username,
			@PageableDefault(size = 20) Pageable pageable) {
		return userService.getLikedAlbumsPage(username, pageable);
	}

	@GetMapping("/{username}/ratings")
	public Page<UserRatingDTO> getRatedAlbums(@PathVariable @NotBlank String username,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return userService.getRatedAlbumsPage(username, pageable);
	}

	@DeleteMapping("/me")
	public ResponseEntity<Void> deleteCurrentUser() {
		userService.deleteCurrentUser();
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/me/profile")
	public ResponseEntity<UserDTO> updateProfile(@AuthenticationPrincipal UserModel currentUser,
			@RequestBody @Valid UpdateProfileDTO data) {
		UserDTO updatedUser = userService.updateProfile(currentUser, data);
		return ResponseEntity.ok(updatedUser);
	}

	@PutMapping("/me/password")
	public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal UserModel currentUser,
			@RequestBody @Valid UpdatePasswordDTO data) {
		userService.updatePassword(currentUser, data);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/me/avatar")
	public ResponseEntity<UserDTO> updateAvatar(@AuthenticationPrincipal UserModel currentUser,
			@RequestParam("file") MultipartFile file) {
		UserDTO updatedUser = userService.updateAvatar(currentUser, file);
		return ResponseEntity.ok(updatedUser);
	}

	@DeleteMapping("/me/avatar")
	public ResponseEntity<UserDTO> resetAvatar(@AuthenticationPrincipal UserModel currentUser) {
		UserDTO updatedUser = userService.resetAvatar(currentUser);
		return ResponseEntity.ok(updatedUser);
	}
}
