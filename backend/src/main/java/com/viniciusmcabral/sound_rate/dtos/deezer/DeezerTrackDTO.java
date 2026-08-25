package com.viniciusmcabral.sound_rate.dtos.deezer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerTrackDTO(long id, boolean readable, String title, @JsonProperty("title_short") String titleShort,
		@JsonProperty("title_version") String titleVersion, String link, int duration, int rank,
		@JsonProperty("explicit_lyrics") boolean explicitLyrics, String preview,
		float bpm, List<DeezerContributorDTO> contributors, DeezerArtistDTO artist, DeezerSimpleAlbumDTO album) {
}
