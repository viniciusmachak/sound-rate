package com.viniciusmcabral.sound_rate.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "ListenLater")
@Table(name = "listen_later_entries", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "album_id" }) })
public class ListenLaterModel extends AuditableEntityModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel user;

	@Column(name = "album_id", nullable = false)
	private String albumId;

	protected ListenLaterModel() {
	}

	public ListenLaterModel(UserModel user, String albumId) {
		this.user = user;
		this.albumId = albumId;
	}

	public UserModel getUser() {
		return user;
	}

	public void setUser(UserModel user) {
		this.user = user;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public LocalDateTime getAddedAt() {
		return getCreatedAt();
	}

	@Override
	public String toString() {
		return "ListenLaterModel{id=" + getId() + ", albumId='" + albumId + "', addedAt=" + getAddedAt() + "}";
	}
}
