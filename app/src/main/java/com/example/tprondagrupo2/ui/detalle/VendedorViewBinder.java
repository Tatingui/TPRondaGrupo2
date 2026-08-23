package com.example.tprondagrupo2.ui.detalle;

import android.content.res.Resources;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Vendedor;

/**
 * Centraliza el formateo de la reputación del vendedor para que la sección
 * del detalle y el perfil público muestren exactamente lo mismo sin duplicar
 * la lógica de textos, estrellas y color del nivel.
 */
public final class VendedorViewBinder {

    private VendedorViewBinder() {
        // Clase de utilidades
    }

    /**
     * Texto de reputación: "4.5 (128 opiniones)" o el aviso de sin opiniones.
     */
    public static String textoReputacion(@NonNull Resources res, @NonNull Vendedor vendedor) {
        if (vendedor.getCantidadOpiniones() <= 0) {
            return res.getString(R.string.vendedor_sin_opiniones);
        }
        return res.getString(R.string.vendedor_reputacion_formato,
                vendedor.getReputacion(), vendedor.getCantidadOpiniones());
    }

    /**
     * Aplica avatar (inicial), estrellas, texto y color de nivel a las vistas
     * que le pasen. Cualquier vista puede ser null si esa pantalla no la usa.
     */
    public static void bindReputacion(@NonNull Vendedor vendedor,
                                      TextView tvAvatar,
                                      RatingBar rbReputacion,
                                      TextView tvReputacion,
                                      TextView tvNivel) {
        if (tvAvatar != null) {
            tvAvatar.setText(vendedor.getInicial());
        }
        if (rbReputacion != null) {
            rbReputacion.setRating((float) vendedor.getReputacion());
        }
        if (tvReputacion != null) {
            tvReputacion.setText(textoReputacion(tvReputacion.getResources(), vendedor));
        }
        if (tvNivel != null) {
            Vendedor.NivelReputacion nivel = vendedor.getNivel();
            tvNivel.setText(nivel.getEtiqueta());
            tvNivel.setTextColor(nivel.getColor());
        }
    }
}
