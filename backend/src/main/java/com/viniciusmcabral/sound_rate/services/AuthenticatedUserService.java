package com.viniciusmcabral.sound_rate.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.viniciusmcabral.sound_rate.models.UserModel;

@Service
public class AuthenticatedUserService {

	public UserModel requireCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication != null ? authentication.getPrincipal() : null;

		if (principal instanceof UserModel user) {
			return user;
		}

		throw new IllegalStateException("Could not retrieve authenticated user.");
	}

	public UserModel getCurrentUserOrNull() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return null;
		}

		return authentication.getPrincipal() instanceof UserModel user ? user : null;
	}
}
