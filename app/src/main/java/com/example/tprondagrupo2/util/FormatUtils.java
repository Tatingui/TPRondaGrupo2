package com.example.tprondagrupo2.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

    private static final Locale LOCALE_AR = Locale.forLanguageTag("es-AR");

    public static String formatPrice(double price) {
        return NumberFormat.getCurrencyInstance(LOCALE_AR).format(price);
    }

    public static String formatPrice(Double price) {
        if (price == null) {
            return NumberFormat.getCurrencyInstance(LOCALE_AR).format(0.0);
        }
        return formatPrice(price.doubleValue());
    }

    /**
     * Convierte una cadena ISO (ej. "2025-05-10T14:30:00") a formato dd/MM/yyyy HH:mm.
     *
     * @param isoDate Cadena con la fecha en formato ISO.
     * @return Fecha formateada o la cadena original si hay error.
     */
    public static String formatFechaDisplay(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return "-";
        }
        try {
            SimpleDateFormat inFormat;
            if (isoDate.contains("T")) {
                inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            } else {
                inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }
            Date date = inFormat.parse(isoDate);
            if (date != null) {
                SimpleDateFormat outFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                return outFormat.format(date);
            }
        } catch (Exception ignored) { }
        return isoDate;
    }

    /**
     * Verifica si un timestamp ISO está dentro de los últimos 7 días respecto a ahora.
     *
     * @param isoDate Cadena con la fecha en formato ISO.
     * @return true si la fecha está dentro de la ventana de 7 días, false en caso contrario.
     */
    public static boolean isDentroDe7Dias(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf;
            if (isoDate.contains("T")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }
            Date date = sdf.parse(isoDate);
            if (date != null) {
                long diffMs = System.currentTimeMillis() - date.getTime();
                long maxMs = 7L * 24 * 60 * 60 * 1000L;
                return diffMs >= 0 && diffMs <= maxMs;
            }
        } catch (Exception ignored) { }
        return false;
    }
}
