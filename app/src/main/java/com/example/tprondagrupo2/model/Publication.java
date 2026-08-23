package com.example.tprondagrupo2.model;

import java.io.Serializable;

public class Publication implements Serializable {
    private String id;
    private String title;
    private String description;
    private double price;
    private String condition; // Nuevo, Como nuevo, Usado
    private String location;
    private String category;
    private String imageUrl;
    private long timestamp;

    public Publication(String id, String title, String description, double price, String condition, String location, String category, String imageUrl, long timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.condition = condition;
        this.location = location;
        this.category = category;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCondition() { return condition; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }
}
