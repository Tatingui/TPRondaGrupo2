package com.example.tprondagrupo2.ui.publish;

import java.util.Locale;

/** Valida texto postal; no geocodifica ni promete exactitud del lugar ingresado. */
public final class DeliveryAddress {
    private DeliveryAddress() {}

    public static boolean isValid(String address) {
        if (address == null || address.trim().isEmpty() || address.trim().length() > 255) return false;
        String text = address.trim().toLowerCase(Locale.ROOT);
        return !text.contains("://") && !text.contains("www.")
                && !text.contains("maps.app.goo.gl") && !text.contains("goo.gl/maps");
    }
}
