package com.example.tprondagrupo2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Perfil público de un usuario (GET /usuarios/{id}/publico).
 * Tiene los mismos datos que el Vendedor (reputación, antigüedad, zona)
 * más sus publicaciones activas.
 */
public class PerfilPublico extends Vendedor {

    private List<Publicacion> publicacionesActivas;

    public PerfilPublico() {
        // Constructor vacio requerido por Gson
    }

    public List<Publicacion> getPublicacionesActivas() {
        if (publicacionesActivas == null) {
            publicacionesActivas = new ArrayList<>();
        }
        return publicacionesActivas;
    }
}
