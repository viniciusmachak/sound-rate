package com.viniciusmcabral.sound_rate.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.viniciusmcabral.sound_rate.models.FollowModel;
import com.viniciusmcabral.sound_rate.repositories.FollowRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;
import com.viniciusmcabral.sound_rate.support.TestDataFactory;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

	@Mock
	private FollowRepository followRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private FollowService followService;

	@Test
	void followUserPersistsRelationshipWhenTargetExists() {
		var currentUser = TestDataFactory.user(1L, "alice");
		var targetUser = TestDataFactory.user(2L, "bob");
		when(userRepository.findByUsernameAndActiveTrue("bob")).thenReturn(Optional.of(targetUser));
		when(followRepository.findByFollowerAndFollowing(currentUser, targetUser)).thenReturn(Optional.empty());

		followService.followUser("bob", currentUser);

		verify(followRepository).save(any(FollowModel.class));
	}

	@Test
	void followUserRejectsSelfFollow() {
		var currentUser = TestDataFactory.user(1L, "alice");
		when(userRepository.findByUsernameAndActiveTrue("alice")).thenReturn(Optional.of(currentUser));

		assertThatThrownBy(() -> followService.followUser("alice", currentUser))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("cannot follow yourself");

		verify(followRepository, never()).save(any());
	}

	@Test
	void unfollowUserDeletesRelationship() {
		var currentUser = TestDataFactory.user(1L, "alice");
		var targetUser = TestDataFactory.user(2L, "bob");
		when(userRepository.findByUsernameAndActiveTrue("bob")).thenReturn(Optional.of(targetUser));

		followService.unfollowUser("bob", currentUser);

		verify(followRepository).deleteByFollowerAndFollowing(currentUser, targetUser);
	}
}
