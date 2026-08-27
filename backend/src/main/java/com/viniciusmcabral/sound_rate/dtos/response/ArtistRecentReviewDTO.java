package com.viniciusmcabral.sound_rate.dtos.response;

import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;

public record ArtistRecentReviewDTO(DeezerAlbumDTO album, AlbumReviewDTO review) {
}
