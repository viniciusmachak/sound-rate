package com.viniciusmcabral.sound_rate.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.viniciusmcabral.sound_rate.dtos.request.ForgotPasswordRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.LoginRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.RegisterRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.ResetPasswordRequestDTO;
import com.viniciusmcabral.sound_rate.support.AbstractIntegrationTest;

class AuthFlowIntegrationTest extends AbstractIntegrationTest {

	@Test
	void registerCreatesUserAndReturnsJwt() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register").contentType(APPLICATION_JSON)
				.content(json(new RegisterRequestDTO("newuser", "newuser@example.com", "secret123"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").isString())
				.andExpect(jsonPath("$.user.username").value("newuser"));

		var savedUser = userRepository.findByUsername("newuser").orElseThrow();
		assertThat(passwordEncoder.matches("secret123", savedUser.getPassword())).isTrue();
	}

	@Test
	void loginReturnsTokenForActiveUser() throws Exception {
		createUser("listener", "secret123");

		mockMvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON)
				.content(json(new LoginRequestDTO("listener", "secret123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isString())
				.andExpect(jsonPath("$.user.username").value("listener"));
	}

	@Test
	void resetPasswordFlowAllowsLoggingInWithNewPassword() throws Exception {
		createUser("listener", "secret123");

		mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(APPLICATION_JSON)
				.content(json(new ForgotPasswordRequestDTO("listener@example.com"))))
				.andExpect(status().isOk());

		var resetToken = passwordResetTokenRepository.findAll().stream().findFirst().orElseThrow();

		mockMvc.perform(post("/api/v1/auth/reset-password").contentType(APPLICATION_JSON)
				.content(json(new ResetPasswordRequestDTO(resetToken.getToken(), "newsecret123"))))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON)
				.content(json(new LoginRequestDTO("listener", "newsecret123"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isString());
	}

	@Test
	void disabledAccountCannotLogIn() throws Exception {
		createUser("disabled", "secret123", false);

		mockMvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON)
				.content(json(new LoginRequestDTO("disabled", "secret123"))))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Unauthorized"));
	}
}
