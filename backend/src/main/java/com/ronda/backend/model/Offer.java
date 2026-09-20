package com.ronda.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "publication_id")
    private Publication publication;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(nullable = false)
    private Double offeredPrice;

    @Column(nullable = false)
    private Double amount;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Offer() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.expiresAt == null) {
            this.expiresAt = LocalDateTime.now().plusHours(48);
        }
        if (this.amount == null && this.offeredPrice != null) {
            this.amount = this.offeredPrice;
        } else if (this.offeredPrice == null && this.amount != null) {
            this.offeredPrice = this.amount;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Double getOfferedPrice() {
        return offeredPrice != null ? offeredPrice : amount;
    }

    public void setOfferedPrice(Double offeredPrice) {
        this.offeredPrice = offeredPrice;
        this.amount = offeredPrice;
    }

    public Double getAmount() {
        return amount != null ? amount : offeredPrice;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
        this.offeredPrice = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public OfferStatus getEffectiveStatus() {
        if (status == OfferStatus.PENDING && expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            return OfferStatus.EXPIRED;
        }
        return status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
