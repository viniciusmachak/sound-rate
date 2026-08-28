package com.viniciusmcabral.sound_rate.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileDTO(

		@NotBlank(message = "Email cannot be blank") 
		@Email(message = "Email should be valid") String email,
		@Size(max = 280, message = "Bio must be at most 280 characters") String bio) {

	public UpdateProfileDTO(String email) {
		this(email, null);
	}
}
