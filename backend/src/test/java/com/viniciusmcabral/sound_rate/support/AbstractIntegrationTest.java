package com.viniciusmcabral.sound_rate.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerAlbumDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerArtistDTO;
import com.viniciusmcabral.sound_rate.dtos.deezer.DeezerTracklistDTO;
import com.viniciusmcabral.sound_rate.models.UserModel;
import com.viniciusmcabral.sound_rate.repositories.AlbumLikeRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.AlbumReviewRepository;
import com.viniciusmcabral.sound_rate.repositories.ArtistFollowRepository;
import com.viniciusmcabral.sound_rate.repositories.FollowRepository;
import com.viniciusmcabral.sound_rate.repositories.ListenLaterRepository;
import com.viniciusmcabral.sound_rate.repositories.PasswordResetTokenRepository;
import com.viniciusmcabral.sound_rate.repositories.ReviewLikeRepository;
import com.viniciusmcabral.sound_rate.repositories.TrackRatingRepository;
import com.viniciusmcabral.sound_rate.repositories.UserRepository;
import com.viniciusmcabral.sound_rate.services.DeezerService;
import com.viniciusmcabral.sound_rate.services.EmailService;
import com.viniciusmcabral.sound_rate.services.StorageService;
import com.viniciusmcabral.sound_rate.services.TokenService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected AlbumLikeRepository albumLikeRepository;

	@Autowired
	protected AlbumRatingRepository albumRatingRepository;

	@Autowired
	protected AlbumReviewRepository albumReviewRepository;

	@Autowired
	protected ArtistFollowRepository artistFollowRepository;

	@Autowired
	protected FollowRepository followRepository;

	@Autowired
	protected ListenLaterRepository listenLaterRepository;

	@Autowired
	protected PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	protected ReviewLikeRepository reviewLikeRepository;

	@Autowired
	protected TrackRatingRepository trackRatingRepository;

	@Autowired
	protected org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

	@Autowired
	protected TokenService tokenService;

	@MockBean
	protected EmailService emailService;

	@MockBean
	protected DeezerService deezerService;

	@MockBean
	protected StorageService storageService;

	@BeforeEach
	void resetDatabaseAndMocks() {
		reviewLikeRepository.deleteAll();
		artistFollowRepository.deleteAll();
		albumLikeRepository.deleteAll();
		followRepository.deleteAll();
		passwordResetTokenRepository.deleteAll();
		albumReviewRepository.deleteAll();
		trackRatingRepository.deleteAll();
		albumRatingRepository.deleteAll();
		listenLaterRepository.deleteAll();
		userRepository.deleteAll();

		lenient().when(deezerService.getAlbumDetails(anyString())).thenAnswer(invocation -> albumDto(invocation.getArgument(0)));
		lenient().when(storageService.uploadFile(any())).thenReturn("https://cdn.example/avatar.png");
	}

	protected UserModel createUser(String username, String rawPassword) {
		return createUser(username, rawPassword, true);
	}

	protected UserModel createUser(String username, String rawPassword, boolean active) {
		UserModel user = new UserModel(username, username + "@example.com", passwordEncoder.encode(rawPassword));
		user.setAvatarUrl("https://avatar.example/" + username);
		user.setActive(active);
		return userRepository.save(user);
	}

	protected String bearerToken(UserModel user) {
		return "Bearer " + tokenService.generateToken(user);
	}

	protected HttpHeaders authHeaders(UserModel user) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(tokenService.generateToken(user));
		return headers;
	}

	protected String json(Object body) {
		try {
			return objectMapper.writeValueAsString(body);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to write JSON for test body", exception);
		}
	}

	protected DeezerAlbumDTO albumDto(String albumId) {
		long id = Long.parseLong(albumId.replaceAll("\\D", ""));
		return new DeezerAlbumDTO(id, "Album " + albumId, "https://deezer.example/albums/" + albumId,
				"https://img.example/" + albumId + ".jpg", "https://img.example/" + albumId + "@2x.jpg",
				new DeezerArtistDTO(1L, "Artist", "https://deezer.example/artists/1", null, null, null), "2024-01-01", 3600,
				500, 4.5, false, "Test Label", "2024 Test Label", null, java.util.List.of(),
				new DeezerTracklistDTO(java.util.List.of()));
	}

	protected Map<String, Object> mapOf(Object... values) {
		return java.util.stream.IntStream.range(0, values.length / 2).boxed()
				.collect(java.util.stream.Collectors.toMap(index -> values[index * 2].toString(),
						index -> values[index * 2 + 1]));
	}
}
