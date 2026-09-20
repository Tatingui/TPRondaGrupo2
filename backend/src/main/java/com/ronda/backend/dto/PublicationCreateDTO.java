package com.ronda.backend.dto;

import com.ronda.backend.model.PublicationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PublicationCreateDTO {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
    private Double price;

    @NotNull(message = "El estado del artículo es obligatorio")
    private PublicationStatus status;

    @NotBlank(message = "La zona de entrega es obligatoria")
    private String location;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    private List<String> imageUrls;

    // Texto copiado desde Maps; separado de la zona publica. Obligatorio para nuevas publicaciones.
    @NotBlank(message = "La dirección exacta de entrega es obligatoria")
    @jakarta.validation.constraints.Size(max = 255, message = "La dirección admite hasta 255 caracteres")
    @jakarta.validation.constraints.Pattern(
            regexp = "(?is)^(?!.*(?:://|www\\.|maps\\.app\\.goo\\.gl|goo\\.gl/maps)).*$",
            message = "Pegá la dirección de Google Maps, no un enlace")
    private String address;
    private Double latitude;
    private Double longitude;

    public PublicationCreateDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public void setStatus(PublicationStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
