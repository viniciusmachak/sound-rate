package com.viniciusmcabral.sound_rate.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.viniciusmcabral.sound_rate.dtos.request.RatingRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.ReviewRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdatePasswordDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdateProfileDTO;
import com.viniciusmcabral.sound_rate.support.AbstractIntegrationTest;

class ProtectedEndpointsIntegrationTest extends AbstractIntegrationTest {

	@ParameterizedTest
	@MethodSource("protectedRequests")
	void protectedEndpointsRejectUnauthenticatedRequests(MockHttpServletRequestBuilder requestBuilder) throws Exception {
		mockMvc.perform(requestBuilder).andExpect(status().isUnauthorized());
	}

	@Test
	void authenticatedRequestsCanMutateLikesFollowsListenLaterAndRatings() throws Exception {
		var currentUser = createUser("listener", "secret123");
		var otherUser = createUser("author", "secret123");
		var review = albumReviewRepository.save(new com.viniciusmcabral.sound_rate.models.AlbumReviewModel("1001",
				"Review text long enough", otherUser, 4.0));

		mockMvc.perform(post("/api/v1/albums/1001/like").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/api/v1/albums/1001/like").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/users/author/follow").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/api/v1/users/author/follow").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/artists/27/follow").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/api/v1/artists/27/follow").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/listen-later/1001").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/listen-later").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1001));
		mockMvc.perform(delete("/api/v1/listen-later/1001").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/ratings").header("Authorization", bearerToken(currentUser))
				.contentType(APPLICATION_JSON).content(json(new RatingRequestDTO("1001", null, 4.5))))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/ratings").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.albumRatings[0].rating").value(4.5));
		mockMvc.perform(delete("/api/v1/ratings").header("Authorization", bearerToken(currentUser)).param("albumId", "1001"))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/reviews/" + review.getId() + "/like").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/api/v1/reviews/" + review.getId() + "/like").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isNoContent());
	}

	@Test
	void authenticatedRequestsCanCreateUpdateAndDeleteOwnReviewAndProfileState() throws Exception {
		var currentUser = createUser("listener", "secret123");

		var reviewResponse = mockMvc.perform(post("/api/v1/reviews").header("Authorization", bearerToken(currentUser))
				.contentType(APPLICATION_JSON).content(json(new ReviewRequestDTO("1001", "Review text long enough", 4.0))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.albumId").doesNotExist())
				.andReturn();
		long reviewId = objectMapper.readTree(reviewResponse.getResponse().getContentAsString()).path("id").asLong();

		mockMvc.perform(put("/api/v1/reviews/{id}", reviewId).header("Authorization", bearerToken(currentUser))
				.contentType(APPLICATION_JSON)
				.content(json(new ReviewRequestDTO("1001", "Updated review text long enough", 3.5))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.text").value("Updated review text long enough"));
		mockMvc.perform(delete("/api/v1/reviews/{id}", reviewId).header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isNoContent());

		mockMvc.perform(put("/api/v1/users/me/profile").header("Authorization", bearerToken(currentUser))
				.contentType(APPLICATION_JSON).content(json(new UpdateProfileDTO("listener+new@example.com"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.avatarUrl").isString());
		mockMvc.perform(put("/api/v1/users/me/password").header("Authorization", bearerToken(currentUser))
				.contentType(APPLICATION_JSON).content(json(new UpdatePasswordDTO("secret123", "newsecret123"))))
				.andExpect(status().isNoContent());
		mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
				.file(new MockMultipartFile("file", "avatar.png", "image/png", "png".getBytes()))
				.header("Authorization", bearerToken(currentUser))).andExpect(status().isOk())
				.andExpect(jsonPath("$.avatarUrl").value("https://cdn.example/avatar.png"));
		mockMvc.perform(delete("/api/v1/users/me/avatar").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.avatarUrl").value(org.hamcrest.Matchers.containsString("dicebear")));
		mockMvc.perform(delete("/api/v1/users/me").header("Authorization", bearerToken(currentUser)))
				.andExpect(status().isNoContent());
	}

	static Stream<MockHttpServletRequestBuilder> protectedRequests() {
		return Stream.of(
				post("/api/v1/albums/1001/like"),
				delete("/api/v1/albums/1001/like"),
				post("/api/v1/users/author/follow"),
				delete("/api/v1/users/author/follow"),
				post("/api/v1/artists/27/follow"),
				delete("/api/v1/artists/27/follow"),
				get("/api/v1/listen-later"),
				post("/api/v1/listen-later/1001"),
				delete("/api/v1/listen-later/1001"),
				post("/api/v1/ratings").contentType(APPLICATION_JSON)
						.content("{\"albumId\":\"1001\",\"rating\":4.0}"),
				get("/api/v1/ratings"),
				delete("/api/v1/ratings").param("albumId", "1001"),
				post("/api/v1/reviews").contentType(APPLICATION_JSON)
						.content("{\"albumId\":\"1001\",\"text\":\"Review text long enough\",\"rating\":4.0}"),
				put("/api/v1/reviews/1").contentType(APPLICATION_JSON)
						.content("{\"albumId\":\"1001\",\"text\":\"Review text long enough\",\"rating\":4.0}"),
				delete("/api/v1/reviews/1"),
				post("/api/v1/reviews/1/like"),
				delete("/api/v1/reviews/1/like"),
				delete("/api/v1/users/me"),
				put("/api/v1/users/me/profile").contentType(APPLICATION_JSON).content("{\"email\":\"listener@example.com\"}"),
				put("/api/v1/users/me/password").contentType(APPLICATION_JSON)
						.content("{\"currentPassword\":\"secret123\",\"newPassword\":\"newsecret123\"}"),
				multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
						.file(new MockMultipartFile("file", "avatar.png", "image/png", "png".getBytes())),
				delete("/api/v1/users/me/avatar"));
	}
}
