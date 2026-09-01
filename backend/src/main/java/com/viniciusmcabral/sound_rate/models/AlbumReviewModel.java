package com.viniciusmcabral.sound_rate.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "AlbumReview")
@Table(name = "album_reviews")
public class AlbumReviewModel extends AuditableEntityModel {

	@Column(name = "album_id", nullable = false)
	private String albumId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String text;

	@Column(nullable = false)
	private Double rating;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel user;

	protected AlbumReviewModel() {
	}

	public AlbumReviewModel(String albumId, String text, UserModel user, Double rating) {
		this.albumId = albumId;
		this.text = text;
		this.user = user;
		this.rating = rating;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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
		return "AlbumReviewModel{id=" + getId() + ", albumId='" + albumId + "', rating=" + rating + ", createdAt="
				+ getCreatedAt() + ", updatedAt=" + getUpdatedAt() + "}";
	}
}
