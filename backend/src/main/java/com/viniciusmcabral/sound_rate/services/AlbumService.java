package com.viniciusmcabral.sound_rate.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AlbumDashboardDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AlbumDetailsDTO;
import com.viniciusmcabral.sound_rate.dtos.response.AlbumReviewDTO;
import com.viniciusmcabral.sound_rate.dtos.response.TrackRatingDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserDTO;
import com.viniciusmcabral.sound_rate.models.AlbumRatingModel;
import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.models.TrackRatingModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumLikeRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.ListenLaterRepository;
import com.viniciusmcabral.sound_rate.repositories.ReviewLikeRepository;
import com.viniciusmcabral.sound_rate.repositories.TrackRatingRepository;

@Service
public class AlbumService {

	private final DeezerService deezerService;
	private final AlbumRatingRepository albumRatingRepository;
	private final AlbumReviewRepository albumReviewRepository;
	private final AlbumLikeRepository albumLikeRepository;
	private final ReviewLikeRepository reviewLikeRepository;
	private final ListenLaterRepository listenLaterRepository;
	private final TrackRatingRepository trackRatingRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public AlbumService(DeezerService deezerService, AlbumRatingRepository albumRatingRepository,
			AlbumReviewRepository albumReviewRepository, AlbumLikeRepository albumLikeRepository,
			ReviewLikeRepository reviewLikeRepository, ListenLaterRepository listenLaterRepository,
			TrackRatingRepository trackRatingRepository, AuthenticatedUserService authenticatedUserService) {
		this.deezerService = deezerService;
		this.albumRatingRepository = albumRatingRepository;
		this.albumReviewRepository = albumReviewRepository;
		this.albumLikeRepository = albumLikeRepository;
		this.reviewLikeRepository = reviewLikeRepository;
		this.listenLaterRepository = listenLaterRepository;
		this.trackRatingRepository = trackRatingRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional(readOnly = true)
	public AlbumDetailsDTO getAlbumDetails(String albumId) {
		DeezerAlbumDTO deezerDetails = Optional.ofNullable(deezerService.getAlbumDetails(albumId))
				.orElseThrow(() -> new NoSuchElementException("Album not found on Deezer with ID: " + albumId));

		Double communityScore = albumRatingRepository.findCommunityAverageRating(albumId).orElse(null);
		long likesCount = albumLikeRepository.countByAlbumId(albumId);
		long ratingsCount = albumRatingRepository.countByAlbumId(albumId);

		Pageable firstPageOfReviews = PageRequest.of(0, 10, Sort.by("createdAt").descending());
		List<AlbumReviewModel> reviewModels = albumReviewRepository.findActiveReviewsByAlbumId(albumId, firstPageOfReviews)
				.getContent();
		Function<AlbumReviewModel, AlbumReviewDTO> reviewMapper = buildReviewMapper(reviewModels);
		List<AlbumReviewDTO> userReviews = reviewModels.stream().map(reviewMapper).collect(Collectors.toList());

		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();

		Double currentUserRating = null;
		AlbumReviewDTO currentUserReview = null;
		boolean isLikedByCurrentUser = false;
		boolean isOnListenLaterList = false;
		List<TrackRatingDTO> currentUserTrackRatings = Collections.emptyList();

		if (currentUser != null) {
			currentUserRating = findCurrentUserRating(albumId);
			currentUserReview = findCurrentUserReview(albumId);
			isLikedByCurrentUser = isAlbumLikedByCurrentUser(albumId);
			isOnListenLaterList = isAlbumOnListenLaterList(albumId);
			currentUserTrackRatings = trackRatingRepository.findByUserAndAlbumId(currentUser, albumId).stream()
					.map(this::convertTrackRatingToDto).collect(Collectors.toList());
		}

		return new AlbumDetailsDTO(deezerDetails, communityScore, currentUserRating, currentUserReview, userReviews,
				likesCount, isLikedByCurrentUser, isOnListenLaterList, currentUserTrackRatings, ratingsCount);
	}

	@Transactional(readOnly = true)
	public List<AlbumDashboardDTO> getHighestRatedAlbums() {
		Pageable limit = PageRequest.of(0, 15);
		List<String> topAlbumIds = albumRatingRepository.findTopRatedAlbumIds(limit).getContent();

		if (topAlbumIds.isEmpty()) {
			return Collections.emptyList();
		}

		return topAlbumIds.stream().map(albumId -> {
			DeezerAlbumDTO deezerDetails = deezerService.getAlbumDetails(albumId);
			Double communityScore = albumRatingRepository.findCommunityAverageRating(albumId).orElse(0.0);

			return new AlbumDashboardDTO(albumId, deezerDetails.title(), deezerDetails.coverMedium(),
					deezerDetails.artist().name(), communityScore);
		}).collect(Collectors.toList());
	}

	private Double findCurrentUserRating(String albumId) {
		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();
		if (currentUser == null) {
			return null;
		}

		Optional<AlbumRatingModel> directAlbumRating = albumRatingRepository.findByUserAndAlbumId(currentUser, albumId);

		if (directAlbumRating.isPresent()) {
			return directAlbumRating.get().getRating();
		}

		List<TrackRatingModel> trackRatings = trackRatingRepository.findByUserAndAlbumId(currentUser, albumId);

		if (!trackRatings.isEmpty()) {
			return trackRatings.stream().mapToDouble(TrackRatingModel::getRating).average().orElse(0.0);
		}

		return null;
	}

	private boolean isAlbumLikedByCurrentUser(String albumId) {
		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();

		if (currentUser == null)
			return false;

		return albumLikeRepository.findByUserAndAlbumId(currentUser, albumId).isPresent();
	}

	private AlbumReviewDTO findCurrentUserReview(String albumId) {
		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();

		if (currentUser == null)
			return null;
		return albumReviewRepository.findByUserAndAlbumId(currentUser, albumId)
				.map(review -> buildReviewMapper(List.of(review)).apply(review)).orElse(null);
	}

	private boolean isAlbumOnListenLaterList(String albumId) {
		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();

		if (currentUser == null)
			return false;
		return listenLaterRepository.findByUserAndAlbumId(currentUser, albumId).isPresent();
	}

	private Function<AlbumReviewModel, AlbumReviewDTO> buildReviewMapper(List<AlbumReviewModel> reviews) {
		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();
		List<Long> reviewIds = reviews.stream().map(AlbumReviewModel::getId).toList();
		Map<Long, Long> likesCountByReviewId = reviewIds.isEmpty() ? Collections.emptyMap()
				: reviewLikeRepository.countByAlbumReviewIds(reviewIds).stream().collect(Collectors.toMap(
						ReviewLikeRepository.ReviewLikeCountProjection::getReviewId,
						ReviewLikeRepository.ReviewLikeCountProjection::getLikesCount));
		Set<Long> likedReviewIds = (currentUser == null || reviewIds.isEmpty()) ? Collections.emptySet()
				: reviewLikeRepository.findLikedReviewIdsByUserAndAlbumReviewIds(currentUser, reviewIds).stream()
						.collect(Collectors.toSet());

		return review -> new AlbumReviewDTO(review.getId(), review.getText(), review.getRating(), review.getCreatedAt(),
				review.getUpdatedAt(),
				new UserDTO(review.getUser().getId(), review.getUser().getUsername(), review.getUser().getAvatarUrl()),
				likesCountByReviewId.getOrDefault(review.getId(), 0L), likedReviewIds.contains(review.getId()));
	}

	private TrackRatingDTO convertTrackRatingToDto(TrackRatingModel rating) {
		return new TrackRatingDTO(rating.getId(), rating.getRating(), rating.getTrackId());
	}
}
