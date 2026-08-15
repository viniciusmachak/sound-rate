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

import com.viniciusmcabral.sound_rate.models.ListenLaterModel;
import com.viniciusmcabral.sound_rate.repositories.ListenLaterRepository;
import com.viniciusmcabral.sound_rate.support.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class ListenLaterServiceTest {

	@Mock
	private ListenLaterRepository listenLaterRepository;

	@Mock
	private DeezerService deezerService;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@InjectMocks
	private ListenLaterService listenLaterService;

	@Test
	void addAlbumCreatesEntryWhenAlbumIsNotTrackedYet() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(listenLaterRepository.findByUserAndAlbumId(currentUser, "album-1")).thenReturn(Optional.empty());

		listenLaterService.addAlbum("album-1");

		verify(listenLaterRepository).save(any());
	}

	@Test
	void addAlbumDoesNotDuplicateExistingEntry() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);
		when(listenLaterRepository.findByUserAndAlbumId(currentUser, "album-1"))
				.thenReturn(Optional.of(new ListenLaterModel(currentUser, "album-1")));

		listenLaterService.addAlbum("album-1");

		verify(listenLaterRepository, never()).save(any());
	}

	@Test
	void removeAlbumDeletesCurrentUsersEntry() {
		var currentUser = TestDataFactory.user(1L, "listener");
		when(authenticatedUserService.requireCurrentUser()).thenReturn(currentUser);

		listenLaterService.removeAlbum("album-1");

		verify(listenLaterRepository).deleteByUserAndAlbumId(currentUser, "album-1");
	}
}
