package com.viniciusmcabral.sound_rate.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viniciusmcabral.sound_rate.models.ArtistFollowModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.ArtistFollowRepository;

@Service
public class ArtistFollowService {

	private final ArtistFollowRepository artistFollowRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public ArtistFollowService(ArtistFollowRepository artistFollowRepository,
			AuthenticatedUserService authenticatedUserService) {
		this.artistFollowRepository = artistFollowRepository;
		this.authenticatedUserService = authenticatedUserService;
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
}
