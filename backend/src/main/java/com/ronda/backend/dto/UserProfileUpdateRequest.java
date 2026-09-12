package com.ronda.backend.dto;

/**
 * Campos editables del perfil. PUT /usuarios/me.
 * Solo se actualizan los campos que no vengan null.
 */
public class UserProfileUpdateRequest {

    private String nombre;
    private String telefono;
    private String zona;

    public UserProfileUpdateRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }
}
