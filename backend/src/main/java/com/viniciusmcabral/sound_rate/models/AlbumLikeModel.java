package com.viniciusmcabral.sound_rate.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "AlbumLike")
@Table(name = "album_likes", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "album_id" }) })
public class AlbumLikeModel extends BaseEntityModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel user;

	@Column(name = "album_id", nullable = false)
	private String albumId;

	protected AlbumLikeModel() {
	}

	public AlbumLikeModel(UserModel user, String albumId) {
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
	@Override
	public String toString() {
		return "AlbumLikeModel{id=" + getId() + ", albumId='" + albumId + "'}";
	}
}
