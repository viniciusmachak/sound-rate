package com.viniciusmcabral.sound_rate.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viniciusmcabral.sound_rate.models.ArtistFollowModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

@Repository
public interface ArtistFollowRepository extends JpaRepository<ArtistFollowModel, Long> {

	Optional<ArtistFollowModel> findByUserAndArtistId(UserModel user, String artistId);

	void deleteByUserAndArtistId(UserModel user, String artistId);

	long countByArtistId(String artistId);
}
