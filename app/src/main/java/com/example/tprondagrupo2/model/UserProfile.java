package com.example.tprondagrupo2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo para la respuesta de GET /usuarios/me.
 * Contiene los datos del perfil del usuario logueado.
 */
public class UserProfile implements ReputacionInfo {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private boolean telefonoVisible = true;
    private String zona;
    private String profileImageUrl;
    private String miembroDesde;  // Ej: "Septiembre 2026"

    // Reputacion (se completa con las calificaciones del punto 9)
    private double reputacion;
    private int cantidadOpiniones;
    private int cantidadVentas;     // Operaciones concretadas como vendedor
    private int cantidadCompras;    // Operaciones concretadas como comprador
    private List<String> insignias = new ArrayList<>();

    public UserProfile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
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

    @Override
    public double getReputacion() {
        return reputacion;
    }

    @Override
    public int getCantidadOpiniones() {
        return cantidadOpiniones;
    }

    @Override
    public int getCantidadVentas() {
        return cantidadVentas;
    }

    @Override
    public int getCantidadCompras() {
        return cantidadCompras;
    }

    @Override
    public String getMiembroDesde() {
        return miembroDesde;
    }

    public void setMiembroDesde(String miembroDesde) {
        this.miembroDesde = miembroDesde;
    }

    public boolean isTelefonoVisible() {
        return telefonoVisible;
    }

    public void setTelefonoVisible(boolean telefonoVisible) {
        this.telefonoVisible = telefonoVisible;
    }

    public List<String> getInsignias() {
        return insignias != null ? insignias : new ArrayList<>();
    }

    public void setInsignias(List<String> insignias) {
        this.insignias = insignias != null ? insignias : new ArrayList<>();
    }
}
