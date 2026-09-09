package com.example.tprondagrupo2.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Publicacion implements Serializable {

    private String id;

    private String title;

    private List<String> imageUrls;

    private String description;

    private String categoryName;

    private String status;

    private double price;

    private String createdAt;

    private Vendedor vendedor;

    // TODO: decidir si deberíamos seguir usando sellerId y sellerName
    //       o si es redundante porque vendedor ya los incluye.
    private Long sellerId;

    private String sellerName;

    private String location;

    private boolean isFavorite;

    public Publicacion() {
        // Constructor vacio requerido por Gson
        // this.imageUrls = new ArrayList<>();
    }

    public Publicacion(String id, String title, List<String> imageUrls, String description,
                       String categoryName, String status, double price, String createdAt) {
        this(id, title, imageUrls, description, categoryName, status, price, createdAt, null);
    }

    public Publicacion(String id, String title, List<String> imageUrls, String description,
                       String categoryName, String status, double price, String createdAt,
                       Vendedor vendedor) {
        this.id = id;
        this.title = title;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.description = description;
        this.categoryName = categoryName;
        this.status = status;
        this.price = price;
        this.createdAt = createdAt;
        this.vendedor = vendedor;
        if (vendedor != null) {
            this.sellerName = vendedor.getNombre();
            this.location = vendedor.getUbicacion();
            if (vendedor.getId() != null) {
                try {
                    this.sellerId = Long.parseLong(vendedor.getId());
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getIdLong() {
        if (id == null) return null;
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setId(Long id) {
        this.id = id != null ? id.toString() : null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getImageUrls() {
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public int getCantidadFotos() {
        return getImageUrls().size();
    }

    public String getFirstImageUrl() {
        List<String> urls = getImageUrls();
        if (!urls.isEmpty()) {
            return urls.get(0);
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Vendedor getVendedor() {
        if (vendedor != null) {
            return vendedor;
        }
        if (sellerId != null || sellerName != null || location != null) {
            return new Vendedor(
                    sellerId != null ? sellerId.toString() : "0",
                    sellerName,
                    4.5,
                    15,
                    10,
                    "2 años",
                    location
            );
        }
        return null;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
        if (vendedor != null) {
            this.sellerName = vendedor.getNombre();
            this.location = vendedor.getUbicacion();
            if (vendedor.getId() != null) {
                try {
                    this.sellerId = Long.parseLong(vendedor.getId());
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    public Long getSellerId() {
        if (sellerId != null) return sellerId;
        if (vendedor != null && vendedor.getId() != null) {
            try {
                return Long.parseLong(vendedor.getId());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        if (sellerName != null) return sellerName;
        if (vendedor != null) return vendedor.getNombre();
        return null;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getLocation() {
        if (location != null) return location;
        if (vendedor != null) return vendedor.getUbicacion();
        return null;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
