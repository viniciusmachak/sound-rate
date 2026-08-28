package com.viniciusmcabral.sound_rate.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.models.ArtistFollowModel;
import com.viniciusmcabral.sound_rate.repositories.ArtistFollowRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;
import com.viniciusmcabral.sound_rate.support.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class ArtistFollowServiceTest {

	@Mock
	private ArtistFollowRepository artistFollowRepository;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private DeezerService deezerService;

	@InjectMocks
	private ArtistFollowService artistFollowService;

	@Test
	void returnsPagedArtistsFollowedByUser() {
		var user = TestDataFactory.user(1L, "alice");
		var pageable = PageRequest.of(0, 20);
		var follow = new ArtistFollowModel(user, "27");
		when(userRepository.findByUsernameAndActiveTrue("alice")).thenReturn(Optional.of(user));
		when(artistFollowRepository.findByUser(user, pageable))
				.thenReturn(new PageImpl<>(List.of(follow), pageable, 1));
		when(deezerService.getArtistDetails("27"))
				.thenReturn(new DeezerArtistDetailsDTO(27L, "Daft Punk", null, null,
						"https://example.com/daft-punk.jpg", 8, 1000, null));

		var result = artistFollowService.getFollowingArtists("alice", null, pageable);

		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).singleElement().satisfies(artist -> {
			assertThat(artist.id()).isEqualTo(27L);
			assertThat(artist.name()).isEqualTo("Daft Punk");
			assertThat(artist.pictureMedium()).isEqualTo("https://example.com/daft-punk.jpg");
		});
	}

	@Test
	void searchesFollowedArtistsByName() {
		var user = TestDataFactory.user(1L, "alice");
		var pageable = PageRequest.of(0, 20);
		var daftPunk = new ArtistFollowModel(user, "27");
		var radiohead = new ArtistFollowModel(user, "399");
		when(userRepository.findByUsernameAndActiveTrue("alice")).thenReturn(Optional.of(user));
		when(artistFollowRepository.findByUser(user)).thenReturn(List.of(daftPunk, radiohead));
		when(deezerService.getArtistDetails("27"))
				.thenReturn(new DeezerArtistDetailsDTO(27L, "Daft Punk", null, null, null, 8, 1000, null));
		when(deezerService.getArtistDetails("399"))
				.thenReturn(new DeezerArtistDetailsDTO(399L, "Radiohead", null, null, null, 9, 1000, null));

		var result = artistFollowService.getFollowingArtists("alice", "punk", pageable);

		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).extracting(artist -> artist.name()).containsExactly("Daft Punk");
	}
}
