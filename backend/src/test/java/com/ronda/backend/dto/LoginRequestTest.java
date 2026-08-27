package com.ronda.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "1234";

    private LoginRequest request;

    @BeforeEach
    void setUp() {
        request = new LoginRequest(EMAIL, PASSWORD);
    }

    @Test
    void testConstructorAsignaEmailYPassword() {
        // Valida que el constructor deja ambos campos con los valores recibidos
        assertEquals(EMAIL, request.getEmail());
        assertEquals(PASSWORD, request.getPassword());
    }

    @Test
    void testConstructorVacioDejaLosCamposEnNull() {
        // El constructor vacio lo necesita Jackson para deserializar el body
        LoginRequest vacio = new LoginRequest();

        org.junit.jupiter.api.Assertions.assertNull(vacio.getEmail());
        org.junit.jupiter.api.Assertions.assertNull(vacio.getPassword());
    }

    @Test
    void testSettersActualizanLosValores() {
        // Valida que los setters reemplazan lo puesto por el constructor
        request.setEmail("otro@mail.com");
        request.setPassword("9999");

        assertEquals("otro@mail.com", request.getEmail());
        assertEquals("9999", request.getPassword());
    }
}
