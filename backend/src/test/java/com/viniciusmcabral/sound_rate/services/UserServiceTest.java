package com.viniciusmcabral.sound_rate.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.viniciusmcabral.sound_rate.dtos.request.UpdatePasswordDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdateProfileDTO;
import com.viniciusmcabral.sound_rate.repositories.AlbumLikeRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.ArtistFollowRepository;
import com.viniciusmcabral.sound_rate.repositories.FollowRepository;
import com.viniciusmcabral.sound_rate.repositories.TrackRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;
import com.viniciusmcabral.sound_rate.support.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private AlbumReviewRepository albumReviewRepository;

	@Mock
	private AlbumRatingRepository albumRatingRepository;

	@Mock
	private TrackRatingRepository trackRatingRepository;

	@Mock
	private DeezerService deezerService;

	@Mock
	private StorageService storageService;

	@Mock
	private AlbumLikeRepository albumLikeRepository;

	@Mock
	private FollowRepository followRepository;

	@Mock
	private ArtistFollowRepository artistFollowRepository;

	@Mock
	private EmailService emailService;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@InjectMocks
	private UserService userService;

	@Test
	void profileFollowingCountIncludesUsersAndArtists() {
		var user = TestDataFactory.user(1L, "listener");
		when(userRepository.findByUsernameAndActiveTrue("listener")).thenReturn(Optional.of(user));
		when(followRepository.countActiveFollowingByUser(user)).thenReturn(2L);
		when(artistFollowRepository.countByUser(user)).thenReturn(3L);

		var profile = userService.getUserProfile("listener");

		assertThat(profile.followingCount()).isEqualTo(5L);
	}

	@Test
	void deleteCurrentUserSoftDeletesAccountAndSendsEmail() {
		var user = TestDataFactory.user(1L, "listener");
		when(authenticatedUserService.requireCurrentUser()).thenReturn(user);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		userService.deleteCurrentUser();

		assertThat(user.isActive()).isFalse();
		verify(emailService).sendAccountDeletionEmail(user.getEmail(), user.getUsername());
		verify(userRepository).save(user);
	}

	@Test
	void updateProfileRejectsEmailUsedByAnotherAccount() {
		var currentUser = TestDataFactory.user(1L, "listener");
		var otherUser = TestDataFactory.user(2L, "other");
		when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

		assertThatThrownBy(() -> userService.updateProfile(currentUser, new UpdateProfileDTO("other@example.com")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("already in use");
	}

	@Test
	void updatePasswordRejectsWrongCurrentPassword() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(passwordEncoder.matches("wrong-current", currentUser.getPassword())).thenReturn(false);

		assertThatThrownBy(
				() -> userService.updatePassword(currentUser, new UpdatePasswordDTO("wrong-current", "new-secret")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Incorrect current password");
	}

	@Test
	void updateAvatarStoresReturnedUrl() {
		var currentUser = TestDataFactory.user(1L, "listener");
		var file = new MockMultipartFile("file", "avatar.png", "image/png", "png".getBytes());
		when(storageService.uploadFile(file)).thenReturn("https://cdn.example/avatar.png");

		var updatedUser = userService.updateAvatar(currentUser, file);

		assertThat(updatedUser.avatarUrl()).isEqualTo("https://cdn.example/avatar.png");
		verify(userRepository).save(currentUser);
	}

	@Test
	void resetAvatarReturnsDicebearUrl() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var updatedUser = userService.resetAvatar(currentUser);

		assertThat(updatedUser.avatarUrl()).contains("dicebear").contains(currentUser.getUsername());
	}
}
