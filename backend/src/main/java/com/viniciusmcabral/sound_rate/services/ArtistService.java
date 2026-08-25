package com.viniciusmcabral.sound_rate.services;

import java.util.NoSuchElementException;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerTrackDTO;
import com.viniciusmcabral.sound_rate.dtos.response.ArtistPageDTO;

@Service
public class ArtistService {

	private final DeezerService deezerService;
	private final ArtistFollowService artistFollowService;

	public ArtistService(DeezerService deezerService, ArtistFollowService artistFollowService) {
		this.deezerService = deezerService;
		this.artistFollowService = artistFollowService;
	}

	public ArtistPageDTO getArtistPageDetails(String artistId, Pageable pageable) {
		DeezerArtistDetailsDTO artistDetails = deezerService.getArtistDetails(artistId);
		if (artistDetails == null) {
			throw new NoSuchElementException("Artist not found on Deezer with ID: " + artistId);
		}

		Page<DeezerAlbumDTO> albums = deezerService.getArtistAlbums(artistId, pageable);
		List<DeezerTrackDTO> popularTracks = deezerService.getArtistTopTracks(artistId);
		if (popularTracks == null) popularTracks = Collections.emptyList();

		return new ArtistPageDTO(artistDetails, albums, popularTracks,
				artistFollowService.countFollowers(artistId), artistFollowService.isFollowedByCurrentUser(artistId));
	}
}
