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

    /**
     * Inicial del nombre para usar como avatar cuando no hay foto real.
     */
    public String getInicial() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "?";
        }
        return nombre.trim().substring(0, 1).toUpperCase();
    }

    /**
     * Nivel de reputacion derivado del puntaje, al estilo de un semaforo:
     * permite pintar el color y mostrar una etiqueta sin repetir la logica.
     */
    public NivelReputacion getNivel() {
        if (cantidadOpiniones == 0) {
            return NivelReputacion.SIN_CALIFICACIONES;
        }
        if (reputacion >= 4.5) {
            return NivelReputacion.EXCELENTE;
        }
        if (reputacion >= 3.5) {
            return NivelReputacion.BUENO;
        }
        if (reputacion >= 2.5) {
            return NivelReputacion.REGULAR;
        }
        return NivelReputacion.MALO;
    }

    public enum NivelReputacion {
        EXCELENTE("Excelente vendedor", 0xFF2E7D32),
        BUENO("Buen vendedor", 0xFF9E9D24),
        REGULAR("Reputacion regular", 0xFFEF6C00),
        MALO("Reputacion baja", 0xFFC62828),
        SIN_CALIFICACIONES("Sin calificaciones aun", 0xFF757575);

        private final String etiqueta;
        private final int color;

        NivelReputacion(String etiqueta, int color) {
            this.etiqueta = etiqueta;
            this.color = color;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        public int getColor() {
            return color;
        }
    }
}
