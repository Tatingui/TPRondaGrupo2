package com.ronda.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Operacion concretada: se crea automaticamente cuando el vendedor acepta una oferta.
 * Registra comprador, vendedor, publicacion, monto final y fecha de entrega.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

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

    @OneToOne(optional = false)
    @JoinColumn(name = "offer_id", unique = true)
    private Offer acceptedOffer;

    /** Monto final acordado (el amount de la oferta aceptada). */
    @Column(nullable = false)
    private Double finalAmount;

    /** Fecha en que se concreto la entrega. Null hasta que se confirme. */
    private LocalDateTime deliveryDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Indica si el usuario dado puede calificar en esta transaccion:
     * debe ser parte de la operacion, la entrega debe estar confirmada,
     * y no deben haber pasado mas de 7 dias desde la entrega.
     */
    public boolean canBeRatedBy(Long userId) {
        if (deliveryDate == null) {
            return false;
        }
        boolean isParticipant = (buyer.getId().equals(userId) || seller.getId().equals(userId));
        boolean withinWindow = deliveryDate.plusDays(7).isAfter(LocalDateTime.now());
        return isParticipant && withinWindow;
    }

    // Getters y Setters

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

    public Offer getAcceptedOffer() {
        return acceptedOffer;
    }

    public void setAcceptedOffer(Offer acceptedOffer) {
        this.acceptedOffer = acceptedOffer;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
