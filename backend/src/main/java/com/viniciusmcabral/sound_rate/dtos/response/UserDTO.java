package com.viniciusmcabral.sound_rate.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDTO(Long id, String username, String avatarUrl, String email, String bio) {

	public UserDTO(Long id, String username, String avatarUrl) {
		this(id, username, avatarUrl, null, null);
	}
}
