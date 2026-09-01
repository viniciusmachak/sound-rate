package com.viniciusmcabral.sound_rate.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.support.AbstractIntegrationTest;

class ArtistPageIntegrationTest extends AbstractIntegrationTest {

	@Test
	void returnsArtistPageWhenDiscographyContainsReviewedAlbum() throws Exception {
		var author = createUser("reviewer", "Password123!");
		albumReviewRepository.save(new AlbumReviewModel("1001", "A detailed album review.", author, 4.5));

		when(deezerService.getArtistDetails("1"))
				.thenReturn(new DeezerArtistDetailsDTO(1L, "Artist", "artist-link", "picture-xl", "picture-medium",
						1, 100, null));
		when(deezerService.getArtistAlbums("1")).thenReturn(List.of(albumDto("1001")));
		when(deezerService.getArtistTopTracks("1")).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/artists/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.community.reviewsCount").value(1))
				.andExpect(jsonPath("$.community.recentReviews[0].album.id").value(1001))
				.andExpect(jsonPath("$.community.recentReviews[0].review.author.username").value("reviewer"))
				.andExpect(jsonPath("$.community.recentReviews[0].review.text").value("A detailed album review."));
	}
}
