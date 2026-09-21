package com.example.tprondagrupo2.model;

import java.io.Serializable;

/**
 * Datos del vendedor que se muestran en el detalle de la publicacion
 * y en su perfil publico: reputacion, cantidad de ventas y antiguedad.
 */
public class Vendedor implements Serializable, ReputacionInfo {

    private Long id;
    private String nombre;
    private double reputacion;        // Puntaje de 0 a 5 (promedio de opiniones)
    private int cantidadVentas;       // Operaciones concretadas como vendedor
    private int cantidadCompras;      // Operaciones concretadas como comprador
    private int cantidadOpiniones;    // Opiniones recibidas
    private String miembroDesde;      // Ej: "Marzo 2023"
    private String ubicacion;         // Zona del vendedor

    public Vendedor() {
        // Constructor vacio requerido por Gson
    }

    public Vendedor(Long id, String nombre, double reputacion, int cantidadVentas,
                    int cantidadOpiniones, String miembroDesde, String ubicacion) {
        this.id = id;
        this.nombre = nombre;
        this.reputacion = reputacion;
        this.cantidadVentas = cantidadVentas;
        this.cantidadOpiniones = cantidadOpiniones;
        this.miembroDesde = miembroDesde;
        this.ubicacion = ubicacion;
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

    @Override
    public double getReputacion() {
        return reputacion;
    }

    public void setReputacion(double reputacion) {
        this.reputacion = reputacion;
    }

    @Override
    public int getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(int cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    @Override
    public int getCantidadCompras() {
        return cantidadCompras;
    }

    public void setCantidadCompras(int cantidadCompras) {
        this.cantidadCompras = cantidadCompras;
    }

    @Override
    public int getCantidadOpiniones() {
        return cantidadOpiniones;
    }

    public void setCantidadOpiniones(int cantidadOpiniones) {
        this.cantidadOpiniones = cantidadOpiniones;
    }

    @Override
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
}
