package com.viniciusmcabral.sound_rate.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

public interface AlbumReviewRepository extends JpaRepository<AlbumReviewModel, Long> {

	interface AlbumReviewTextProjection {
		String getAlbumId();

		String getText();
	}

	@EntityGraph(attributePaths = "user")
	Optional<AlbumReviewModel> findByUserAndAlbumId(UserModel user, String albumId);

	List<AlbumReviewModel> findAllByUser(UserModel user, Pageable pageable);

	Page<AlbumReviewModel> findByUser(UserModel user, Pageable pageable);

	long countByUser(UserModel user);

	@Query("SELECT r.albumId FROM AlbumReview r WHERE r.user = :user ORDER BY r.createdAt DESC")
	List<String> findAllAlbumIdsByUser(UserModel user);

	@EntityGraph(attributePaths = "user")
	@Query("SELECT r FROM AlbumReview r WHERE r.albumId = :albumId AND r.user.active = true")
	Page<AlbumReviewModel> findActiveReviewsByAlbumId(@Param("albumId") String albumId, Pageable pageable);

	@EntityGraph(attributePaths = "user")
	@Query("SELECT r FROM AlbumReview r WHERE r.albumId IN :albumIds AND r.user.active = true ORDER BY r.createdAt DESC")
	List<AlbumReviewModel> findRecentActiveReviewsByAlbumIds(@Param("albumIds") List<String> albumIds,
			Pageable pageable);

	@Query("SELECT COUNT(r) FROM AlbumReview r WHERE r.albumId IN :albumIds AND r.user.active = true")
	long countActiveReviewsByAlbumIds(@Param("albumIds") List<String> albumIds);

	@Query("SELECT r.albumId AS albumId, r.text AS text FROM AlbumReview r WHERE r.user = :user AND r.albumId IN :albumIds")
	List<AlbumReviewTextProjection> findReviewTextsByUserAndAlbumIds(@Param("user") UserModel user,
			@Param("albumIds") List<String> albumIds);
}
