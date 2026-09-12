package com.example.tprondagrupo2.model;

/**
 * Body para PUT /usuarios/me.
 * Solo los campos que no vengan null se actualizan en el backend.
 */
public class UserProfileUpdateRequest {

    private String nombre;
    private String telefono;
    private String zona;

    public UserProfileUpdateRequest() {
    }

    public UserProfileUpdateRequest(String nombre, String telefono, String zona) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.zona = zona;
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
