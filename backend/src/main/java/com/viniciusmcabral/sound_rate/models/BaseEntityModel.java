package com.viniciusmcabral.sound_rate.models;

import java.util.Objects;

import org.hibernate.Hibernate;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntityModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
			return false;
		}
		BaseEntityModel that = (BaseEntityModel) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(Hibernate.getClass(this), id);
	}
}
