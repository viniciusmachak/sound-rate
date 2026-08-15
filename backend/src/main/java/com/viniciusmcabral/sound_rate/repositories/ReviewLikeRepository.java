package com.viniciusmcabral.sound_rate.repositories;

import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.models.ReviewLikeModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLikeModel, Long> {

	interface ReviewLikeCountProjection {
		Long getReviewId();

		long getLikesCount();
	}

	Optional<ReviewLikeModel> findByUserAndAlbumReview(UserModel user, AlbumReviewModel albumReview);

	void deleteByUserAndAlbumReview(UserModel user, AlbumReviewModel albumReview);

	long countByAlbumReview(AlbumReviewModel albumReview);

	@Query("""
			SELECT rl.albumReview.id AS reviewId, COUNT(rl) AS likesCount
			FROM ReviewLike rl
			WHERE rl.albumReview.id IN :reviewIds
			GROUP BY rl.albumReview.id
			""")
	List<ReviewLikeCountProjection> countByAlbumReviewIds(@Param("reviewIds") Collection<Long> reviewIds);

	@Query("""
			SELECT rl.albumReview.id
			FROM ReviewLike rl
			WHERE rl.user = :user AND rl.albumReview.id IN :reviewIds
			""")
	List<Long> findLikedReviewIdsByUserAndAlbumReviewIds(@Param("user") UserModel user,
			@Param("reviewIds") Collection<Long> reviewIds);
}
