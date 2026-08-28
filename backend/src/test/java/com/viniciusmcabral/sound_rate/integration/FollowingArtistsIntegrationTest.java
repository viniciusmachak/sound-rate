package com.viniciusmcabral.sound_rate.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.models.ArtistFollowModel;
import com.viniciusmcabral.sound_rate.models.FollowModel;
import com.viniciusmcabral.sound_rate.support.AbstractIntegrationTest;

class FollowingArtistsIntegrationTest extends AbstractIntegrationTest {

	@Test
	void listsFollowedArtistsWithPublicProfileFields() throws Exception {
		var user = createUser("listener", "secret123");
		artistFollowRepository.save(new ArtistFollowModel(user, "27"));
		when(deezerService.getArtistDetails("27"))
				.thenReturn(new DeezerArtistDetailsDTO(27L, "Daft Punk", null, null,
						"https://img.example/daft-punk.jpg", 8, 1000, null));

		mockMvc.perform(get("/api/v1/users/listener/following/artists").param("query", "daft"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].id").value(27))
				.andExpect(jsonPath("$.content[0].name").value("Daft Punk"))
				.andExpect(jsonPath("$.content[0].picture_medium")
						.value("https://img.example/daft-punk.jpg"))
				.andExpect(jsonPath("$.content[0].followedAt").isString())
				.andExpect(jsonPath("$.content[0].pictureMedium").doesNotExist());
	}

	@Test
	void filtersPeopleAndReturnsCurrentUsersFollowState() throws Exception {
		var currentUser = createUser("current", "secret123");
		var profileOwner = createUser("listener", "secret123");
		var matchingUser = createUser("matching", "secret123");
		var otherUser = createUser("other", "secret123");
		followRepository.save(new FollowModel(profileOwner, matchingUser));
		followRepository.save(new FollowModel(profileOwner, otherUser));
		followRepository.save(new FollowModel(currentUser, matchingUser));

		mockMvc.perform(get("/api/v1/users/listener/following").param("query", "match")
				.header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].username").value("matching"))
				.andExpect(jsonPath("$.content[0].isFollowedByCurrentUser").value(true));
	}
}
