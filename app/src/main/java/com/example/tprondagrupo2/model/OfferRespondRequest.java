package com.example.tprondagrupo2.model;

import java.io.Serializable;

public class OfferRespondRequest implements Serializable {
    private String status; // ACCEPTED, REJECTED, COUNTER_OFFER
    private Double newPrice;

    public OfferRespondRequest(String status, Double newPrice) {
        this.status = status;
        this.newPrice = newPrice;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getNewPrice() { return newPrice; }
    public void setNewPrice(Double newPrice) { this.newPrice = newPrice; }
}
