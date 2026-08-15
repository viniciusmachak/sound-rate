package com.viniciusmcabral.sound_rate.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

import com.viniciusmcabral.sound_rate.dtos.request.RatingRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.ReviewRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdateProfileDTO;
import com.viniciusmcabral.sound_rate.support.AbstractIntegrationTest;

class ValidationAndOwnershipIntegrationTest extends AbstractIntegrationTest {

	@Test
	void reviewEndpointsEnforceValidationAndOwnership() throws Exception {
		var owner = createUser("owner", "secret123");
		var intruder = createUser("intruder", "secret123");
		var review = albumReviewRepository.save(new com.viniciusmcabral.sound_rate.models.AlbumReviewModel("1001",
				"Owner review text", owner, 4.0));

		mockMvc.perform(post("/api/v1/reviews").header("Authorization", bearerToken(owner)).contentType(APPLICATION_JSON)
				.content(json(new ReviewRequestDTO("1001", "short", 5.5))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.text").value("Review must be between 10 and 10000 characters"))
				.andExpect(jsonPath("$.errors.rating").value("Rating must be at most 5.0"));

		mockMvc.perform(put("/api/v1/reviews/{id}", review.getId()).header("Authorization", bearerToken(intruder))
				.contentType(APPLICATION_JSON).content(json(new ReviewRequestDTO("1001", "Intruder review text", 3.0))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("User is not the author of this review."));

		mockMvc.perform(delete("/api/v1/reviews/{id}", review.getId()).header("Authorization", bearerToken(intruder)))
				.andExpect(status().isForbidden());
	}

	@Test
	void ratingsFollowsAndProfileUpdatesValidateInput() throws Exception {
		var currentUser = createUser("listener", "secret123");
		createUser("taken", "secret123");

		mockMvc.perform(delete("/api/v1/ratings").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("You must provide either an albumId or a trackId."));

		mockMvc.perform(post("/api/v1/ratings").header("Authorization", bearerToken(currentUser))
				.contentType(APPLICATION_JSON).content(json(new RatingRequestDTO("1001", null, 5.5))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.rating").value("Rating must be at most 5.0"));

		mockMvc.perform(post("/api/v1/users/listener/follow").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("You cannot follow yourself."));

		mockMvc.perform(put("/api/v1/users/me/profile").header("Authorization", bearerToken(currentUser))
				.contentType(APPLICATION_JSON).content(json(new UpdateProfileDTO("taken@example.com"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Email already in use by another account."));
	}

	@Test
	void avatarUploadRequiresANonEmptyFile() throws Exception {
		var currentUser = createUser("listener", "secret123");

		mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
				.file(new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]))
				.header("Authorization", bearerToken(currentUser))).andExpect(status().isBadRequest());
	}
}
