package com.example.tprondagrupo2.model;

import java.io.Serializable;

public class OfferCreateRequest implements Serializable {
    private Long publicationId;
    private Double offeredPrice;
    private String message;

    public OfferCreateRequest(Long publicationId, Double offeredPrice, String message) {
        this.publicationId = publicationId;
        this.offeredPrice = offeredPrice;
        this.message = message;
    }

    public Long getPublicationId() { return publicationId; }
    public void setPublicationId(Long publicationId) { this.publicationId = publicationId; }

    public Double getOfferedPrice() { return offeredPrice; }
    public void setOfferedPrice(Double offeredPrice) { this.offeredPrice = offeredPrice; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
