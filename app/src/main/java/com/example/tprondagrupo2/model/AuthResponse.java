package com.example.tprondagrupo2.model;

public class AuthResponse {

    private String token;
    private String message;
    private boolean success;

    public AuthResponse() {
        // Constructor vacio requerido por Gson
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
