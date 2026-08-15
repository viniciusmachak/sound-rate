package com.viniciusmcabral.sound_rate.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.viniciusmcabral.sound_rate.models.UserModel;

@Repository
public interface 	UserRepository extends JpaRepository<UserModel, Long> {

	Optional<UserModel> findByUsernameAndActiveTrue(String username);

	Optional<UserModel> findByEmailAndActiveTrue(String email);

	@Query("SELECT u FROM User u WHERE (u.username = :login OR u.email = :login) AND u.active = true")
	Optional<UserModel> findByLoginAndActiveTrue(@Param("login") String login);

	Optional<UserModel> findByUsername(String username);

	Optional<UserModel> findByEmail(String email);
	
	@Query("SELECT u FROM User u WHERE u.active = true AND lower(u.username) LIKE lower(concat('%', :query, '%'))")
    Page<UserModel> searchByUsername(String query, Pageable pageable);
}
