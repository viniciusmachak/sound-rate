package com.viniciusmcabral.sound_rate.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.viniciusmcabral.sound_rate.models.FollowModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

@Repository
public interface FollowRepository extends JpaRepository<FollowModel, Long> {

	Optional<FollowModel> findByFollowerAndFollowing(UserModel follower, UserModel following);

	void deleteByFollowerAndFollowing(UserModel follower, UserModel following);

	@EntityGraph(attributePaths = "follower")
	@Query("SELECT f FROM Follow f WHERE f.following = :user AND f.follower.active = true")
	Page<FollowModel> findActiveFollowersByUser(UserModel user, Pageable pageable);

	@EntityGraph(attributePaths = "following")
	@Query("SELECT f FROM Follow f WHERE f.follower = :user AND f.following.active = true")
	Page<FollowModel> findActiveFollowingByUser(UserModel user, Pageable pageable);

	@Query("SELECT count(f) FROM Follow f WHERE f.following = :user AND f.follower.active = true")
	long countActiveFollowersByUser(@Param("user") UserModel user);

	@Query("SELECT count(f) FROM Follow f WHERE f.follower = :user AND f.following.active = true")
	long countActiveFollowingByUser(@Param("user") UserModel user);
}
