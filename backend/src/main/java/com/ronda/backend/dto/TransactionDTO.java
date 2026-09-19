package com.ronda.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Operacion concretada vista desde el historial del usuario.
 * El campo "tipo" indica si es COMPRA o VENTA segun quien consulta.
 */
public class TransactionDTO {

    private Long id;
    private String tipo; // "COMPRA" o "VENTA"
    private String tituloPublicacion;
    private String imagenUrl; // Primera imagen de la publicacion (puede ser null)
    private Double montoFinal;
    private String nombreContraparte;
    private Long idContraparte;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaEntrega;

    private boolean puedeCalificar;
    private boolean yaCalificado;

    public TransactionDTO() {
    }

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTituloPublicacion() { return tituloPublicacion; }
    public void setTituloPublicacion(String tituloPublicacion) { this.tituloPublicacion = tituloPublicacion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Double getMontoFinal() { return montoFinal; }
    public void setMontoFinal(Double montoFinal) { this.montoFinal = montoFinal; }

    public String getNombreContraparte() { return nombreContraparte; }
    public void setNombreContraparte(String nombreContraparte) { this.nombreContraparte = nombreContraparte; }

    public Long getIdContraparte() { return idContraparte; }
    public void setIdContraparte(Long idContraparte) { this.idContraparte = idContraparte; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public boolean isPuedeCalificar() { return puedeCalificar; }
    public void setPuedeCalificar(boolean puedeCalificar) { this.puedeCalificar = puedeCalificar; }

    public boolean isYaCalificado() { return yaCalificado; }
    public void setYaCalificado(boolean yaCalificado) { this.yaCalificado = yaCalificado; }
}
