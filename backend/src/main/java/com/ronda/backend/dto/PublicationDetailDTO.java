package com.ronda.backend.dto;

/**
 * Detalle completo de una publicacion (GET /publications/{id}).
 * Ademas de los datos del listado, trae el resumen del vendedor.
 */
public class PublicationDetailDTO extends PublicationDTO {

    private SellerDTO vendedor;

    public PublicationDetailDTO() {
    }

    public SellerDTO getVendedor() {
        return vendedor;
    }

    public void setVendedor(SellerDTO vendedor) {
        this.vendedor = vendedor;
    }
}
