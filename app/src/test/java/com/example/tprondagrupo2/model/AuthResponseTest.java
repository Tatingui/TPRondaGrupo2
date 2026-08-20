package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class AuthResponseTest {

    private AuthResponse response;

    @Before
    public void setUp() {
        response = new AuthResponse();
    }

    @Test
    public void testConstructorVacioNoLanzaExcepcion() {
        // Valida que el constructor sin argumentos que necesita Gson crea la instancia
        assertNotNull(new AuthResponse());
    }

    @Test
    public void testIsSuccessEsFalsePorDefecto() {
        // Valida que el boolean arranca en false, el valor por defecto de Java
        assertFalse(response.isSuccess());
    }

    @Test
    public void testTokenYMessageSonNullPorDefecto() {
        // Valida que los String arrancan en null antes de que Gson los complete
        assertNull(response.getToken());
        assertNull(response.getMessage());
    }

    @Test
    public void testSettersYGettersDeToken() {
        // Valida el ida y vuelta del campo token
        response.setToken("abc123");

        assertEquals("abc123", response.getToken());
    }

    @Test
    public void testSettersYGettersDeMessage() {
        // Valida el ida y vuelta del campo message
        response.setMessage("Login exitoso");

        assertEquals("Login exitoso", response.getMessage());
    }

    @Test
    public void testSettersYGettersDeSuccess() {
        // Valida que success se puede pasar a true y volver a false
        response.setSuccess(true);
        assertTrue(response.isSuccess());

        response.setSuccess(false);
        assertFalse(response.isSuccess());
    }
}
