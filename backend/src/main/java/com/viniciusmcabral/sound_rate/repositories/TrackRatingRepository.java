package com.viniciusmcabral.sound_rate.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.viniciusmcabral.sound_rate.models.TrackRatingModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

public interface TrackRatingRepository extends JpaRepository<TrackRatingModel, Long> {

	List<TrackRatingModel> findByUserAndAlbumId(UserModel user, String albumId);

	Optional<TrackRatingModel> findByUserAndTrackId(UserModel user, String trackId);

	Optional<TrackRatingModel> findByUserAndAlbumIdAndTrackId(UserModel user, String albumId, String trackId);

	@Query("SELECT AVG(tr.rating) FROM TrackRating tr WHERE tr.albumId = :albumId")
	Optional<Double> findAverageRatingByAlbumId(String albumId);

	void deleteByUserAndTrackId(UserModel user, String trackId);

	void deleteAllByUserAndAlbumId(UserModel user, String albumId);

	List<TrackRatingModel> findAllByUser(UserModel user, Pageable pagealble);

	long countByUser(UserModel user);
}
