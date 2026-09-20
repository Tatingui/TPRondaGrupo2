package com.example.tprondagrupo2.ui.detalle;

import com.example.tprondagrupo2.model.Publicacion;

/** Regla de la acción Cómo llegar, independiente de vistas y aplicaciones de mapas. */
public final class ComoLlegarResolver {

    /** Devuelve el destino autorizado o null cuando no corresponde mostrar la acción. */
    public String resolver(Publicacion publicacion) {
        if (publicacion == null || publicacion.isOwner() || !publicacion.isAddressVisible()) {
            return null;
        }

        Double latitud = publicacion.getLatitude();
        Double longitud = publicacion.getLongitude();
        if (enRango(latitud, 90) && enRango(longitud, 180)) {
            return latitud + "," + longitud;
        }

        String direccion = publicacion.getAddress();
        return direccion == null || direccion.trim().isEmpty() ? null : direccion.trim();
    }

    private boolean enRango(Double valor, double limite) {
        return valor != null && Double.isFinite(valor) && Math.abs(valor) <= limite;
    }
}
