package com.viniciusmcabral.sound_rate.dtos.response;

import org.springframework.data.domain.Page;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerTrackDTO;

import java.util.List;

public record ArtistPageDTO(DeezerArtistDetailsDTO artistDetails, Page<DeezerAlbumDTO> albums,
		List<DeezerTrackDTO> popularTracks, long followersCount, boolean isFollowedByCurrentUser,
		ArtistCommunityDTO community) {
}
