package com.example.tprondagrupo2.model;

import java.io.Serializable;
import java.util.List;

public class PublicationCreateRequest implements Serializable {
    private String title;
    private String description;
    private Double price;
    private String status; // NUEVO, COMO_NUEVO, USADO
    private String location;
    private Long categoryId;
    private List<String> imageUrls;

    public PublicationCreateRequest(String title, String description, Double price, String status, String location, Long categoryId, List<String> imageUrls) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
        this.location = location;
        this.categoryId = categoryId;
        this.imageUrls = imageUrls;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
