package com.viniciusmcabral.sound_rate.dtos.deezer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerGenreListDTO(List<DeezerGenreDTO> data) {
}
