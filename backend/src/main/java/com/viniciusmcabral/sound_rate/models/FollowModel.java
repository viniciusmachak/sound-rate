package com.viniciusmcabral.sound_rate.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "Follow")
@Table(name = "follows", uniqueConstraints = { @UniqueConstraint(columnNames = { "follower_id", "following_id" }) })
public class FollowModel extends AuditableEntityModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "follower_id", nullable = false)
	private UserModel follower;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "following_id", nullable = false)
	private UserModel following;

	protected FollowModel() {
	}

	public FollowModel(UserModel follower, UserModel following) {
		this.follower = follower;
		this.following = following;
	}

	public UserModel getFollower() {
		return follower;
	}

	public void setFollower(UserModel follower) {
		this.follower = follower;
	}

	public UserModel getFollowing() {
		return following;
	}

	public void setFollowing(UserModel following) {
		this.following = following;
	}

	@Override
	public String toString() {
		return "FollowModel{id=" + getId() + ", createdAt=" + getCreatedAt() + "}";
	}
}
