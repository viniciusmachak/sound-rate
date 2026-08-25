package com.viniciusmcabral.sound_rate.dtos.deezer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerGenreDTO(long id, String name, String picture) {
}
