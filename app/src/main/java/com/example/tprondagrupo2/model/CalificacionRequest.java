package com.example.tprondagrupo2.model;

/**
 * Body para calificar a la contraparte de una operación.
 * Mapea al RatingCreateRequest del backend.
 */
public class CalificacionRequest {

    private int stars;
    private String comment;

    public CalificacionRequest(int stars, String comment) {
        this.stars = stars;
        this.comment = comment;
    }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
