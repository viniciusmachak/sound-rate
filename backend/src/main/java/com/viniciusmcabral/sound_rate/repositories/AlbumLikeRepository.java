package com.viniciusmcabral.sound_rate.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viniciusmcabral.sound_rate.models.AlbumLikeModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

@Repository
public interface AlbumLikeRepository extends JpaRepository<AlbumLikeModel, Long> {

	Optional<AlbumLikeModel> findByUserAndAlbumId(UserModel user, String albumId);

	void deleteByUserAndAlbumId(UserModel user, String albumId);

	long countByAlbumId(String albumId);

	Page<AlbumLikeModel> findByUser(UserModel user, Pageable pageable);
}
