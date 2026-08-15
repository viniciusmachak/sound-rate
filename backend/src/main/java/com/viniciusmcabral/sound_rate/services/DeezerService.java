package com.viniciusmcabral.sound_rate.services;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumSearchResponseDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistAlbumsResponseDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistSearchResponseDTO;

@Service
public class DeezerService {

	private static final Logger logger = LoggerFactory.getLogger(DeezerService.class);
	private final RestTemplate restTemplate;
	private final String deezerApiUrl = "https://api.deezer.com";

	public DeezerService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public List<DeezerAlbumDTO> searchAlbums(String query) {
		logger.debug("Searching Deezer albums for query '{}'.", query);
		return getOrDefault(
				() -> restTemplate.getForObject(buildUri("/search/album").queryParam("q", query).toUriString(),
						DeezerAlbumSearchResponseDTO.class),
				response -> response != null ? response.data() : Collections.emptyList(),
				Collections.emptyList(),
				"Failed to search Deezer albums for query '{}': {}.",
				query);
	}

	public List<DeezerArtistDTO> searchArtists(String query) {
		return getOrDefault(
				() -> restTemplate.getForObject(buildUri("/search/artist").queryParam("q", query).toUriString(),
						DeezerArtistSearchResponseDTO.class),
				response -> response != null ? response.data() : Collections.emptyList(),
				Collections.emptyList(),
				"Failed to search Deezer artists for query '{}': {}.",
				query);
	}

	public DeezerArtistDetailsDTO getArtistDetails(String artistId) {
		return getOrDefault(() -> restTemplate.getForObject(buildUri("/artist/{artistId}").build(artistId),
				DeezerArtistDetailsDTO.class), details -> details, null,
				"Failed to fetch Deezer artist details for artist '{}': {}.", artistId);
	}

	public Page<DeezerAlbumDTO> getArtistAlbums(String artistId, Pageable pageable) {
		int index = pageable.getPageNumber() * pageable.getPageSize();
		int limit = pageable.getPageSize();

		return getOrDefault(
				() -> restTemplate.getForObject(
						buildUri("/artist/{artistId}/albums").queryParam("index", index).queryParam("limit", limit)
								.build(artistId),
						DeezerArtistAlbumsResponseDTO.class),
				response -> response != null ? new PageImpl<>(response.data(), pageable, response.total())
						: Page.empty(pageable),
				Page.empty(pageable),
				"Failed to fetch Deezer albums for artist '{}' at page {}: {}.",
				artistId, pageable.getPageNumber());
	}

	public DeezerAlbumDTO getAlbumDetails(String albumId) {
		logger.debug("Fetching Deezer album details for album '{}'.", albumId);
		return getOrDefault(() -> restTemplate.getForObject(buildUri("/album/{albumId}").build(albumId),
				DeezerAlbumDTO.class), album -> album, null,
				"Failed to fetch Deezer album details for album '{}': {}.", albumId);
	}

	private UriComponentsBuilder buildUri(String path) {
		return UriComponentsBuilder.fromUriString(deezerApiUrl).path(path);
	}

	private <T, R> R getOrDefault(Supplier<T> requestSupplier, Function<T, R> responseMapper, R fallbackValue,
			String logMessage, Object... logArgs) {
		try {
			return responseMapper.apply(requestSupplier.get());
		} catch (RestClientException exception) {
			Object[] fullLogArgs = new Object[logArgs.length + 1];
			System.arraycopy(logArgs, 0, fullLogArgs, 0, logArgs.length);
			fullLogArgs[logArgs.length] = exception.getMessage();
			logger.error(logMessage, fullLogArgs);
			return fallbackValue;
		}
	}
}
