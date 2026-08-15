package com.viniciusmcabral.sound_rate.support;

import java.time.LocalDateTime;

import org.springframework.test.util.ReflectionTestUtils;

import com.viniciusmcabral.sound_rate.models.AlbumReviewModel;
import com.viniciusmcabral.sound_rate.models.PasswordResetTokenModel;
import com.viniciusmcabral.sound_rate.models.UserModel;

public final class TestDataFactory {

	private TestDataFactory() {
	}

	public static UserModel user(Long id, String username) {
		UserModel user = new UserModel(username, username + "@example.com", "encoded-password");
		user.setAvatarUrl("https://avatar.example/" + username);
		setId(user, id);
		return user;
	}

	public static AlbumReviewModel review(Long id, String albumId, UserModel author, Double rating) {
		AlbumReviewModel review = new AlbumReviewModel(albumId, "Detailed review text", author, rating);
		setId(review, id);
		ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.now().minusDays(1));
		ReflectionTestUtils.setField(review, "updatedAt", LocalDateTime.now());
		return review;
	}

	public static PasswordResetTokenModel resetToken(Long id, String token, UserModel user, LocalDateTime expiryDate) {
		PasswordResetTokenModel resetToken = new PasswordResetTokenModel();
		setId(resetToken, id);
		resetToken.setToken(token);
		resetToken.setUser(user);
		resetToken.setExpiryDate(expiryDate);
		return resetToken;
	}

	public static void setId(Object entity, Long id) {
		ReflectionTestUtils.setField(entity, "id", id, Long.class);
	}
}
