package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class LoginRequestTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "1234";

    private LoginRequest request;

    @Before
    public void setUp() {
        request = new LoginRequest(EMAIL, PASSWORD);
    }

    @Test
    public void testConstructorAsignaEmailYPassword() {
        // Valida que el constructor deja ambos campos con los valores recibidos
        assertEquals(EMAIL, request.getEmail());
        assertEquals(PASSWORD, request.getPassword());
    }

    @Test
    public void testGettersDevuelvenLosValoresAsignados() {
        // Valida que los getters no alteran ni pierden el valor guardado
        assertEquals(EMAIL, request.getEmail());
        assertEquals(PASSWORD, request.getPassword());
    }

    @Test
    public void testSettersActualizanLosValores() {
        // Valida que los setters reemplazan los valores puestos por el constructor
        request.setEmail("otro@mail.com");
        request.setPassword("9999");

        assertEquals("otro@mail.com", request.getEmail());
        assertEquals("9999", request.getPassword());
    }
}
