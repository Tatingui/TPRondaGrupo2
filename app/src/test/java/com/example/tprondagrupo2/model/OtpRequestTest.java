package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class OtpRequestTest {

    private static final String EMAIL = "test@mail.com";
    private static final String CODE = "123456";

    private OtpRequest request;

    @Before
    public void setUp() {
        request = new OtpRequest(EMAIL, CODE);
    }

    @Test
    public void testConstructorAsignaEmailYCode() {
        // Valida que el constructor deja email y codigo con los valores recibidos
        assertEquals(EMAIL, request.getEmail());
        assertEquals(CODE, request.getCode());
    }

    @Test
    public void testGettersDevuelvenLosValoresAsignados() {
        // Valida que los getters devuelven el email y el codigo guardados
        assertEquals(EMAIL, request.getEmail());
        assertEquals(CODE, request.getCode());
    }

    @Test
    public void testSettersActualizanLosValores() {
        // Valida que los setters permiten cambiar email y codigo
        request.setEmail("otro@mail.com");
        request.setCode("654321");

        assertEquals("otro@mail.com", request.getEmail());
        assertEquals("654321", request.getCode());
    }
}
