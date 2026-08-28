package com.viniciusmcabral.sound_rate.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.repositories.AlbumRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

	@Mock
	private DeezerService deezerService;

	@Mock
	private ArtistFollowService artistFollowService;

	@Mock
	private AlbumRatingRepository albumRatingRepository;

	@Mock
	private AlbumReviewRepository albumReviewRepository;

	@Mock
	private ReviewService reviewService;

	@InjectMocks
	private ArtistService artistService;

	@BeforeEach
	void setUpArtist() {
		when(deezerService.getArtistDetails("1"))
				.thenReturn(new DeezerArtistDetailsDTO(1L, "Artist", "https://deezer.example/artist/1", null,
						null, 4, 1000, null));
		when(deezerService.getArtistTopTracks("1")).thenReturn(List.of());
	}

	@Test
	void filtersSinglesAndEpsAndSortsThemByNewestRelease() {
		when(deezerService.getArtistAlbums("1")).thenReturn(List.of(
				album(1L, "Older EP", "ep", "2022-02-10", 200),
				album(2L, "Album", "album", "2025-01-01", 900),
				album(3L, "New Single", "single", "2024-11-08", 500)));
		when(albumRatingRepository.findCommunityAverageRatings(List.of("1", "2", "3"))).thenReturn(List.of());

		var result = artistService.getArtistPageDetails("1", "singles", "release", "desc",
				PageRequest.of(0, 12));

		assertThat(result.albums().getContent()).extracting(DeezerAlbumDTO::title)
				.containsExactly("New Single", "Older EP");
	}

	@Test
	void sortsTheCompleteDiscographyByCommunityScoreBeforePaginating() {
		when(deezerService.getArtistAlbums("1")).thenReturn(List.of(
				album(1L, "First", "album", "2024-01-01", 900),
				album(2L, "Community Favorite", "single", "2023-01-01", 100),
				album(3L, "Unrated", "compile", "2025-01-01", 1000)));
		when(albumRatingRepository.findCommunityAverageRatings(List.of("1", "2", "3")))
				.thenReturn(List.of(new Object[] { "1", 3.5 }, new Object[] { "2", 4.8 }));
		when(albumRatingRepository.findDiscographyAverageRating(List.of("1", "2", "3")))
				.thenReturn(Optional.of(4.1));
		when(albumRatingRepository.countByAlbumIdIn(List.of("1", "2", "3"))).thenReturn(12L);
		when(albumReviewRepository.countActiveReviewsByAlbumIds(List.of("1", "2", "3"))).thenReturn(4L);

		var result = artistService.getArtistPageDetails("1", "popular", "community", "desc",
				PageRequest.of(0, 2));

		assertThat(result.albums().getTotalElements()).isEqualTo(3);
		assertThat(result.albums().getContent()).extracting(DeezerAlbumDTO::title)
				.containsExactly("Community Favorite", "First");
		assertThat(result.albums().getContent().getFirst().communityScore()).isEqualTo(4.8);
		assertThat(result.community().highestRatedAlbum().title()).isEqualTo("Community Favorite");
		assertThat(result.community().communityFavorites()).extracting(DeezerAlbumDTO::title)
				.containsExactly("Community Favorite", "First");
		assertThat(result.community().discographyAverage()).isEqualTo(4.1);
		assertThat(result.community().ratingsCount()).isEqualTo(12);
		assertThat(result.community().reviewsCount()).isEqualTo(4);
	}

	@Test
	void sortsPopularityInAscendingOrderWhenRequested() {
		when(deezerService.getArtistAlbums("1")).thenReturn(List.of(
				album(1L, "Most popular", "album", "2024-01-01", 900),
				album(2L, "Least popular", "album", "2023-01-01", 100)));
		when(albumRatingRepository.findCommunityAverageRatings(List.of("1", "2"))).thenReturn(List.of());

		var result = artistService.getArtistPageDetails("1", "albums", "popularity", "asc",
				PageRequest.of(0, 12));

		assertThat(result.albums().getContent()).extracting(DeezerAlbumDTO::title)
				.containsExactly("Least popular", "Most popular");
	}

	private DeezerAlbumDTO album(long id, String title, String recordType, String releaseDate, int fans) {
		return new DeezerAlbumDTO(id, title, "https://deezer.example/album/" + id, null, null,
				new DeezerArtistDTO(1L, "Artist", null, null, null, null), recordType, releaseDate, 0, fans, 0,
				false, null, null, null, List.of(), null, null);
	}
}
