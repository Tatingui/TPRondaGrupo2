package com.example.tprondagrupo2.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Vendedor;

import java.util.List;

@Entity(tableName = "publicaciones")
public class PublicacionEntity {

    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private List<String> imageUrls;
    private String description;
    private String categoryName;
    private String status;
    private double price;
    private String createdAt;
    private Vendedor vendedor;
    private boolean isFavorite;
    private long lastSeenTimestamp;

    public PublicacionEntity() {
    }

    public static PublicacionEntity fromModel(Publicacion p) {
        PublicacionEntity entity = new PublicacionEntity();
        entity.setId(p.getId());
        entity.setTitle(p.getTitle());
        entity.setImageUrls(p.getImageUrls());
        entity.setDescription(p.getDescription());
        entity.setCategoryName(p.getCategoryName());
        entity.setStatus(p.getStatus());
        entity.setPrice(p.getPrice());
        entity.setCreatedAt(p.getCreatedAt());
        entity.setVendedor(p.getVendedor());
        entity.setFavorite(p.isFavorite());
        entity.setLastSeenTimestamp(System.currentTimeMillis());
        return entity;
    }

    public Publicacion toModel() {
        Publicacion p = new Publicacion();
        p.setId(this.id);
        p.setTitle(this.title);
        p.setImageUrls(this.imageUrls);
        p.setDescription(this.description);
        p.setCategoryName(this.categoryName);
        p.setStatus(this.status);
        p.setPrice(this.price);
        p.setCreatedAt(this.createdAt);
        p.setVendedor(this.vendedor);
        p.setFavorite(this.isFavorite);
        return p;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Vendedor getVendedor() { return vendedor; }
    public void setVendedor(Vendedor vendedor) { this.vendedor = vendedor; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public long getLastSeenTimestamp() { return lastSeenTimestamp; }
    public void setLastSeenTimestamp(long lastSeenTimestamp) { this.lastSeenTimestamp = lastSeenTimestamp; }
}
