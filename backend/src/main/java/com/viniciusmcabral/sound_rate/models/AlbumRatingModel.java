package com.viniciusmcabral.sound_rate.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "AlbumRating")
@Table(name = "album_ratings", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "album_id" }) })
public class AlbumRatingModel extends AuditableEntityModel {

	@Column(name = "album_id", nullable = false)
	private String albumId;

	@Column
	private Double rating;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel user;

	protected AlbumRatingModel() {
	}

	public AlbumRatingModel(String albumId, Double rating, UserModel user) {
		this.albumId = albumId;
		this.rating = rating;
		this.user = user;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public UserModel getUser() {
		return user;
	}

	public void setUser(UserModel user) {
		this.user = user;
	}
	@Override
	public String toString() {
		return "AlbumRatingModel{id=" + getId() + ", albumId='" + albumId + "', rating=" + rating + ", createdAt="
				+ getCreatedAt() + ", updatedAt=" + getUpdatedAt() + "}";
	}
}
