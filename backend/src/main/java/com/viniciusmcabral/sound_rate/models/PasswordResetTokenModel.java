package com.viniciusmcabral.sound_rate.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name = "PasswordResetToken")
@Table(name = "password_reset_tokens")
public class PasswordResetTokenModel extends BaseEntityModel {

	@Column(nullable = false, unique = true)
	private String token;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "user_id")
	private UserModel user;

	@Column(name = "expiry_date", nullable = false)
	private LocalDateTime expiryDate;

	public PasswordResetTokenModel() {
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public UserModel getUser() {
		return user;
	}

	public void setUser(UserModel user) {
		this.user = user;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}
	@Override
	public String toString() {
		return "PasswordResetTokenModel{id=" + getId() + ", expiryDate=" + expiryDate + "}";
	}
}
