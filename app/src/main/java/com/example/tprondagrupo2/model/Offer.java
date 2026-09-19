package com.example.tprondagrupo2.model;

import java.io.Serializable;

public class Offer implements Serializable {
    private Long id;
    private Long publicationId;
    private String publicationTitle;
    private String publicationImage;
    private Double publicationOriginalPrice;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private Double offeredPrice;
    private Double amount;
    private String message;
    private String status; // PENDING, ACCEPTED, REJECTED, COUNTER_OFFER, EXPIRED
    private String expiresAt;
    private String createdAt;

    public Offer() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPublicationId() { return publicationId; }
    public void setPublicationId(Long publicationId) { this.publicationId = publicationId; }

    public String getPublicationTitle() { return publicationTitle; }
    public void setPublicationTitle(String publicationTitle) { this.publicationTitle = publicationTitle; }

    public String getPublicationImage() { return publicationImage; }
    public void setPublicationImage(String publicationImage) { this.publicationImage = publicationImage; }

    public Double getPublicationOriginalPrice() { return publicationOriginalPrice; }
    public void setPublicationOriginalPrice(Double publicationOriginalPrice) { this.publicationOriginalPrice = publicationOriginalPrice; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public Double getOfferedPrice() { return offeredPrice != null ? offeredPrice : amount; }
    public void setOfferedPrice(Double offeredPrice) { this.offeredPrice = offeredPrice; }

    public Double getAmount() { return amount != null ? amount : offeredPrice; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
