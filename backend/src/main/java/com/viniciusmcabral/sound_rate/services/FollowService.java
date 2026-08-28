package com.viniciusmcabral.sound_rate.services;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viniciusmcabral.sound_rate.dtos.response.SocialUserDTO;
import com.viniciusmcabral.sound_rate.models.FollowModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.FollowRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;

@Service
public class FollowService {

	private final FollowRepository followRepository;
	private final UserRepository userRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public FollowService(FollowRepository followRepository, UserRepository userRepository,
			AuthenticatedUserService authenticatedUserService) {
		this.followRepository = followRepository;
		this.userRepository = userRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional
	public void followUser(String usernameToFollow, UserModel currentUser) {
		UserModel userToFollow = userRepository.findByUsernameAndActiveTrue(usernameToFollow)
				.orElseThrow(() -> new NoSuchElementException("User to follow not found: " + usernameToFollow));

		if (currentUser.getId().equals(userToFollow.getId()))
			throw new IllegalArgumentException("You cannot follow yourself.");

		if (followRepository.findByFollowerAndFollowing(currentUser, userToFollow).isEmpty()) {
			FollowModel newFollow = new FollowModel(currentUser, userToFollow);
			followRepository.save(newFollow);
		}
	}

	@Transactional
	public void unfollowUser(String usernameToUnfollow, UserModel currentUser) {
		UserModel userToUnfollow = userRepository.findByUsernameAndActiveTrue(usernameToUnfollow)
				.orElseThrow(() -> new NoSuchElementException("User to unfollow not found: " + usernameToUnfollow));
		followRepository.deleteByFollowerAndFollowing(currentUser, userToUnfollow);
	}

	@Transactional(readOnly = true)
	public Page<SocialUserDTO> getFollowers(String username, String query, Pageable pageable) {
		UserModel user = findUserByUsername(username);
		Page<FollowModel> followersPage = followRepository.findActiveFollowersByUser(user,
				normalizeQuery(query), pageable);

		return toSocialUserPage(followersPage, FollowModel::getFollower);
	}

	@Transactional(readOnly = true)
	public Page<SocialUserDTO> getFollowing(String username, String query, Pageable pageable) {
		UserModel user = findUserByUsername(username);
		Page<FollowModel> followingPage = followRepository.findActiveFollowingByUser(user,
				normalizeQuery(query), pageable);

		return toSocialUserPage(followingPage, FollowModel::getFollowing);
	}

	private UserModel findUserByUsername(String username) {
		return userRepository.findByUsernameAndActiveTrue(username)
				.orElseThrow(() -> new NoSuchElementException("User not found: " + username));
	}

	private Page<SocialUserDTO> toSocialUserPage(Page<FollowModel> follows,
			Function<FollowModel, UserModel> userExtractor) {
		List<UserModel> users = follows.getContent().stream().map(userExtractor).toList();
		UserModel currentUser = authenticatedUserService.getCurrentUserOrNull();
		Set<Long> followedUserIds = currentUser == null || users.isEmpty()
				? Set.of()
				: new HashSet<>(followRepository.findFollowingIds(currentUser, users));

		return follows.map(follow -> {
			UserModel user = userExtractor.apply(follow);
			return new SocialUserDTO(user.getId(), user.getUsername(), user.getAvatarUrl(),
					followedUserIds.contains(user.getId()));
		});
	}

	private String normalizeQuery(String query) {
		if (query == null || query.isBlank()) return null;
		return query.trim();
	}
}
