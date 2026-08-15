package com.viniciusmcabral.sound_rate.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viniciusmcabral.sound_rate.dtos.request.RegisterRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AuthResponseDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserDTO;
import com.viniciusmcabral.sound_rate.models.PasswordResetTokenModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.PasswordResetTokenRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;

@Service
public class AuthService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;
	private final PasswordResetTokenRepository tokenRepository;
	private final EmailService emailService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService,
			PasswordResetTokenRepository tokenRepository, EmailService emailService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.tokenRepository = tokenRepository;
		this.emailService = emailService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByLoginAndActiveTrue(username).orElseThrow(
				() -> new UsernameNotFoundException("User not found or is inactive with identifier: " + username));
	}

	@Transactional
	public AuthResponseDTO registerUser(RegisterRequestDTO data) {
		if (userRepository.findByUsername(data.username()).isPresent()) {
			logger.warn("Registration rejected: username '{}' already exists.", data.username());
			throw new IllegalStateException("Username already exists");
		}

		if (userRepository.findByEmail(data.email()).isPresent()) {
			logger.warn("Registration rejected: email '{}' already in use.", data.email());
			throw new IllegalStateException("Email already in use");
		}

		UserModel newUser = new UserModel(data.username(), data.email(), passwordEncoder.encode(data.password()));

		String avatarUrl = "https://api.dicebear.com/8.x/initials/svg?seed=" + newUser.getUsername();
		newUser.setAvatarUrl(avatarUrl);

		userRepository.save(newUser);
		logger.info("User '{}' registered successfully.", newUser.getUsername());
		emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getUsername());

		String token = tokenService.generateToken(newUser);
		UserDTO userDTO = new UserDTO(newUser.getId(), newUser.getUsername(), newUser.getAvatarUrl());

		return new AuthResponseDTO(token, userDTO);
	}

	@Transactional
	public void requestPasswordReset(String userEmail) {
		Optional<UserModel> userOpt = userRepository.findByEmail(userEmail);

		if (userOpt.isEmpty()) {
			logger.info("Password reset requested for non-existent email '{}'.", userEmail);
			return;
		}

		UserModel user = userOpt.get();
		tokenRepository.deleteByUser(user);

		String tokenString = UUID.randomUUID().toString();
		PasswordResetTokenModel passwordResetToken = new PasswordResetTokenModel();
		passwordResetToken.setToken(tokenString);
		passwordResetToken.setUser(user);
		passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
		tokenRepository.save(passwordResetToken);
		logger.info("Issued password reset token for user '{}'.", user.getUsername());

		String resetLink = "http://localhost:4200/reset-password?token=" + tokenString;
		emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);
	}

	@Transactional
	public void performPasswordReset(String token, String newPassword) {
		PasswordResetTokenModel resetToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("Invalid password reset token."));

		if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
			tokenRepository.delete(resetToken);
			logger.warn("Rejected expired password reset token for user '{}'.", resetToken.getUser().getUsername());
			throw new IllegalArgumentException("Password reset token has expired.");
		}

		UserModel user = resetToken.getUser();
		String encodedPassword = passwordEncoder.encode(newPassword);

		user.setPassword(encodedPassword);
		userRepository.save(user);
		tokenRepository.delete(resetToken);
		logger.info("Completed password reset for user '{}'.", user.getUsername());
	}
}
