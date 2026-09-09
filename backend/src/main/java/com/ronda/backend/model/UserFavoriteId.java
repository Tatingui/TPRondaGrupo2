package com.ronda.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserFavoriteId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "publication_id")
    private Long publicationId;

    public UserFavoriteId() {
    }

    public UserFavoriteId(Long userId, Long publicationId) {
        this.userId = userId;
        this.publicationId = publicationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFavoriteId that = (UserFavoriteId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(publicationId, that.publicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, publicationId);
    }
}
