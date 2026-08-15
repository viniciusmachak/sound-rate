package com.viniciusmcabral.sound_rate.integration;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

import com.viniciusmcabral.sound_rate.dtos.request.ResetPasswordRequestDTO;
import com.viniciusmcabral.sound_rate.support.AbstractIntegrationTest;

class ErrorSanitizationIntegrationTest extends AbstractIntegrationTest {

	@Test
	void resetPasswordErrorsDoNotExposeSensitiveTokenOrPasswordDetails() throws Exception {
		mockMvc.perform(post("/api/v1/auth/reset-password").contentType(APPLICATION_JSON)
				.content(json(new ResetPasswordRequestDTO("secret-reset-token", "secret123"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid password reset token."))
				.andExpect(content().string(not(containsString("secret-reset-token"))))
				.andExpect(content().string(not(containsString("secret123"))))
				.andExpect(content().string(not(containsString("Exception"))))
				.andExpect(content().string(not(containsString("trace"))));
	}

	@Test
	void unexpectedErrorsReturnGenericMessageWithoutInternalDetails() throws Exception {
		var currentUser = createUser("listener", "secret123");
		when(storageService.uploadFile(any())).thenThrow(new RuntimeException("token=password stack trace SQLException"));

		mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me/avatar")
				.file(new MockMultipartFile("file", "avatar.png", "image/png", "png".getBytes()))
				.header("Authorization", bearerToken(currentUser))).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("An unexpected error occurred."))
				.andExpect(content().string(not(containsString("token=password"))))
				.andExpect(content().string(not(containsString("SQLException"))))
				.andExpect(content().string(not(containsString("stack"))));
	}
}
