package com.viniciusmcabral.sound_rate.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.viniciusmcabral.sound_rate.models.PasswordResetTokenModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenModel, Long> {
	
	Optional<PasswordResetTokenModel> findByToken(String token);

	Optional<PasswordResetTokenModel> findByUser(UserModel user);

	@Modifying
	@Query("DELETE FROM PasswordResetToken t WHERE t.user = :user")
	void deleteByUser(@Param("user") UserModel user);
}
