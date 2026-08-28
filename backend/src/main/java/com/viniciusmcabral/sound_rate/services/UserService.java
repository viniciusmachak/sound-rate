package com.viniciusmcabral.sound_rate.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdatePasswordDTO;
import com.viniciusmcabral.sound_rate.dtos.request.UpdateProfileDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserAlbumHighlightDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserProfileDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserRatingDTO;
import com.viniciusmcabral.sound_rate.dtos.response.UserReviewDTO;
import com.viniciusmcabral.sound_rate.models.AlbumLikeModel;
import com.viniciusmcabral.sound_rate.models.AlbumRatingModel;
import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumLikeRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.ArtistFollowRepository;
import com.viniciusmcabral.sound_rate.repositories.FollowRepository;
import com.viniciusmcabral.sound_rate.repositories.ListenLaterRepository;
import com.viniciusmcabral.sound_rate.repositories.TrackRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AlbumReviewRepository albumReviewRepository;
	private final AlbumRatingRepository albumRatingRepository;
	private final TrackRatingRepository trackRatingRepository;
	private final DeezerService deezerService;
	private final StorageService storageService;
	private final AlbumLikeRepository albumLikeRepository;
	private final FollowRepository followRepository;
	private final ArtistFollowRepository artistFollowRepository;
	private final ListenLaterRepository listenLaterRepository;
	private final EmailService emailService;
	private final AuthenticatedUserService authenticatedUserService;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			AlbumReviewRepository albumReviewRepository, AlbumRatingRepository albumRatingRepository,
			TrackRatingRepository trackRatingRepository, DeezerService deezerService, StorageService storageService,
			AlbumLikeRepository albumLikeRepository, FollowRepository followRepository,
			ArtistFollowRepository artistFollowRepository, ListenLaterRepository listenLaterRepository,
			EmailService emailService,
			AuthenticatedUserService authenticatedUserService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.albumReviewRepository = albumReviewRepository;
		this.albumRatingRepository = albumRatingRepository;
		this.trackRatingRepository = trackRatingRepository;
		this.deezerService = deezerService;
		this.storageService = storageService;
		this.albumLikeRepository = albumLikeRepository;
		this.followRepository = followRepository;
		this.artistFollowRepository = artistFollowRepository;
		this.listenLaterRepository = listenLaterRepository;
		this.emailService = emailService;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional(readOnly = true)
	public UserProfileDTO getUserProfile(String username) {
		UserModel user = userRepository.findByUsernameAndActiveTrue(username)
				.orElseThrow(() -> new NoSuchElementException("User not found with username: " + username));
		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();

		long followersCount = followRepository.countActiveFollowersByUser(user);
		long followedArtistsCount = artistFollowRepository.countByUser(user);
		long followingCount = followRepository.countActiveFollowingByUser(user) + followedArtistsCount;

		boolean isFollowed = (currentUser != null)
				&& followRepository.findByFollowerAndFollowing(currentUser, user).isPresent();
		long totalReviews = albumReviewRepository.countByUser(user);
		long totalAlbumRatings = albumRatingRepository.countByUser(user);
		long totalTrackRatings = trackRatingRepository.countByUser(user);
		long totalLikes = albumLikeRepository.countByUser(user);
		boolean isOwnProfile = currentUser != null && currentUser.getId().equals(user.getId());
		long totalListenLater = isOwnProfile ? listenLaterRepository.countByUser(user) : 0;
		long totalActivity = totalAlbumRatings + totalReviews + totalLikes + followedArtistsCount;
		Double averageRating = calculateAverageRating(user, totalAlbumRatings, totalTrackRatings);
		UserAlbumHighlightDTO featuredAlbum = albumRatingRepository
				.findFirstByUserAndRatingIsNotNullOrderByRatingDescUpdatedAtDesc(user)
				.map(rating -> {
					DeezerAlbumDTO album = deezerService.getAlbumDetails(rating.getAlbumId());
					return album == null ? null : new UserAlbumHighlightDTO(album, rating.getRating());
				})
				.orElse(null);

		return new UserProfileDTO(new UserDTO(user.getId(), user.getUsername(), user.getAvatarUrl()), user.getBio(),
				user.getCreatedAt(), totalReviews, totalAlbumRatings, totalTrackRatings, totalLikes, totalListenLater,
				totalActivity, followersCount, followingCount, isFollowed, averageRating, featuredAlbum);
	}

	private Double calculateAverageRating(UserModel user, long totalAlbumRatings, long totalTrackRatings) {
		long totalRatings = totalAlbumRatings + totalTrackRatings;
		if (totalRatings == 0) {
			return null;
		}

		double albumTotal = albumRatingRepository.findAverageRatingByUser(user).orElse(0.0) * totalAlbumRatings;
		double trackTotal = trackRatingRepository.findAverageRatingByUser(user).orElse(0.0) * totalTrackRatings;
		return (albumTotal + trackTotal) / totalRatings;
	}

	@Transactional(readOnly = true)
	public Page<UserRatingDTO> getRatedAlbumsPage(String username, Pageable pageable) {
		UserModel user = userRepository.findByUsernameAndActiveTrue(username)
				.orElseThrow(() -> new NoSuchElementException("User not found: " + username));
		Page<AlbumRatingModel> ratingsPage = albumRatingRepository.findByUser(user, pageable);
		List<String> albumIds = ratingsPage.getContent().stream().map(AlbumRatingModel::getAlbumId).toList();
		Map<String, String> reviewTextByAlbumId = albumIds.isEmpty() ? new HashMap<>()
				: albumReviewRepository.findReviewTextsByUserAndAlbumIds(user, albumIds).stream().collect(Collectors
						.toMap(AlbumReviewRepository.AlbumReviewTextProjection::getAlbumId,
								AlbumReviewRepository.AlbumReviewTextProjection::getText));
		List<UserRatingDTO> dtoList = ratingsPage.getContent().stream().map(rating -> {
			DeezerAlbumDTO albumDetails = deezerService.getAlbumDetails(String.valueOf(rating.getAlbumId()));

			if (albumDetails == null)
				return null;

			String reviewText = reviewTextByAlbumId.get(rating.getAlbumId());

			return new UserRatingDTO(albumDetails, rating.getRating(), rating.getCreatedAt(), reviewText);
		}).filter(Objects::nonNull).collect(Collectors.toList());

		return new PageImpl<>(dtoList, pageable, ratingsPage.getTotalElements());
	}

	@Transactional(readOnly = true)
	public Page<DeezerAlbumDTO> getLikedAlbumsPage(String username, Pageable pageable) {
		UserModel user = userRepository.findByUsernameAndActiveTrue(username)
				.orElseThrow(() -> new NoSuchElementException("User not found: " + username));
		Page<AlbumLikeModel> likedAlbumsPage = albumLikeRepository.findByUser(user, pageable);

		return likedAlbumsPage.map(like -> deezerService.getAlbumDetails(like.getAlbumId()));
	}

	@Transactional(readOnly = true)
	public Page<UserReviewDTO> getReviewsPage(String username, Pageable pageable) {
		UserModel user = userRepository.findByUsernameAndActiveTrue(username)
				.orElseThrow(() -> new NoSuchElementException("User not found: " + username));
		Page<AlbumReviewModel> reviewsPage = albumReviewRepository.findByUser(user, pageable);
		List<UserReviewDTO> reviews = reviewsPage.getContent().stream().map(review -> {
			DeezerAlbumDTO album = deezerService.getAlbumDetails(review.getAlbumId());
			return album == null ? null : new UserReviewDTO(review.getId(), album, review.getText(), review.getRating(),
					review.getCreatedAt());
		}).filter(Objects::nonNull).toList();

		return new PageImpl<>(reviews, pageable, reviewsPage.getTotalElements());
	}

	@Transactional(readOnly = true)
	public Page<UserDTO> searchUsers(String query, Pageable pageable) {
		Page<UserModel> userPage = userRepository.searchByUsername(query, pageable);
		return userPage.map(user -> new UserDTO(user.getId(), user.getUsername(), user.getAvatarUrl()));
	}

	@Transactional
	public void deleteCurrentUser() {
		UserModel currentUser = authenticatedUserService.requireCurrentUser();
		UserModel userToDelete = userRepository.findById(currentUser.getId())
				.orElseThrow(() -> new NoSuchElementException("User not found for deletion."));

		emailService.sendAccountDeletionEmail(userToDelete.getEmail(), userToDelete.getUsername());

		userToDelete.setActive(false);
		userRepository.save(userToDelete);
	}

	@Transactional
	public UserDTO updateProfile(UserModel currentUser, UpdateProfileDTO data) {
		userRepository.findByEmail(data.email()).ifPresent(user -> {
			if (!user.getId().equals(currentUser.getId()))
				throw new IllegalStateException("Email already in use by another account.");
		});

		currentUser.setEmail(data.email());
		currentUser.setBio(normalizeBio(data.bio()));
		UserModel updatedUser = userRepository.save(currentUser);

		return toPrivateUserDTO(updatedUser);
	}

	@Transactional
	public void updatePassword(UserModel currentUser, UpdatePasswordDTO data) {
		if (!passwordEncoder.matches(data.currentPassword(), currentUser.getPassword()))
			throw new IllegalArgumentException("Incorrect current password.");

		String newHashedPassword = passwordEncoder.encode(data.newPassword());
		currentUser.setPassword(newHashedPassword);

		userRepository.save(currentUser);
	}

	@Transactional
	public UserDTO updateAvatar(UserModel currentUser, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Avatar file cannot be empty.");
		}

		String newAvatarUrl = storageService.uploadFile(file);
		currentUser.setAvatarUrl(newAvatarUrl);

		userRepository.save(currentUser);

		return toPrivateUserDTO(currentUser);
	}

	@Transactional
	public UserDTO resetAvatar(UserModel currentUser) {
		String defaultAvatarUrl = "https://api.dicebear.com/8.x/initials/svg?seed=" + currentUser.getUsername();
		currentUser.setAvatarUrl(defaultAvatarUrl);

		UserModel updatedUser = userRepository.save(currentUser);

		return toPrivateUserDTO(updatedUser);
	}

	private UserDTO toPrivateUserDTO(UserModel user) {
		return new UserDTO(user.getId(), user.getUsername(), user.getAvatarUrl(), user.getEmail(), user.getBio());
	}

	private String normalizeBio(String bio) {
		if (bio == null || bio.isBlank()) {
			return null;
		}
		return bio.trim();
	}

}
