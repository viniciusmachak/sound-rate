package com.viniciusmcabral.sound_rate.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.viniciusmcabral.sound_rate.models.AlbumLikeModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumLikeRepository;
import com.viniciusmcabral.sound_rate.support.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class AlbumLikeServiceTest {

	@Mock
	private AlbumLikeRepository albumLikeRepository;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@InjectMocks
	private AlbumLikeService albumLikeService;

	@Test
	void likeAlbumSavesWhenAlbumWasNotLikedYet() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(albumLikeRepository.findByUserAndAlbumId(currentUser, "album-1")).thenReturn(Optional.empty());

		albumLikeService.likeAlbum("album-1");

		verify(albumLikeRepository).save(any());
	}

	@Test
	void likeAlbumDoesNotDuplicateExistingLike() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(albumLikeRepository.findByUserAndAlbumId(currentUser, "album-1"))
				.thenReturn(Optional.of(new AlbumLikeModel(currentUser, "album-1")));

		albumLikeService.likeAlbum("album-1");

		verify(albumLikeRepository, never()).save(any());
	}

	@Test
	void unlikeAlbumDeletesCurrentUsersLike() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);

		albumLikeService.unlikeAlbum("album-1");

		verify(albumLikeRepository).deleteByUserAndAlbumId(currentUser, "album-1");
	}
}
