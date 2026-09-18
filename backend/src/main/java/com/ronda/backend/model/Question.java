package com.ronda.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Pregunta publica que un interesado le hace al vendedor sobre una publicacion.
 * El vendedor la responde una sola vez.
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "publication_id")
    private Publication publication;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asker_id")
    private User asker;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(length = 500)
    private String answer;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime answeredAt;

    public Question() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public User getAsker() {
        return asker;
    }

    public void setAsker(User asker) {
        this.asker = asker;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}
