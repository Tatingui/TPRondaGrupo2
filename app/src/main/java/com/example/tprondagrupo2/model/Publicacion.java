package com.example.tprondagrupo2.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Publicacion implements Serializable {

    private String id;
    private String titulo;
    private List<String> fotos;
    private String descripcion;
    private String categoria;
    private String estado;
    private double precio;
    private String fechaPublicacion;

    public Publicacion() {
        // Constructor vacio requerido por Gson
        this.fotos = new ArrayList<>();
    }

    public Publicacion(String id, String titulo, List<String> fotos, String descripcion,
                       String categoria, String estado, double precio, String fechaPublicacion) {
        this.id = id;
        this.titulo = titulo;
        this.fotos = fotos != null ? fotos : new ArrayList<>();
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.estado = estado;
        this.precio = precio;
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos != null ? fotos : new ArrayList<>();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(String fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public int getCantidadFotos() {
        return fotos != null ? fotos.size() : 0;
    }
}
