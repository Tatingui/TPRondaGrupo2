package com.example.tprondagrupo2.model;

import java.io.Serializable;

/**
 * Datos del vendedor que se muestran en el detalle de la publicación
 * y en su perfil público: reputación, cantidad de ventas y antigüedad.
 */
public class Vendedor implements Serializable {

    private String id;
    private String nombre;
    private double reputacion;        // Puntaje de 0 a 5 (promedio de opiniones)
    private int cantidadVentas;       // Operaciones concretadas
    private int cantidadOpiniones;    // Opiniones recibidas
    private String miembroDesde;      // Ej: "Marzo 2023"
    private String ubicacion;         // Zona del vendedor

    public Vendedor() {
        // Constructor vacio requerido por Gson
    }

    public Vendedor(String id, String nombre, double reputacion, int cantidadVentas,
                    int cantidadOpiniones, String miembroDesde, String ubicacion) {
        this.id = id;
        this.nombre = nombre;
        this.reputacion = reputacion;
        this.cantidadVentas = cantidadVentas;
        this.cantidadOpiniones = cantidadOpiniones;
        this.miembroDesde = miembroDesde;
        this.ubicacion = ubicacion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
     * Nivel de reputación derivado del puntaje, al estilo de un semáforo:
     * permite pintar el color y mostrar una etiqueta sin repetir la lógica.
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
        REGULAR("Reputación regular", 0xFFEF6C00),
        MALO("Reputación baja", 0xFFC62828),
        SIN_CALIFICACIONES("Sin calificaciones aún", 0xFF757575);

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
