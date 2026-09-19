package com.ronda.backend.dto;

import com.ronda.backend.model.OfferStatus;
import java.time.LocalDateTime;

public class OfferDTO {
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
    private String message;
    private OfferStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public OfferDTO() {
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

    public Double getOfferedPrice() { return offeredPrice; }
    public void setOfferedPrice(Double offeredPrice) { this.offeredPrice = offeredPrice; }

    public Double getAmount() { return offeredPrice; }
    public void setAmount(Double amount) { this.offeredPrice = amount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
