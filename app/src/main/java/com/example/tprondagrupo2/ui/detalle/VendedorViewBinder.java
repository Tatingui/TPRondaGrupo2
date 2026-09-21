package com.example.tprondagrupo2.ui.detalle;

import android.content.res.Resources;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.ReputacionInfo;

/**
 * Centraliza el formateo de la reputacion para que la seccion
 * del detalle, el perfil propio y el perfil publico muestren exactamente
 * lo mismo sin duplicar la logica de textos, estrellas y color del nivel.
 *
 * Trabaja con ReputacionInfo: tanto Vendedor como UserProfile la implementan.
 */
public final class VendedorViewBinder {

    private VendedorViewBinder() {
        // Clase de utilidades
    }

    public enum NivelReputacion {
        EXCELENTE("Excelente vendedor", 0xFF2E7D32),
        BUENO("Buen vendedor", 0xFF9E9D24),
        REGULAR("Reputacion regular", 0xFFEF6C00),
        MALO("Reputacion baja", 0xFFC62828),
        SIN_CALIFICACIONES("Sin calificaciones aun", 0xFF757575);

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
     * Obtiene la inicial del nombre para mostrar en el avatar.
     */
    public static String obtenerInicial(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "?";
        }
        return nombre.trim().substring(0, 1).toUpperCase();
    }

    /**
     * Calcula el nivel de reputacion a partir de los datos de cualquier
     * modelo que implemente ReputacionInfo.
     */
    public static NivelReputacion calcularNivel(@NonNull ReputacionInfo info) {
        if (info.getCantidadOpiniones() == 0) {
            return NivelReputacion.SIN_CALIFICACIONES;
        }
        if (info.getReputacion() >= 4.5) {
            return NivelReputacion.EXCELENTE;
        }
        if (info.getReputacion() >= 3.5) {
            return NivelReputacion.BUENO;
        }
        if (info.getReputacion() >= 2.5) {
            return NivelReputacion.REGULAR;
        }
        return NivelReputacion.MALO;
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
            tvAvatar.setText(obtenerInicial(info.getNombre()));
        }
        if (rbReputacion != null) {
            rbReputacion.setRating((float) info.getReputacion());
        }
        if (tvReputacion != null) {
            tvReputacion.setText(textoReputacion(tvReputacion.getResources(), info));
        }
        if (tvNivel != null) {
            NivelReputacion nivel = calcularNivel(info);
            tvNivel.setText(nivel.getEtiqueta());
            tvNivel.setTextColor(nivel.getColor());
        }
    }
}
