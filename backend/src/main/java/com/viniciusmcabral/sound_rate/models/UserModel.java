package com.viniciusmcabral.sound_rate.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name = "User")
@Table(name = "users")
public class UserModel extends AuditableEntityModel implements UserDetails, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(name = "avatar_url")
	private String avatarUrl;

	@Column(nullable = false)
	private boolean active = true;

	protected UserModel() {
	}

	public UserModel(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.active;
	}
	@Override
	public String toString() {
		return "UserModel{id=" + getId() + ", username='" + username + "', active=" + active + ", createdAt=" + getCreatedAt()
				+ "}";
	}
}
