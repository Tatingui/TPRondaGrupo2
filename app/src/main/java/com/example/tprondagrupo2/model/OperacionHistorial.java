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

    /** @return Identificador único de la transacción. */
    public Long getId() { return id; }

    /** @return Tipo de operación ("COMPRA" o "VENTA"). */
    public String getTipo() { return tipo; }

    /** @return Título del artículo o publicación asociada. */
    public String getTituloPublicacion() { return tituloPublicacion; }

    /** @return URL de la primera imagen de la publicación. */
    public String getImagenUrl() { return imagenUrl; }

    /** @return Monto final en que se concretó la operación. */
    public double getMontoFinal() { return montoFinal; }

    /** @return Nombre del usuario contraparte (comprador o vendedor según corresponda). */
    public String getNombreContraparte() { return nombreContraparte; }

    /** @return Identificador del usuario contraparte. */
    public Long getIdContraparte() { return idContraparte; }

    /** @return Fecha de creación de la transacción en formato ISO date-time. */
    public String getFecha() { return fecha; }

    /** @return Fecha de confirmación de la entrega en formato ISO date-time (null si no fue entregado). */
    public String getFechaEntrega() { return fechaEntrega; }

    /** @return true si la operación es elegible para ser calificada por el usuario. */
    public boolean isPuedeCalificar() { return puedeCalificar; }

    /** @return true si el usuario autenticado ya emitió una calificación para esta transacción. */
    public boolean isYaCalificado() { return yaCalificado; }

    // ── Setters ──

    /** Establece el estado de si la operación ya fue calificada. */
    public void setYaCalificado(boolean yaCalificado) { this.yaCalificado = yaCalificado; }

    /** Establece si la operación es elegible para calificación. */
    public void setPuedeCalificar(boolean puedeCalificar) { this.puedeCalificar = puedeCalificar; }

    /** Establece la fecha de confirmación de la entrega. */
    public void setFechaEntrega(String fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    // ── Helpers ──

    /** @return true si el tipo de operación es "COMPRA". */
    public boolean isCompra() {
        return "COMPRA".equals(tipo);
    }

    /** @return true si la entrega fue confirmada (fechaEntrega no es null). */
    public boolean entregaConfirmada() {
        return fechaEntrega != null;
    }
}
