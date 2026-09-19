package com.example.tprondagrupo2.model;

/**
 * Interfaz que unifica los datos de reputacion entre UserProfile y Vendedor.
 * Permite que VendedorViewBinder trabaje con ambos modelos sin necesidad
 * de crear un Vendedor intermedio solo para mostrar la reputacion.
 */
public interface ReputacionInfo {
    String getNombre();
    double getReputacion();
    int getCantidadVentas();
    int getCantidadCompras();
    int getCantidadOpiniones();
    String getMiembroDesde();
}
