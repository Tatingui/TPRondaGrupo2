package com.example.tprondagrupo2.ui.detalle;

import android.content.res.Resources;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.ReputacionInfo;
import com.example.tprondagrupo2.model.Vendedor;

/**
 * Centraliza el formateo de la reputacion para que la seccion
 * del detalle, el perfil propio y el perfil publico muestren exactamente
 * lo mismo sin duplicar la logica de textos, estrellas y color del nivel.
 *
 * Trabaja con ReputacionInfo: tanto Vendedor como UserProfile la implementan,
 * eliminando la necesidad de crear un Vendedor intermedio.
 */
public final class VendedorViewBinder {

    private VendedorViewBinder() {
        // Clase de utilidades
    }

    /**
     * Texto de reputacion: "4.5 (128 opiniones)" o el aviso de sin opiniones.
     */
    public static String textoReputacion(@NonNull Resources res, @NonNull ReputacionInfo info) {
        if (info.getCantidadOpiniones() <= 0) {
            return res.getString(R.string.vendedor_sin_opiniones);
        }
        return res.getString(R.string.vendedor_reputacion_formato,
                info.getReputacion(), info.getCantidadOpiniones());
    }

    /**
     * Calcula el nivel de reputacion a partir de los datos de cualquier
     * modelo que implemente ReputacionInfo.
     */
    public static Vendedor.NivelReputacion calcularNivel(@NonNull ReputacionInfo info) {
        if (info.getCantidadOpiniones() == 0) {
            return Vendedor.NivelReputacion.SIN_CALIFICACIONES;
        }
        if (info.getReputacion() >= 4.5) {
            return Vendedor.NivelReputacion.EXCELENTE;
        }
        if (info.getReputacion() >= 3.5) {
            return Vendedor.NivelReputacion.BUENO;
        }
        if (info.getReputacion() >= 2.5) {
            return Vendedor.NivelReputacion.REGULAR;
        }
        return Vendedor.NivelReputacion.MALO;
    }

    /**
     * Aplica avatar (inicial), estrellas, texto y color de nivel a las vistas
     * que le pasen. Cualquier vista puede ser null si esa pantalla no la usa.
     */
    public static void bindReputacion(@NonNull ReputacionInfo info,
                                      TextView tvAvatar,
                                      RatingBar rbReputacion,
                                      TextView tvReputacion,
                                      TextView tvNivel) {
        if (tvAvatar != null) {
            String nombre = info.getNombre();
            tvAvatar.setText(nombre != null && !nombre.trim().isEmpty()
                    ? nombre.trim().substring(0, 1).toUpperCase() : "?");
        }
        if (rbReputacion != null) {
            rbReputacion.setRating((float) info.getReputacion());
        }
        if (tvReputacion != null) {
            tvReputacion.setText(textoReputacion(tvReputacion.getResources(), info));
        }
        if (tvNivel != null) {
            Vendedor.NivelReputacion nivel = calcularNivel(info);
            tvNivel.setText(nivel.getEtiqueta());
            tvNivel.setTextColor(nivel.getColor());
        }
    }
}
