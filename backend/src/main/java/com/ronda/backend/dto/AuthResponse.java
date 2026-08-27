package com.ronda.backend.dto;

/**
 * Respuesta comun de todos los endpoints de autenticacion.
 * El cliente Android considera exitosa una llamada solo si el HTTP es 2xx
 * Y ademas success == true, por eso el flag viaja siempre en el body.
 */
public class AuthResponse {

    private String token;
    private String message;
    private boolean success;

    public AuthResponse() {
    }

    public AuthResponse(String token, String message, boolean success) {
        this.token = token;
        this.message = message;
        this.success = success;
    }

    public static AuthResponse ok(String token, String message) {
        return new AuthResponse(token, message, true);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(null, message, false);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
