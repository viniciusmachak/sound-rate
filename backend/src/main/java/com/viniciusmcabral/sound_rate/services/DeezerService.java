package com.viniciusmcabral.sound_rate.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerTrackDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerTracklistDTO;

@Service
public class DeezerService {

	private static final Logger logger = LoggerFactory.getLogger(DeezerService.class);
	private static final Pattern FEATURED_ARTIST_PATTERN = Pattern
			.compile("(?i)(?:\\bfeat(?:uring)?\\.?\\s+|\\bft\\.?\\s+|\\(with\\s+)");
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

	public List<DeezerTrackDTO> getArtistTopTracks(String artistId) {
		return getOrDefault(
				() -> restTemplate.getForObject(
						buildUri("/artist/{artistId}/top").queryParam("limit", 10).build(artistId),
						DeezerTracklistDTO.class),
				response -> response != null && response.data() != null ? response.data() : Collections.emptyList(),
				Collections.emptyList(),
				"Failed to fetch Deezer top tracks for artist '{}': {}.", artistId);
	}

	public List<DeezerAlbumDTO> getArtistAlbums(String artistId) {
		final int pageSize = 100;
		List<DeezerAlbumDTO> albums = new ArrayList<>();
		int index = 0;
		int total = Integer.MAX_VALUE;

		while (index < total) {
			int currentIndex = index;
			DeezerArtistAlbumsResponseDTO response = getOrDefault(
					() -> restTemplate.getForObject(
							buildUri("/artist/{artistId}/albums").queryParam("index", currentIndex)
									.queryParam("limit", pageSize).build(artistId),
							DeezerArtistAlbumsResponseDTO.class),
					Function.identity(), null,
					"Failed to fetch Deezer albums for artist '{}' at index {}: {}.", artistId, currentIndex);

			if (response == null || response.data() == null || response.data().isEmpty()) {
				break;
			}

			albums.addAll(response.data());
			total = response.total();
			index += response.data().size();
		}

		return List.copyOf(albums);
	}

	public DeezerAlbumDTO getAlbumDetails(String albumId) {
		logger.debug("Fetching Deezer album details for album '{}'.", albumId);
		return getOrDefault(() -> restTemplate.getForObject(buildUri("/album/{albumId}").build(albumId),
				DeezerAlbumDTO.class), this::enrichFeaturedTrackContributors, null,
				"Failed to fetch Deezer album details for album '{}': {}.", albumId);
	}

	private DeezerAlbumDTO enrichFeaturedTrackContributors(DeezerAlbumDTO album) {
		if (album == null || album.tracks() == null || album.tracks().data() == null) {
			return album;
		}

		List<DeezerTrackDTO> tracks = new ArrayList<>(album.tracks().data());
		boolean changed = false;

		for (int index = 0; index < tracks.size(); index++) {
			DeezerTrackDTO track = tracks.get(index);
			if (track == null) {
				continue;
			}

			boolean hasFeaturedContributor = track.contributors() != null && track.contributors().stream()
					.anyMatch(contributor -> contributor != null
							&& (album.artist() == null || contributor.id() != album.artist().id()));

			if (hasFeaturedContributor || !FEATURED_ARTIST_PATTERN.matcher(
							String.join(" ", track.title() == null ? "" : track.title(),
									track.titleVersion() == null ? "" : track.titleVersion()))
							.find()) {
				continue;
			}

			DeezerTrackDTO detailedTrack = getOrDefault(
					() -> restTemplate.getForObject(buildUri("/track/{trackId}").build(track.id()), DeezerTrackDTO.class),
					Function.identity(), track,
					"Failed to fetch Deezer contributors for track '{}': {}.", track.id());

			if (detailedTrack != null && detailedTrack.contributors() != null
					&& !detailedTrack.contributors().isEmpty()) {
				tracks.set(index, detailedTrack);
				changed = true;
			}
		}

		if (!changed) {
			return album;
		}

		return new DeezerAlbumDTO(album.id(), album.title(), album.link(), album.coverMedium(), album.coverXl(),
				album.artist(), album.recordType(), album.releaseDate(), album.duration(), album.fans(), album.rating(), album.explicitLyrics(),
				album.label(), album.copyright(), album.genres(), album.contributors(),
				new DeezerTracklistDTO(List.copyOf(tracks)), album.communityScore());
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
