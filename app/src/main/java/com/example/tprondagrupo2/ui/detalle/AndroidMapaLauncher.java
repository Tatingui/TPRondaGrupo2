package com.example.tprondagrupo2.ui.detalle;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import java.util.function.Consumer;

/** Adaptador Android: traduce la solicitud en un Intent y detecta apps no disponibles. */
public final class AndroidMapaLauncher implements MapaNavigator.Lanzador {

    private final Consumer<Intent> iniciarActividad;

    public AndroidMapaLauncher(Consumer<Intent> iniciarActividad) {
        this.iniciarActividad = iniciarActividad;
    }

    @Override
    public boolean abrir(String uri, String paquete) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (paquete != null) intent.setPackage(paquete);
        try {
            iniciarActividad.accept(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
