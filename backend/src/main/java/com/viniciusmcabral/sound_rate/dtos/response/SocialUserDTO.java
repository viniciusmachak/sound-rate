package com.viniciusmcabral.sound_rate.dtos.response;

public record SocialUserDTO(Long id, String username, String avatarUrl, boolean isFollowedByCurrentUser) {
}
