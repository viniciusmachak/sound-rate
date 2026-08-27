package com.viniciusmcabral.sound_rate.dtos.deezer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeezerAlbumDTO(long id, String title, String link, @JsonProperty("cover_medium") String coverMedium,
		@JsonProperty("cover_xl") String coverXl, DeezerArtistDTO artist,
		@JsonProperty("record_type") String recordType,
		@JsonProperty("release_date") String releaseDate, int duration, int fans, double rating,
		@JsonProperty("explicit_lyrics") boolean explicitLyrics, String label, String copyright,
		DeezerGenreListDTO genres, List<DeezerContributorDTO> contributors, DeezerTracklistDTO tracks,
		@JsonProperty("community_score") Double communityScore) {
}
