package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class RegisterRequestTest {

    private static final String NOMBRE = "Juan Perez";
    private static final String EMAIL = "juan@mail.com";
    private static final String PASSWORD = "secreto";

    private RegisterRequest request;

    @Before
    public void setUp() {
        request = new RegisterRequest(NOMBRE, EMAIL, PASSWORD);
    }

    @Test
    public void testConstructorAsignaNombreEmailYPassword() {
        // Valida que el constructor de 3 parametros asigna los campos en el orden correcto
        assertEquals(NOMBRE, request.getNombre());
        assertEquals(EMAIL, request.getEmail());
        assertEquals(PASSWORD, request.getPassword());
    }

    @Test
    public void testGettersDevuelvenLosValoresAsignados() {
        // Valida que los tres getters devuelven lo que se guardo
        assertEquals(NOMBRE, request.getNombre());
        assertEquals(EMAIL, request.getEmail());
        assertEquals(PASSWORD, request.getPassword());
    }

    @Test
    public void testSettersActualizanLosValores() {
        // Valida que los tres setters sobreescriben los valores iniciales
        request.setNombre("Ana Gomez");
        request.setEmail("ana@mail.com");
        request.setPassword("nuevo");

        assertEquals("Ana Gomez", request.getNombre());
        assertEquals("ana@mail.com", request.getEmail());
        assertEquals("nuevo", request.getPassword());
    }
}
