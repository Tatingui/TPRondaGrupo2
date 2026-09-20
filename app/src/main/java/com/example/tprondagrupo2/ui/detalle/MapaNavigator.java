package com.example.tprondagrupo2.ui.detalle;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/** Elige las alternativas de mapas sin depender de una pantalla ni mostrar mensajes. */
public final class MapaNavigator {

    public interface Lanzador {
        /** paquete null permite que Android elija la aplicación. */
        boolean abrir(String uri, String paquete);
    }

    private final Lanzador lanzador;

    public MapaNavigator(Lanzador lanzador) {
        this.lanzador = lanzador;
    }

    public boolean abrir(String destino) {
        if (destino == null || destino.trim().isEmpty()) return false;

        String query = codificar(destino.trim());
        if (lanzador.abrir("https://www.google.com/maps/dir/?api=1&destination=" + query,
                "com.google.android.apps.maps")) {
            return true;
        }
        return lanzador.abrir("geo:0,0?q=" + query, null);
    }

    private String codificar(String destino) {
        try {
            return URLEncoder.encode(destino, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 debe estar disponible", e);
        }
    }
}
