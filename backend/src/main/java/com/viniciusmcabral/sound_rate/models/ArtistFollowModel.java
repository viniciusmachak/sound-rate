package com.viniciusmcabral.sound_rate.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "ArtistFollow")
@Table(name = "artist_follows", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "user_id", "artist_id" }) })
public class ArtistFollowModel extends AuditableEntityModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel user;

	@Column(name = "artist_id", nullable = false)
	private String artistId;

	protected ArtistFollowModel() {
	}

	public ArtistFollowModel(UserModel user, String artistId) {
		this.user = user;
		this.artistId = artistId;
	}

	public UserModel getUser() {
		return user;
	}

	public String getArtistId() {
		return artistId;
	}
}
