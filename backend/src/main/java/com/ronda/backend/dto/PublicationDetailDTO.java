package com.ronda.backend.dto;

/**
 * Detalle completo de una publicacion (GET /publications/{id}).
 * Ademas de los datos del listado, trae el resumen del vendedor y
 * datos que dependen de quien esta mirando.
 */
public class PublicationDetailDTO extends PublicationDTO {

    private SellerDTO vendedor;

    // true si quien mira es el propio vendedor
    private boolean owner;

    // La direccion exacta solo se manda al vendedor o a quien tenga una oferta aceptada
    private boolean addressVisible;
    private String address;
    private Double latitude;
    private Double longitude;

    // Oferta aceptada de quien mira; si no tiene una, su ultima oferta (null si no oferto).
    private OfferDTO myOffer;

    public PublicationDetailDTO() {
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isAddressVisible() {
        return addressVisible;
    }

    public void setAddressVisible(boolean addressVisible) {
        this.addressVisible = addressVisible;
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

    public OfferDTO getMyOffer() {
        return myOffer;
    }

    public void setMyOffer(OfferDTO myOffer) {
        this.myOffer = myOffer;
    }

    public SellerDTO getVendedor() {
        return vendedor;
    }

    public void setVendedor(SellerDTO vendedor) {
        this.vendedor = vendedor;
    }
}
