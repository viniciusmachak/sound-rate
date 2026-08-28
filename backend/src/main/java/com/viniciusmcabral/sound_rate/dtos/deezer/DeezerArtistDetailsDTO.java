package com.viniciusmcabral.sound_rate.dtos.deezer;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerArtistDetailsDTO(long id, String name, String link, @JsonProperty("picture_xl") String pictureXl,
		@JsonProperty("picture_medium") String pictureMedium, @JsonProperty("nb_album") int numberOfAlbums,
		@JsonProperty("nb_fan") int numberOfFans, Page<DeezerAlbumDTO> albums) {
}
