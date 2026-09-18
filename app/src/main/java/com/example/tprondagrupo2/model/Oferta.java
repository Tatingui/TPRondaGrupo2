package com.example.tprondagrupo2.model;

import java.io.Serializable;

/**
 * Oferta que un interesado hace por una publicación.
 * status: PENDING, ACCEPTED, REJECTED o EXPIRED.
 */
public class Oferta implements Serializable {

    private Long id;
    private Long publicationId;
    private double amount;
    private String message;
    private String status;
    private String expiresAt;

    public Oferta() {
        // Constructor vacio requerido por Gson
    }

    public Long getId() {
        return id;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public double getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getStatusTexto() {
        if (status == null) return "";
        switch (status) {
            case "PENDING":
                return "Pendiente";
            case "ACCEPTED":
                return "Aceptada";
            case "REJECTED":
                return "Rechazada";
            case "EXPIRED":
                return "Vencida";
            default:
                return status;
        }
    }

    public boolean estaPendiente() {
        return "PENDING".equals(status);
    }
}
