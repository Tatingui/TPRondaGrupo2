package com.ronda.backend.dto;

/**
 * Datos del perfil del usuario logueado.
 * Se devuelve en GET /usuarios/me.
 */
public class UserProfileResponse {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String zona;
    private String profileImageUrl;
    private String miembroDesde;  // Ej: "Septiembre 2026"

    // Reputacion: se construye con las calificaciones recibidas (punto 9)
    private double reputacion;          // Promedio de estrellas
    private int cantidadOpiniones;      // Calificaciones recibidas
    private int cantidadVentas;         // Operaciones concretadas como vendedor
    private int cantidadCompras;        // Operaciones concretadas como comprador

    public UserProfileResponse() {
    }

    public double getReputacion() {
        return reputacion;
    }

    public void setReputacion(double reputacion) {
        this.reputacion = reputacion;
    }

    public int getCantidadOpiniones() {
        return cantidadOpiniones;
    }

    public void setCantidadOpiniones(int cantidadOpiniones) {
        this.cantidadOpiniones = cantidadOpiniones;
    }

    public int getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(int cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public int getCantidadCompras() {
        return cantidadCompras;
    }

    public void setCantidadCompras(int cantidadCompras) {
        this.cantidadCompras = cantidadCompras;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getMiembroDesde() {
        return miembroDesde;
    }

    public void setMiembroDesde(String miembroDesde) {
        this.miembroDesde = miembroDesde;
    }
}
