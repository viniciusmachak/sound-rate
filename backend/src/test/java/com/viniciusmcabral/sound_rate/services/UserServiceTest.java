package com.viniciusmcabral.sound_rate.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdatePasswordDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdateProfileDTO;
import com.viniciusmcabral.sound_rate.models.AlbumRatingModel;
import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumLikeRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.ArtistFollowRepository;
import com.viniciusmcabral.sound_rate.repositories.FollowRepository;
import com.viniciusmcabral.sound_rate.repositories.ListenLaterRepository;
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
	private ListenLaterRepository listenLaterRepository;

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
	void profileIncludesBioJoinedDateAverageAndHighestRatedAlbum() {
		var user = TestDataFactory.user(1L, "listener");
		var joinedAt = LocalDateTime.of(2024, 3, 10, 12, 0);
		user.setBio("Shoegaze and soul collector.");
		ReflectionTestUtils.setField(user, "createdAt", joinedAt);
		var highestRating = new AlbumRatingModel("101", 5.0, user);
		var album = new DeezerAlbumDTO(101L, "Favorite record", null, "cover.jpg", "cover-xl.jpg", null,
				null, null, 0, 0, 0, false, null, null, null, null, null, null);

		when(userRepository.findByUsernameAndActiveTrue("listener")).thenReturn(Optional.of(user));
		when(albumRatingRepository.countByUser(user)).thenReturn(2L);
		when(trackRatingRepository.countByUser(user)).thenReturn(1L);
		when(albumRatingRepository.findAverageRatingByUser(user)).thenReturn(Optional.of(4.5));
		when(trackRatingRepository.findAverageRatingByUser(user)).thenReturn(Optional.of(3.0));
		when(albumRatingRepository.findFirstByUserAndRatingIsNotNullOrderByRatingDescUpdatedAtDesc(user))
				.thenReturn(Optional.of(highestRating));
		when(deezerService.getAlbumDetails("101")).thenReturn(album);

		var profile = userService.getUserProfile("listener");

		assertThat(profile.bio()).isEqualTo("Shoegaze and soul collector.");
		assertThat(profile.joinedAt()).isEqualTo(joinedAt);
		assertThat(profile.averageRating()).isEqualTo(4.0);
		assertThat(profile.featuredAlbum().album().title()).isEqualTo("Favorite record");
		assertThat(profile.featuredAlbum().userRating()).isEqualTo(5.0);
	}

	@Test
	void profileIncludesTotalsForEveryTabAndKeepsListenLaterPrivate() {
		var user = TestDataFactory.user(1L, "listener");
		when(userRepository.findByUsernameAndActiveTrue("listener")).thenReturn(Optional.of(user));
		when(authenticatedUserService.getCurrentUserOrNull()).thenReturn(user);
		when(albumReviewRepository.countByUser(user)).thenReturn(2L);
		when(albumRatingRepository.countByUser(user)).thenReturn(3L);
		when(albumLikeRepository.countByUser(user)).thenReturn(4L);
		when(artistFollowRepository.countByUser(user)).thenReturn(5L);
		when(listenLaterRepository.countByUser(user)).thenReturn(6L);

		var profile = userService.getUserProfile("listener");

		assertThat(profile.totalReviews()).isEqualTo(2L);
		assertThat(profile.totalAlbumRatings()).isEqualTo(3L);
		assertThat(profile.totalLikes()).isEqualTo(4L);
		assertThat(profile.totalListenLater()).isEqualTo(6L);
		assertThat(profile.totalActivity()).isEqualTo(14L);
	}

	@Test
	void reviewsPageIncludesAlbumAndReviewDetails() {
		var user = TestDataFactory.user(1L, "listener");
		var review = new AlbumReviewModel("101", "A thoughtful review.", user, 4.5);
		TestDataFactory.setId(review, 8L);
		var reviewDate = LocalDateTime.of(2025, 4, 2, 14, 30);
		ReflectionTestUtils.setField(review, "createdAt", reviewDate);
		var album = new DeezerAlbumDTO(101L, "Reviewed record", null, "cover.jpg", "cover-xl.jpg", null,
				null, null, 0, 0, 0, false, null, null, null, null, null, null);
		var pageable = PageRequest.of(0, 8);

		when(userRepository.findByUsernameAndActiveTrue("listener")).thenReturn(Optional.of(user));
		when(albumReviewRepository.findByUser(user, pageable))
				.thenReturn(new PageImpl<>(List.of(review), pageable, 1));
		when(deezerService.getAlbumDetails("101")).thenReturn(album);

		var page = userService.getReviewsPage("listener", pageable);

		assertThat(page.getTotalElements()).isEqualTo(1);
		assertThat(page.getContent().getFirst().album().title()).isEqualTo("Reviewed record");
		assertThat(page.getContent().getFirst().text()).isEqualTo("A thoughtful review.");
		assertThat(page.getContent().getFirst().reviewDate()).isEqualTo(reviewDate);
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
	void updateProfileStoresTrimmedBioAndReturnsPrivateAccountFields() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(userRepository.findByEmail("listener+new@example.com")).thenReturn(Optional.empty());
		when(userRepository.save(currentUser)).thenReturn(currentUser);

		var updated = userService.updateProfile(currentUser,
				new UpdateProfileDTO("listener+new@example.com", "  Always looking for new records.  "));

		assertThat(currentUser.getBio()).isEqualTo("Always looking for new records.");
		assertThat(updated.email()).isEqualTo("listener+new@example.com");
		assertThat(updated.bio()).isEqualTo("Always looking for new records.");
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
