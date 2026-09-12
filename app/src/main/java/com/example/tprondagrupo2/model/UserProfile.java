package com.example.tprondagrupo2.model;

/**
 * Modelo para la respuesta de GET /usuarios/me.
 * Contiene los datos del perfil del usuario logueado.
 */
public class UserProfile {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String zona;
    private String miembroDesde;  // Ej: "Septiembre 2026"

    public UserProfile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getMiembroDesde() {
        return miembroDesde;
    }

    public void setMiembroDesde(String miembroDesde) {
        this.miembroDesde = miembroDesde;
    }
}
