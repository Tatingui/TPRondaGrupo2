package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class OtpSendRequestTest {

    private static final String EMAIL = "test@mail.com";

    private OtpSendRequest request;

    @Before
    public void setUp() {
        request = new OtpSendRequest(EMAIL);
    }

    @Test
    public void testConstructorAsignaEmail() {
        // Valida que el constructor de un solo parametro guarda el email
        assertEquals(EMAIL, request.getEmail());
    }

    @Test
    public void testGetterDevuelveElEmailAsignado() {
        // Valida que el getter devuelve el email sin modificarlo
        assertEquals(EMAIL, request.getEmail());
    }

    @Test
    public void testSetterActualizaElEmail() {
        // Valida que el setter reemplaza el email puesto por el constructor
        request.setEmail("otro@mail.com");

        assertEquals("otro@mail.com", request.getEmail());
    }
}
