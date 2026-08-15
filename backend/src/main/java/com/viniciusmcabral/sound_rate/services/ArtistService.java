package com.viniciusmcabral.sound_rate.services;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.dtos.response.ArtistPageDTO;

@Service
public class ArtistService {

	private final DeezerService deezerService;

	public ArtistService(DeezerService deezerService) {
		this.deezerService = deezerService;
	}

	public ArtistPageDTO getArtistPageDetails(String artistId, Pageable pageable) {
		DeezerArtistDetailsDTO artistDetails = deezerService.getArtistDetails(artistId);
		if (artistDetails == null) {
			throw new NoSuchElementException("Artist not found on Deezer with ID: " + artistId);
		}

		Page<DeezerAlbumDTO> albums = deezerService.getArtistAlbums(artistId, pageable);

		return new ArtistPageDTO(artistDetails, albums);
	}
}
