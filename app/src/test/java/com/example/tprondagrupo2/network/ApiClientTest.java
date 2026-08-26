package com.example.tprondagrupo2.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import retrofit2.Retrofit;

public class ApiClientTest {

    private static final String BASE_URL_ESPERADA = "http://10.0.2.2:8081/api/";

    @Test
    public void testGetClientNoDevuelveNull() {
        // Valida que la construccion del Retrofit no falla ni devuelve null
        assertNotNull(ApiClient.getClient());
    }

    @Test
    public void testGetClientDevuelveSiempreLaMismaInstancia() {
        // Valida el comportamiento singleton: dos llamadas dan el mismo objeto
        Retrofit primera = ApiClient.getClient();
        Retrofit segunda = ApiClient.getClient();

        assertSame(primera, segunda);
    }

    @Test
    public void testGetAuthServiceNoDevuelveNull() {
        // Valida que Retrofit puede generar la implementacion de la interfaz
        assertNotNull(ApiClient.getAuthService());
    }

    @Test
    public void testBaseUrlEsLaEsperada() {
        // Valida que apunta al localhost del host visto desde el emulador
        assertEquals(BASE_URL_ESPERADA, ApiClient.getClient().baseUrl().toString());
    }

    @Test
    public void testBaseUrlTerminaConBarra() {
        // Valida el requisito de Retrofit de que la base URL termine en /
        assertEquals('/', BASE_URL_ESPERADA.charAt(BASE_URL_ESPERADA.length() - 1));
    }
}
