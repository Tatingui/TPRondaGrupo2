package com.ronda.backend.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Perfil publico de un usuario (GET /usuarios/{id}/publico).
 * Tiene los mismos datos que el resumen del vendedor (reputacion, antiguedad, zona)
 * mas sus publicaciones activas. No incluye email ni telefono.
 */
public class PublicProfileDTO extends SellerDTO {

    private List<PublicationDTO> publicacionesActivas = new ArrayList<>();

    public PublicProfileDTO() {
    }

    public List<PublicationDTO> getPublicacionesActivas() {
        return publicacionesActivas;
    }

    public void setPublicacionesActivas(List<PublicationDTO> publicacionesActivas) {
        this.publicacionesActivas = publicacionesActivas;
    }
}
