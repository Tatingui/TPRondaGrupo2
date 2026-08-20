package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

public class GsonSerializationTest {

    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    @Test
    public void testLoginRequestSeSerializaConEmailYPassword() {
        // Valida que el JSON que sale hacia el backend tiene los dos campos esperados
        String json = gson.toJson(new LoginRequest("test@mail.com", "1234"));

        assertTrue(json.contains("\"email\":\"test@mail.com\""));
        assertTrue(json.contains("\"password\":\"1234\""));
    }

    @Test
    public void testRegisterRequestSeSerializaConLosTresCampos() {
        // Valida que el registro manda nombre, email y password en el body
        String json = gson.toJson(new RegisterRequest("Juan Perez", "juan@mail.com", "secreto"));

        assertTrue(json.contains("\"nombre\":\"Juan Perez\""));
        assertTrue(json.contains("\"email\":\"juan@mail.com\""));
        assertTrue(json.contains("\"password\":\"secreto\""));
    }

    @Test
    public void testOtpRequestSeSerializaConEmailYCode() {
        // Valida que la verificacion de OTP manda el email junto con el codigo
        String json = gson.toJson(new OtpRequest("test@mail.com", "123456"));

        assertTrue(json.contains("\"email\":\"test@mail.com\""));
        assertTrue(json.contains("\"code\":\"123456\""));
    }

    @Test
    public void testOtpSendRequestSeSerializaSoloConEmail() {
        // Valida que el envio de OTP manda unicamente el email
        String json = gson.toJson(new OtpSendRequest("test@mail.com"));

        assertTrue(json.contains("\"email\":\"test@mail.com\""));
        assertFalse(json.contains("password"));
    }

    @Test
    public void testAuthResponseSeDeserializaConTodosLosCampos() {
        // Valida que una respuesta completa del backend llena los tres campos
        String json = "{\"token\":\"abc\",\"message\":\"OK\",\"success\":true}";

        AuthResponse response = gson.fromJson(json, AuthResponse.class);

        assertNotNull(response);
        assertEquals("abc", response.getToken());
        assertEquals("OK", response.getMessage());
        assertTrue(response.isSuccess());
    }

    @Test
    public void testAuthResponseConJsonParcialNoCrashea() {
        // Valida que si el backend omite campos, los faltantes quedan en su default
        String json = "{\"message\":\"Credenciales invalidas\"}";

        AuthResponse response = gson.fromJson(json, AuthResponse.class);

        assertNotNull(response);
        assertEquals("Credenciales invalidas", response.getMessage());
        assertNull(response.getToken());
        assertFalse(response.isSuccess());
    }

    @Test
    public void testAuthResponseConJsonVacioDevuelveObjetoConDefaults() {
        // Valida el caso borde de un JSON sin ningun campo conocido
        AuthResponse response = gson.fromJson("{}", AuthResponse.class);

        assertNotNull(response);
        assertNull(response.getToken());
        assertNull(response.getMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    public void testAuthResponseIgnoraCamposDesconocidos() {
        // Valida que un campo extra del backend no rompe la deserializacion
        String json = "{\"token\":\"abc\",\"success\":true,\"campoNuevo\":\"valor\"}";

        AuthResponse response = gson.fromJson(json, AuthResponse.class);

        assertNotNull(response);
        assertEquals("abc", response.getToken());
        assertTrue(response.isSuccess());
    }
}
