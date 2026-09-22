package com.ronda.backend.dto;

/**
 * Resumen del vendedor que se muestra en el detalle de la publicacion.
 * Los nombres de los campos coinciden con el modelo Vendedor de Android.
 */
public class SellerDTO {
    private Long id;
    private String nombre;
    private double reputacion;       // Promedio de estrellas (0 si no tiene calificaciones)
    private int cantidadVentas;      // Operaciones concretadas como vendedor
    private int cantidadCompras;     // Operaciones concretadas como comprador
    private int cantidadOpiniones;   // Calificaciones recibidas
    private String miembroDesde;     // Ej: "Septiembre 2026"
    private String ubicacion;        // Zona del vendedor
    private String profileImageUrl;  // URL de la foto de perfil

    public SellerDTO() {
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

    public double getReputacion() {
        return reputacion;
    }

    public void setReputacion(double reputacion) {
        this.reputacion = reputacion;
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

    public int getCantidadOpiniones() {
        return cantidadOpiniones;
    }

    public void setCantidadOpiniones(int cantidadOpiniones) {
        this.cantidadOpiniones = cantidadOpiniones;
    }

    public String getMiembroDesde() {
        return miembroDesde;
    }

    public void setMiembroDesde(String miembroDesde) {
        this.miembroDesde = miembroDesde;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}