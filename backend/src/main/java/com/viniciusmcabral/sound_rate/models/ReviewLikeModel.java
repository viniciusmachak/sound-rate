package com.viniciusmcabral.sound_rate.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "ReviewLike")
@Table(name = "review_likes", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "album_review_id" }) })
public class ReviewLikeModel extends BaseEntityModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "album_review_id", nullable = false)
	private AlbumReviewModel albumReview;

	protected ReviewLikeModel() {
	}

	public ReviewLikeModel(UserModel user, AlbumReviewModel albumReview) {
		this.user = user;
		this.albumReview = albumReview;
	}

	public UserModel getUser() {
		return user;
	}

	public void setUser(UserModel user) {
		this.user = user;
	}

	public AlbumReviewModel getAlbumReview() {
		return albumReview;
	}

	public void setAlbumReview(AlbumReviewModel albumReview) {
		this.albumReview = albumReview;
	}
	@Override
	public String toString() {
		return "ReviewLikeModel{id=" + getId() + "}";
	}
}
