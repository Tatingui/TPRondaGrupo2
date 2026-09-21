package com.example.tprondagrupo2.util;

public class PublicationConstants {

    public static final String[] CATEGORIES = {"Deportes", "Hogar", "Electrónica", "Ropa", "Otros"};

    public static String getCategoryName(Long categoryId) {
        if (categoryId == null || categoryId < 1 || categoryId > CATEGORIES.length) return "Otros";
        return CATEGORIES[(int) (categoryId - 1)];
    }

    public static String translateStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "ACTIVE":
                return "Activa";
            case "PAUSED":
                return "Pausada";
            case "SOLD":
                return "Vendida";
            case "NEW":
                return "Nuevo";
            case "LIKE_NEW":
                return "Como nuevo";
            case "USED":
                return "Usado";
            default:
                return status;
        }
    }

    public static String toBackendStatus(String uiStatus) {
        if ("Como nuevo".equals(uiStatus)) return "LIKE_NEW";
        if ("Usado".equals(uiStatus)) return "USED";
        return "NEW";
    }
}
