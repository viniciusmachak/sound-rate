package com.viniciusmcabral.sound_rate.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "TrackRating")
@Table(name = "track_ratings", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "track_id" }) })
public class TrackRatingModel extends AuditableEntityModel {

	@Column(name = "album_id", nullable = false)
	private String albumId;

	@Column(name = "track_id", nullable = false)
	private String trackId;

	@Column
	private Double rating;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel user;

	protected TrackRatingModel() {
	}

	public TrackRatingModel(String albumId, String trackId, Double rating, UserModel user) {
		this.albumId = albumId;
		this.trackId = trackId;
		this.rating = rating;
		this.user = user;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getTrackId() {
		return trackId;
	}

	public void setTrackId(String trackId) {
		this.trackId = trackId;
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
		return "TrackRatingModel{id=" + getId() + ", albumId='" + albumId + "', trackId='" + trackId + "', rating="
				+ rating + "}";
	}
}
