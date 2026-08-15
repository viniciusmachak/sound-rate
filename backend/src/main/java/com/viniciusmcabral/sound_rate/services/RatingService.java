package com.viniciusmcabral.sound_rate.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viniciusmcabral.sound_rate.dtos.request.RatingRequestDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AlbumRatingDTO;
import com.viniciusmcabral.sound_rate.dtos.response.TrackRatingDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserDTO;
import com.viniciusmcabral.sound_rate.models.AlbumRatingModel;
import com.viniciusmcabral.sound_rate.models.TrackRatingModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.TrackRatingRepository;

@Service
public class RatingService {

	private final AlbumRatingRepository albumRatingRepository;
	private final TrackRatingRepository trackRatingRepository;
	private final AlbumReviewRepository albumReviewRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public RatingService(AlbumRatingRepository albumRatingRepository, TrackRatingRepository trackRatingRepository,
			AlbumReviewRepository albumReviewRepository, AuthenticatedUserService authenticatedUserService) {
		this.albumRatingRepository = albumRatingRepository;
		this.trackRatingRepository = trackRatingRepository;
		this.albumReviewRepository = albumReviewRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional
	public void rateAlbumOrTrack(RatingRequestDTO ratingDTO) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();

		if (ratingDTO.trackId() != null && !ratingDTO.trackId().isBlank()) {
			rateTrack(ratingDTO, currentUser);
			updateUserAlbumRatingFromTracks(ratingDTO.albumId(), currentUser);
		} else {
			trackRatingRepository.deleteAllByUserAndAlbumId(currentUser, ratingDTO.albumId());
			rateAlbum(ratingDTO, currentUser);
		}
	}

	private void rateAlbum(RatingRequestDTO ratingDTO, UserModel user) {
		AlbumRatingModel rating = albumRatingRepository.findByUserAndAlbumId(user, ratingDTO.albumId())
				.orElse(new AlbumRatingModel(ratingDTO.albumId(), ratingDTO.rating(), user));

		rating.setRating(ratingDTO.rating());
		albumRatingRepository.save(rating);

		albumReviewRepository.findByUserAndAlbumId(user, ratingDTO.albumId()).ifPresent(review -> {
			review.setRating(ratingDTO.rating());
			albumReviewRepository.save(review);
		});
	}

	private void updateUserAlbumRatingFromTracks(String albumId, UserModel user) {
		List<TrackRatingModel> trackRatings = trackRatingRepository.findByUserAndAlbumId(user, albumId);
		double average = trackRatings.stream().mapToDouble(TrackRatingModel::getRating).average().orElse(0.0);

		RatingRequestDTO albumRatingDTO = new RatingRequestDTO(albumId, null, average);

		rateAlbum(albumRatingDTO, user);
	}

	@Transactional
	public void deleteRating(String albumId, String trackId) {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();

		if (trackId != null && !trackId.isBlank()) {
			trackRatingRepository.deleteByUserAndTrackId(currentUser, trackId);
		} else if (albumId != null && !albumId.isBlank()) {
			albumReviewRepository.findByUserAndAlbumId(currentUser, albumId).ifPresent(albumReviewRepository::delete);
			albumRatingRepository.deleteByUserAndAlbumId(currentUser, albumId);
			trackRatingRepository.deleteAllByUserAndAlbumId(currentUser, albumId);
		} else {
			throw new IllegalArgumentException("Either albumId or trackId must be provided to delete a rating.");
		}
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getUserRatings() {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		Pageable pageRequest = PageRequest.of(0, 20, Sort.by("id").descending());

		List<AlbumRatingDTO> albumRatings = albumRatingRepository.findAllByUser(currentUser, pageRequest).stream()
				.map(this::convertToAlbumRatingDTO).collect(Collectors.toList());
		List<TrackRatingDTO> trackRatings = trackRatingRepository.findAllByUser(currentUser, pageRequest).stream()
				.map(this::convertToTrackRatingDTO).collect(Collectors.toList());

		return Map.of("albumRatings", albumRatings, "trackRatings", trackRatings);
	}

	private AlbumRatingDTO convertToAlbumRatingDTO(AlbumRatingModel rating) {
		UserDTO author = new UserDTO(rating.getUser().getId(), rating.getUser().getUsername(),
				rating.getUser().getAvatarUrl());
		return new AlbumRatingDTO(rating.getId(), rating.getRating(), author);
	}

	private TrackRatingDTO convertToTrackRatingDTO(TrackRatingModel rating) {
		return new TrackRatingDTO(rating.getId(), rating.getRating(), rating.getTrackId());
	}

	private void rateTrack(RatingRequestDTO ratingDTO, UserModel user) {
		trackRatingRepository.findByUserAndAlbumIdAndTrackId(user, ratingDTO.albumId(), ratingDTO.trackId())
				.ifPresentOrElse(existingRating -> {
					existingRating.setRating(ratingDTO.rating());
					trackRatingRepository.save(existingRating);
				}, () -> {
					TrackRatingModel newRating = new TrackRatingModel(ratingDTO.albumId(), ratingDTO.trackId(),
							ratingDTO.rating(), user);
					trackRatingRepository.save(newRating);
				});
	}
}
