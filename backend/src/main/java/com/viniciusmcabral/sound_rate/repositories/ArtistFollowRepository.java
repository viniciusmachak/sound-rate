package com.viniciusmcabral.sound_rate.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viniciusmcabral.sound_rate.models.ArtistFollowModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

@Repository
public interface ArtistFollowRepository extends JpaRepository<ArtistFollowModel, Long> {

	Optional<ArtistFollowModel> findByUserAndArtistId(UserModel user, String artistId);

	void deleteByUserAndArtistId(UserModel user, String artistId);

	long countByUser(UserModel user);

	long countByArtistId(String artistId);

	Page<ArtistFollowModel> findByUser(UserModel user, Pageable pageable);

	List<ArtistFollowModel> findByUser(UserModel user);
}
