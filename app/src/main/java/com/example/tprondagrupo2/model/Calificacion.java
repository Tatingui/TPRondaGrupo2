package com.example.tprondagrupo2.model;

import java.io.Serializable;

/**
 * Calificación recibida o emitida en una operación.
 * Mapea al RatingDTO del backend.
 */
public class Calificacion implements Serializable {

    private Long id;
    private int stars;
    private String comment;
    private String fromUserName;
    private Long fromUserId;
    private String createdAt;

    public Calificacion() {
        // Constructor vacío requerido por Gson
    }

    public Long getId() { return id; }
    public int getStars() { return stars; }
    public String getComment() { return comment; }
    public String getFromUserName() { return fromUserName; }
    public Long getFromUserId() { return fromUserId; }
    public String getCreatedAt() { return createdAt; }
}
