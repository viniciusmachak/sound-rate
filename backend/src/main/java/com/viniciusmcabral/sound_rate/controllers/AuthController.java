package com.viniciusmcabral.sound_rate.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.request.ForgotPasswordRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.LoginRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.RegisterRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.request.ResetPasswordRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AuthResponseDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserDTO;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.services.AuthService;
import com.viniciusmcabral.sound_rate.services.TokenService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthenticationManager manager;
	private final TokenService tokenService;
	private final AuthService authService;

	public AuthController(AuthenticationManager manager, TokenService tokenService, AuthService authService) {
		this.manager = manager;
		this.tokenService = tokenService;
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginData) {
		var authToken = new UsernamePasswordAuthenticationToken(loginData.username(), loginData.password());
		var authentication = manager.authenticate(authToken);
		var user = (UserModel) authentication.getPrincipal();
		var tokenJWT = tokenService.generateToken(user);
		var userDto = new UserDTO(user.getId(), user.getUsername(), user.getAvatarUrl(), user.getEmail(), user.getBio());

		return ResponseEntity.ok(new AuthResponseDTO(tokenJWT, userDto));
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterRequestDTO registerDTO) {
		AuthResponseDTO response = authService.registerUser(registerDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO request) {
		authService.requestPasswordReset(request.email());
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/reset-password")
	public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO request) {
		authService.performPasswordReset(request.token(), request.newPassword());
		return ResponseEntity.ok().build();
	}
}
