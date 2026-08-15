package com.viniciusmcabral.sound_rate.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.viniciusmcabral.sound_rate.dtos.request.RegisterRequestDTO;
import com.viniciusmcabral.sound_rate.models.PasswordResetTokenModel;
import com.viniciusmcabral.sound_rate.repositories.PasswordResetTokenRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;
import com.viniciusmcabral.sound_rate.support.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private TokenService tokenService;

	@Mock
	private PasswordResetTokenRepository tokenRepository;

	@Mock
	private EmailService emailService;

	@InjectMocks
	private AuthService authService;

	@Test
	void loadUserByUsernameRejectsInactiveOrMissingUser() {
		when(userRepository.findByLoginAndActiveTrue("ghost")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.loadUserByUsername("ghost"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("ghost");
	}

	@Test
	void registerUserPersistsNewUserAndSendsWelcomeEmail() {
		var request = new RegisterRequestDTO("listener", "listener@example.com", "plain-secret");
		when(userRepository.findByUsername("listener")).thenReturn(Optional.empty());
		when(userRepository.findByEmail("listener@example.com")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("plain-secret")).thenReturn("encoded-secret");
		when(tokenService.generateToken(any())).thenReturn("jwt-token");

		var response = authService.registerUser(request);

		ArgumentCaptor<com.viniciusmcabral.sound_rate.models.UserModel> userCaptor = ArgumentCaptor.forClass(
				com.viniciusmcabral.sound_rate.models.UserModel.class);
		verify(userRepository).save(userCaptor.capture());
		verify(emailService).sendWelcomeEmail("listener@example.com", "listener");
		assertThat(userCaptor.getValue().getAvatarUrl()).contains("listener");
		assertThat(response.token()).isEqualTo("jwt-token");
		assertThat(response.user().username()).isEqualTo("listener");
	}

	@Test
	void requestPasswordResetCreatesFreshTokenAndSendsMail() {
		var user = TestDataFactory.user(1L, "listener");
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		authService.requestPasswordReset(user.getEmail());

		ArgumentCaptor<PasswordResetTokenModel> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenModel.class);
		verify(tokenRepository).deleteByUser(user);
		verify(tokenRepository).save(tokenCaptor.capture());
		verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), eq(user.getUsername()),
				contains(tokenCaptor.getValue().getToken()));
		assertThat(tokenCaptor.getValue().getExpiryDate()).isAfter(LocalDateTime.now().plusMinutes(59));
	}

	@Test
	void requestPasswordResetForUnknownEmailDoesNotLeakOrPersistAnything() {
		when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

		authService.requestPasswordReset("missing@example.com");

		verify(tokenRepository, never()).save(any());
		verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
	}

	@Test
	void performPasswordResetUpdatesPasswordAndDeletesToken() {
		var user = TestDataFactory.user(1L, "listener");
		var resetToken = TestDataFactory.resetToken(9L, "reset-token", user, LocalDateTime.now().plusHours(1));
		when(tokenRepository.findByToken("reset-token")).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.encode("new-secret")).thenReturn("encoded-new-secret");

		authService.performPasswordReset("reset-token", "new-secret");

		assertThat(user.getPassword()).isEqualTo("encoded-new-secret");
		verify(userRepository).save(user);
		verify(tokenRepository).delete(resetToken);
	}

	@Test
	void performPasswordResetRejectsExpiredToken() {
		var user = TestDataFactory.user(1L, "listener");
		var resetToken = TestDataFactory.resetToken(9L, "expired-token", user, LocalDateTime.now().minusMinutes(1));
		when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(resetToken));

		assertThatThrownBy(() -> authService.performPasswordReset("expired-token", "new-secret"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("expired");

		verify(tokenRepository).delete(resetToken);
		verify(userRepository, never()).save(any());
	}
}
