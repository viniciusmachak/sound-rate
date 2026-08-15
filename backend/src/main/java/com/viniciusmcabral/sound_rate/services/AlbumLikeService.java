package com.viniciusmcabral.sound_rate.services;

import com.viniciusmcabral.sound_rate.models.AlbumLikeModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlbumLikeService {

	private final AlbumLikeRepository albumLikeRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public AlbumLikeService(AlbumLikeRepository albumLikeRepository,
			AuthenticatedUserService authenticatedUserService) {
		this.albumLikeRepository = albumLikeRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional
	public void likeAlbum(String albumId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();

		if (albumLikeRepository.findByUserAndAlbumId(currentUser, albumId).isEmpty()) {
			AlbumLikeModel newLike = new AlbumLikeModel(currentUser, albumId);
			albumLikeRepository.save(newLike);
		}
	}

	@Transactional
	public void unlikeAlbum(String albumId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		albumLikeRepository.deleteByUserAndAlbumId(currentUser, albumId);
	}
}
