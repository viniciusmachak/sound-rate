package com.viniciusmcabral.sound_rate.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.models.ListenLaterModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.ListenLaterRepository;

@Service
public class ListenLaterService {

	private final ListenLaterRepository listenLaterRepository;
	private final DeezerService deezerService;
	private final AuthenticatedUserService authenticatedUserService;

	public ListenLaterService(ListenLaterRepository listenLaterRepository, DeezerService deezerService,
			AuthenticatedUserService authenticatedUserService) {
		this.listenLaterRepository = listenLaterRepository;
		this.deezerService = deezerService;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional
	public void addAlbum(String albumId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		
		if (listenLaterRepository.findByUserAndAlbumId(currentUser, albumId).isEmpty()) {
			ListenLaterModel newEntry = new ListenLaterModel(currentUser, albumId);
			listenLaterRepository.save(newEntry);
		}
	}

	@Transactional
	public void removeAlbum(String albumId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		listenLaterRepository.deleteByUserAndAlbumId(currentUser, albumId);
	}

	@Transactional(readOnly = true)
	public Page<DeezerAlbumDTO> getListenLaterList(Pageable pageable) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		Page<ListenLaterModel> entries = listenLaterRepository.findByUser(currentUser, pageable);
		
		return entries.map(entry -> deezerService.getAlbumDetails(entry.getAlbumId()));
	}
}
