package com.viniciusmcabral.sound_rate.repositories;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.viniciusmcabral.sound_rate.models.ListenLaterModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

@Repository
public interface ListenLaterRepository extends JpaRepository<ListenLaterModel, Long> {

	Optional<ListenLaterModel> findByUserAndAlbumId(UserModel user, String albumId);

	void deleteByUserAndAlbumId(UserModel user, String albumId);

	long countByUser(UserModel user);

	Page<ListenLaterModel> findByUser(UserModel user, Pageable pageable);
}
