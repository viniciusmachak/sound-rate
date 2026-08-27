package com.viniciusmcabral.sound_rate.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.viniciusmcabral.sound_rate.models.AlbumRatingModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

public interface AlbumRatingRepository extends JpaRepository<AlbumRatingModel, Long> {

	Optional<AlbumRatingModel> findByUserAndAlbumId(UserModel user, String albumId);

	void deleteByUserAndAlbumId(UserModel user, String albumId);

	List<AlbumRatingModel> findAllByUser(UserModel user, Pageable pageable);

	@Query("SELECT AVG(ar.rating) FROM AlbumRating ar WHERE ar.albumId = :albumId")
	Optional<Double> findCommunityAverageRating(String albumId);

	@Query("SELECT ar.albumId, AVG(ar.rating) FROM AlbumRating ar WHERE ar.albumId IN :albumIds GROUP BY ar.albumId")
	List<Object[]> findCommunityAverageRatings(List<String> albumIds);

	@Query("SELECT AVG(ar.rating) FROM AlbumRating ar WHERE ar.albumId IN :albumIds")
	Optional<Double> findDiscographyAverageRating(List<String> albumIds);

	long countByAlbumIdIn(List<String> albumIds);

	long countByUser(UserModel user);

	@Query("SELECT r.albumId FROM AlbumRating r WHERE r.user = :user ORDER BY r.id DESC")
	List<String> findAllAlbumIdsByUser(UserModel user);

	Page<AlbumRatingModel> findByUser(UserModel user, Pageable pageable);

	long countByAlbumId(String albumId);

	@Query("SELECT ar.albumId FROM AlbumRating ar " + "GROUP BY ar.albumId " + "HAVING COUNT(ar.albumId) >= 5 "
			+ "ORDER BY (AVG(ar.rating) * LOG10(COUNT(ar.albumId))) DESC")
	Page<String> findTopRatedAlbumIds(Pageable pageable);
}
