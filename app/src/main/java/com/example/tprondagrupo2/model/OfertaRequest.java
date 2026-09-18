package com.example.tprondagrupo2.model;

/**
 * Body de POST /publications/{id}/offers.
 */
public class OfertaRequest {

    private final double amount;
    private final String message;   // opcional

    public OfertaRequest(double amount, String message) {
        this.amount = amount;
        this.message = message;
    }

    public double getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }
}
