package com.viniciusmcabral.sound_rate.dtos.response;

import java.time.LocalDateTime;

public record AlbumReviewDTO(Long id, String text, Double rating, LocalDateTime createdAt, LocalDateTime updatedAt,
		UserDTO author, long likesCount, boolean isLikedByCurrentUser) {
}
