package com.example.tprondagrupo2.util;

import java.text.NumberFormat;
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
}
