package com.viniciusmcabral.sound_rate.services;

import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerTrackDTO;
import com.viniciusmcabral.sound_rate.dtos.response.ArtistCommunityDTO;
import com.viniciusmcabral.sound_rate.dtos.response.ArtistPageDTO;
import com.viniciusmcabral.sound_rate.dtos.response.ArtistRecentReviewDTO;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumRatingRepository;

@Service
public class ArtistService {

	private final DeezerService deezerService;
	private final ArtistFollowService artistFollowService;
	private final AlbumRatingRepository albumRatingRepository;
	private final AlbumReviewRepository albumReviewRepository;
	private final ReviewService reviewService;

	public ArtistService(DeezerService deezerService, ArtistFollowService artistFollowService,
			AlbumRatingRepository albumRatingRepository, AlbumReviewRepository albumReviewRepository,
			ReviewService reviewService) {
		this.deezerService = deezerService;
		this.artistFollowService = artistFollowService;
		this.albumRatingRepository = albumRatingRepository;
		this.albumReviewRepository = albumReviewRepository;
		this.reviewService = reviewService;
	}

	public ArtistPageDTO getArtistPageDetails(String artistId, String category, String sort, String direction,
			Pageable pageable) {
		DeezerArtistDetailsDTO artistDetails = deezerService.getArtistDetails(artistId);
		if (artistDetails == null) {
			throw new NoSuchElementException("Artist not found on Deezer with ID: " + artistId);
		}

		List<DeezerAlbumDTO> discography = enrichCommunityScores(deezerService.getArtistAlbums(artistId));
		List<DeezerAlbumDTO> filteredAlbums = discography.stream()
				.filter(album -> matchesCategory(album, category))
				.sorted(albumComparator(sort, direction))
				.toList();
		Page<DeezerAlbumDTO> albums = toPage(filteredAlbums, pageable);
		ArtistCommunityDTO community = buildCommunity(discography);
		List<DeezerTrackDTO> popularTracks = deezerService.getArtistTopTracks(artistId);
		if (popularTracks == null) popularTracks = Collections.emptyList();

		return new ArtistPageDTO(artistDetails, albums, popularTracks,
				artistFollowService.countFollowers(artistId), artistFollowService.isFollowedByCurrentUser(artistId),
				community);
	}

	private ArtistCommunityDTO buildCommunity(List<DeezerAlbumDTO> discography) {
		if (discography.isEmpty()) {
			return new ArtistCommunityDTO(null, null, 0, 0, Collections.emptyList(), Collections.emptyList());
		}

		List<String> albumIds = discography.stream().map(album -> Long.toString(album.id())).toList();
		List<DeezerAlbumDTO> favorites = discography.stream()
				.filter(album -> album.communityScore() != null)
				.sorted(Comparator.comparing(DeezerAlbumDTO::communityScore).reversed()
						.thenComparing(Comparator.comparingInt(DeezerAlbumDTO::fans).reversed()))
				.limit(5)
				.toList();

		Map<String, DeezerAlbumDTO> albumsById = discography.stream().collect(Collectors.toMap(
				album -> Long.toString(album.id()), Function.identity(), (first, ignored) -> first));
		List<ArtistRecentReviewDTO> recentReviews = reviewService.getRecentReviewsForAlbums(albumIds, 5).stream()
				.filter(review -> albumsById.containsKey(review.albumId()))
				.map(review -> new ArtistRecentReviewDTO(albumsById.get(review.albumId()), review.review()))
				.toList();

		return new ArtistCommunityDTO(favorites.isEmpty() ? null : favorites.getFirst(),
				albumRatingRepository.findDiscographyAverageRating(albumIds).orElse(null),
				albumRatingRepository.countByAlbumIdIn(albumIds),
				albumReviewRepository.countActiveReviewsByAlbumIds(albumIds), favorites, recentReviews);
	}

	private List<DeezerAlbumDTO> enrichCommunityScores(List<DeezerAlbumDTO> albums) {
		if (albums.isEmpty()) return albums;

		List<String> albumIds = albums.stream().map(album -> Long.toString(album.id())).toList();
		Map<String, Double> communityScores = new HashMap<>();
		for (Object[] result : albumRatingRepository.findCommunityAverageRatings(albumIds)) {
			communityScores.put((String) result[0], (Double) result[1]);
		}

		return albums.stream().map(album -> new DeezerAlbumDTO(album.id(), album.title(), album.link(),
				album.coverMedium(), album.coverXl(), album.artist(), album.recordType(), album.releaseDate(),
				album.duration(), album.fans(), album.rating(), album.explicitLyrics(), album.label(), album.copyright(),
				album.genres(), album.contributors(), album.tracks(), communityScores.get(Long.toString(album.id()))))
				.toList();
	}

	private boolean matchesCategory(DeezerAlbumDTO album, String category) {
		String normalizedCategory = normalize(category);
		String recordType = normalize(album.recordType());

		return switch (normalizedCategory) {
			case "albums" -> recordType.equals("album");
			case "singles" -> recordType.equals("single") || recordType.equals("ep");
			case "compilations" -> recordType.equals("compile") || recordType.equals("compilation");
			default -> true;
		};
	}

	private Comparator<DeezerAlbumDTO> albumComparator(String sort, String direction) {
		boolean ascending = normalize(direction).equals("asc");
		Comparator<String> textOrder = ascending ? Comparator.naturalOrder() : Comparator.reverseOrder();
		Comparator<Double> scoreOrder = ascending ? Comparator.naturalOrder() : Comparator.reverseOrder();
		Comparator<DeezerAlbumDTO> comparator = switch (normalize(sort)) {
			case "release" -> Comparator.comparing(DeezerAlbumDTO::releaseDate,
					Comparator.nullsLast(textOrder));
			case "community" -> Comparator.comparing(DeezerAlbumDTO::communityScore,
					Comparator.nullsLast(scoreOrder));
			default -> ascending
					? Comparator.comparingInt(DeezerAlbumDTO::fans)
					: Comparator.comparingInt(DeezerAlbumDTO::fans).reversed();
		};

		return comparator.thenComparing(DeezerAlbumDTO::title, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
	}

	private Page<DeezerAlbumDTO> toPage(List<DeezerAlbumDTO> albums, Pageable pageable) {
		int start = Math.min((int) pageable.getOffset(), albums.size());
		int end = Math.min(start + pageable.getPageSize(), albums.size());
		return new PageImpl<>(albums.subList(start, end), pageable, albums.size());
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}
}
