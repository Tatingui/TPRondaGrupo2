package com.ronda.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorites")
public class UserFavorite {

    @EmbeddedId
    private UserFavoriteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("publicationId")
    @JoinColumn(name = "publication_id")
    private Publication publication;

    @Column(name = "saved_price", nullable = false)
    private Double savedPrice;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public UserFavorite() {
    }

    public UserFavorite(User user, Publication publication, Double savedPrice) {
        this.id = new UserFavoriteId(user.getId(), publication.getId());
        this.user = user;
        this.publication = publication;
        this.savedPrice = savedPrice;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public UserFavoriteId getId() {
        return id;
    }

    public void setId(UserFavoriteId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Double getSavedPrice() {
        return savedPrice;
    }

    public void setSavedPrice(Double savedPrice) {
        this.savedPrice = savedPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
