package com.viniciusmcabral.sound_rate.config.security.filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;
import com.viniciusmcabral.sound_rate.services.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);
	private final TokenService tokenService;
	private final UserRepository userRepository;

	public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
		this.tokenService = tokenService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String tokenJWT = recoverToken(request);
		logger.debug("Processing request '{}' with bearer token present: {}.", request.getRequestURI(),
				tokenJWT != null);

		if (tokenJWT != null) {
			try {
				String subject = tokenService.getSubject(tokenJWT);
				var user = userRepository.findByUsernameAndActiveTrue(subject);

				if (user.isPresent()) {
					var authentication = new UsernamePasswordAuthenticationToken(user.get(), null,
							user.get().getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(authentication);
					logger.debug("Authenticated user '{}'.", subject);
				} else {
					logger.warn("Token subject '{}' could not be matched to an active user.", subject);
				}
			} catch (JWTVerificationException exception) {
				logger.warn("Rejected invalid JWT token: {}.", exception.getMessage());
			}
		}

		filterChain.doFilter(request, response);
	}

	private String recoverToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7);
		}
		return null;
	}
}
