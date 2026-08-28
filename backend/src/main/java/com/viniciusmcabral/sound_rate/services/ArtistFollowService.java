package com.viniciusmcabral.sound_rate.services;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDetailsDTO;
import com.viniciusmcabral.sound_rate.dtos.response.FollowedArtistDTO;
import com.viniciusmcabral.sound_rate.models.ArtistFollowModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.ArtistFollowRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;

@Service
public class ArtistFollowService {

	private final ArtistFollowRepository artistFollowRepository;
	private final AuthenticatedUserService authenticatedUserService;
	private final UserRepository userRepository;
	private final DeezerService deezerService;

	public ArtistFollowService(ArtistFollowRepository artistFollowRepository,
			AuthenticatedUserService authenticatedUserService, UserRepository userRepository,
			DeezerService deezerService) {
		this.artistFollowRepository = artistFollowRepository;
		this.authenticatedUserService = authenticatedUserService;
		this.userRepository = userRepository;
		this.deezerService = deezerService;
	}

	@Transactional
	public void followArtist(String artistId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		if (artistFollowRepository.findByUserAndArtistId(currentUser, artistId).isEmpty()) {
			artistFollowRepository.save(new ArtistFollowModel(currentUser, artistId));
		}
	}

	@Transactional
	public void unfollowArtist(String artistId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		artistFollowRepository.deleteByUserAndArtistId(currentUser, artistId);
	}

	@Transactional(readOnly = true)
	public boolean isFollowedByCurrentUser(String artistId) {
		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();
		return currentUser != null && artistFollowRepository.findByUserAndArtistId(currentUser, artistId).isPresent();
	}

	@Transactional(readOnly = true)
	public long countFollowers(String artistId) {
		return artistFollowRepository.countByArtistId(artistId);
	}

	public Page<FollowedArtistDTO> getFollowingArtists(String username, String query, Pageable pageable) {
		UserModel user = userRepository.findByUsernameAndActiveTrue(username)
				.orElseThrow(() -> new NoSuchElementException("User not found: " + username));

		if (query == null || query.isBlank()) {
			return artistFollowRepository.findByUser(user, pageable).map(this::toFollowedArtistDto);
		}

		String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
		List<FollowedArtistDTO> matchingArtists = artistFollowRepository.findByUser(user).stream()
				.map(this::toFollowedArtistDto)
				.filter(artist -> artist.name().toLowerCase(Locale.ROOT).contains(normalizedQuery))
				.toList();
		int start = Math.min((int) pageable.getOffset(), matchingArtists.size());
		int end = Math.min(start + pageable.getPageSize(), matchingArtists.size());
		return new PageImpl<>(matchingArtists.subList(start, end), pageable, matchingArtists.size());
	}

	private FollowedArtistDTO toFollowedArtistDto(ArtistFollowModel follow) {
		DeezerArtistDetailsDTO artist = deezerService.getArtistDetails(follow.getArtistId());
		if (artist == null) {
			throw new NoSuchElementException("Artist not found on Deezer with ID: " + follow.getArtistId());
		}
		return new FollowedArtistDTO(artist.id(), artist.name(), artist.pictureMedium(), follow.getCreatedAt());
	}
}
