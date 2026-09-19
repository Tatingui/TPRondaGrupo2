package com.ronda.backend.dto;

import com.ronda.backend.model.OfferStatus;
import jakarta.validation.constraints.NotNull;

public class OfferRespondDTO {

    @NotNull(message = "El estado de respuesta es obligatorio")
    private OfferStatus status; // ACCEPTED, REJECTED, COUNTER_OFFER

    private Double newPrice; // Usado si es COUNTER_OFFER

    public OfferRespondDTO() {
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public Double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(Double newPrice) {
        this.newPrice = newPrice;
    }
}
