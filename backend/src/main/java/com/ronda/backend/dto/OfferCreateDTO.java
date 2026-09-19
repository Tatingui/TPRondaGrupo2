package com.ronda.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OfferCreateDTO {

    @NotNull(message = "El ID de la publicación es obligatorio")
    private Long publicationId;

    @NotNull(message = "El precio ofertado es obligatorio")
    @Min(value = 1, message = "El precio debe ser mayor a 0")
    private Double offeredPrice;

    private String message;

    public OfferCreateDTO() {
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    public Double getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(Double offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
