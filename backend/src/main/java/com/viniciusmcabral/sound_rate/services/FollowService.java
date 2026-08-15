package com.viniciusmcabral.sound_rate.services;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viniciusmcabral.sound_rate.dtos.response.UserDTO;
import com.viniciusmcabral.sound_rate.models.FollowModel;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.FollowRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;

@Service
public class FollowService {

	private final FollowRepository followRepository;
	private final UserRepository userRepository;

	public FollowService(FollowRepository followRepository, UserRepository userRepository) {
		this.followRepository = followRepository;
		this.userRepository = userRepository;
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
	public Page<UserDTO> getFollowers(String username, Pageable pageable) {
		UserModel user = findUserByUsername(username);
		Page<FollowModel> followersPage = followRepository.findActiveFollowersByUser(user, pageable);
		
		return followersPage.map(follow -> convertUserToDto(follow.getFollower()));
	}

	@Transactional(readOnly = true)
	public Page<UserDTO> getFollowing(String username, Pageable pageable) {
		UserModel user = findUserByUsername(username);
		Page<FollowModel> followingPage = followRepository.findActiveFollowingByUser(user, pageable);
		
		return followingPage.map(follow -> convertUserToDto(follow.getFollowing()));
	}

	private UserModel findUserByUsername(String username) {
		return userRepository.findByUsernameAndActiveTrue(username)
				.orElseThrow(() -> new NoSuchElementException("User not found: " + username));
	}

	private UserDTO convertUserToDto(UserModel user) {
		return new UserDTO(user.getId(), user.getUsername(), user.getAvatarUrl());
	}
}
