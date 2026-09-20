package com.ronda.backend.dto;

import jakarta.validation.constraints.Size;

/**
 * Campos editables del perfil. PUT /usuarios/me.
 * Solo se actualizan los campos que no vengan null.
 */
public class UserProfileUpdateRequest {

    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    private String nombre;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;

    @Size(max = 100, message = "La zona no puede superar los 100 caracteres")
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
