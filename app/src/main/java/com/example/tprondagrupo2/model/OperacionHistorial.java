package com.example.tprondagrupo2.model;

import java.io.Serializable;

/**
 * Representa una operación concretada (compra o venta) en el historial.
 * Mapea al TransactionDTO del backend.
 */
public class OperacionHistorial implements Serializable {

    private Long id;
    private String tipo;               // "COMPRA" o "VENTA"
    private String tituloPublicacion;
    private String imagenUrl;
    private double montoFinal;
    private String nombreContraparte;
    private Long idContraparte;
    private String fecha;              // ISO date-time
    private String fechaEntrega;       // null si no se confirmó aún
    private boolean puedeCalificar;
    private boolean yaCalificado;

    public OperacionHistorial() {
        // Constructor vacío requerido por Gson
    }

    // ── Getters ──

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getTituloPublicacion() { return tituloPublicacion; }
    public String getImagenUrl() { return imagenUrl; }
    public double getMontoFinal() { return montoFinal; }
    public String getNombreContraparte() { return nombreContraparte; }
    public Long getIdContraparte() { return idContraparte; }
    public String getFecha() { return fecha; }
    public String getFechaEntrega() { return fechaEntrega; }
    public boolean isPuedeCalificar() { return puedeCalificar; }
    public boolean isYaCalificado() { return yaCalificado; }

    // ── Helpers ──

    public boolean isCompra() {
        return "COMPRA".equals(tipo);
    }

    public boolean entregaConfirmada() {
        return fechaEntrega != null;
    }
}
