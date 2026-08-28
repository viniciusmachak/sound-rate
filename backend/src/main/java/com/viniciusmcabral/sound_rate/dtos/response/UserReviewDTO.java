package com.viniciusmcabral.sound_rate.dtos.response;

import java.time.LocalDateTime;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;

public record UserReviewDTO(Long id, DeezerAlbumDTO album, String text, Double rating, LocalDateTime reviewDate) {
}
