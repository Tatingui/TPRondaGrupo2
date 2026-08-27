package com.ronda.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AuthResponseTest {

    @Test
    void testOkDevuelveSuccessTrueConToken() {
        // El factory de exito tiene que marcar success=true y conservar el token
        AuthResponse response = AuthResponse.ok("abc123", "Login exitoso");

        assertTrue(response.isSuccess());
        assertEquals("abc123", response.getToken());
        assertEquals("Login exitoso", response.getMessage());
    }

    @Test
    void testOkAceptaTokenNull() {
        // Register y sendOtp responden ok pero sin token todavia
        AuthResponse response = AuthResponse.ok(null, "Código enviado");

        assertTrue(response.isSuccess());
        assertNull(response.getToken());
        assertEquals("Código enviado", response.getMessage());
    }

    @Test
    void testErrorDevuelveSuccessFalseYTokenNull() {
        // El factory de error nunca debe filtrar un token
        AuthResponse response = AuthResponse.error("Usuario no encontrado");

        assertFalse(response.isSuccess());
        assertNull(response.getToken());
        assertEquals("Usuario no encontrado", response.getMessage());
    }

    @Test
    void testConstructorVacioDejaSuccessEnFalse() {
        // Default de Java para boolean, y lo que Jackson usa al deserializar
        AuthResponse response = new AuthResponse();

        assertFalse(response.isSuccess());
        assertNull(response.getToken());
        assertNull(response.getMessage());
    }

    @Test
    void testSettersYGetters() {
        // Valida el ida y vuelta de los tres campos
        AuthResponse response = new AuthResponse();
        response.setToken("t");
        response.setMessage("m");
        response.setSuccess(true);

        assertEquals("t", response.getToken());
        assertEquals("m", response.getMessage());
        assertTrue(response.isSuccess());
    }
}
