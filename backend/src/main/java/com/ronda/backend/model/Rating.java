package com.ronda.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Calificacion que un participante deja sobre el otro despues de una operacion.
 * Estrellas de 1 a 5, con comentario breve opcional.
 * Una transaccion puede tener como maximo 2 ratings: uno del comprador al vendedor
 * y otro del vendedor al comprador.
 */
@Entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"transaction_id", "from_user_id"})
})
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    /** Quien califica. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    /** Quien recibe la calificacion. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "to_user_id")
    private User toUser;

    /** Estrellas: 1 a 5. */
    @Column(nullable = false)
    private Integer stars;

    /** Comentario breve opcional. */
    @Column(length = 500)
    private String comment;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Rating() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        Rating rating = (Rating) o;
        return id != null && id.equals(rating.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
