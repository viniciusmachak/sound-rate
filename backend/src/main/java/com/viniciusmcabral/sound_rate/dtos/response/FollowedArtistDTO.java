package com.viniciusmcabral.sound_rate.dtos.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FollowedArtistDTO(long id, String name,
		@JsonProperty("picture_medium") String pictureMedium, LocalDateTime followedAt) {

	public FollowedArtistDTO(long id, String name, String pictureMedium) {
		this(id, name, pictureMedium, null);
	}
}
