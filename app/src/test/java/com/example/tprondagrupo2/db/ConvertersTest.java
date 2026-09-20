package com.example.tprondagrupo2.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.tprondagrupo2.model.Vendedor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

/** Compatibilidad del JSON de vendedor persistido por Room al cambiar su ID a Long. */
public class ConvertersTest {

    @Test
    public void testLeeVendedorDeCacheAnteriorConIdString() {
        String jsonAnterior = "{\"id\":\"7\",\"nombre\":\"Ana\",\"reputacion\":4.5,"
                + "\"cantidadOpiniones\":2,\"ubicacion\":\"Palermo\"}";

        Vendedor vendedor = Converters.toVendedor(jsonAnterior);

        assertEquals(Long.valueOf(7L), vendedor.getId());
        assertEquals("Ana", vendedor.getNombre());
        assertEquals(4.5, vendedor.getReputacion(), 0.0001);
        assertEquals(2, vendedor.getCantidadOpiniones());
        assertEquals("Palermo", vendedor.getUbicacion());
    }

    @Test
    public void testGuardaIdNumericoYRecuperaVendedor() {
        Vendedor original = new Vendedor(99L, "Ana", 4.5, 3, 2, "2026-09-01", "Palermo");
        original.setCantidadCompras(4);

        String json = Converters.fromVendedor(original);
        JsonObject datos = new Gson().fromJson(json, JsonObject.class);
        Vendedor recuperado = Converters.toVendedor(json);

        assertTrue(datos.getAsJsonPrimitive("id").isNumber());
        assertEquals(Long.valueOf(99L), recuperado.getId());
        assertEquals(original.getNombre(), recuperado.getNombre());
        assertEquals(original.getReputacion(), recuperado.getReputacion(), 0.0001);
        assertEquals(original.getCantidadVentas(), recuperado.getCantidadVentas());
        assertEquals(original.getCantidadCompras(), recuperado.getCantidadCompras());
        assertEquals(original.getCantidadOpiniones(), recuperado.getCantidadOpiniones());
        assertEquals(original.getMiembroDesde(), recuperado.getMiembroDesde());
        assertEquals(original.getUbicacion(), recuperado.getUbicacion());
    }

    @Test
    public void testCacheSinIdConservaIdNulo() {
        Vendedor vendedor = Converters.toVendedor("{\"nombre\":\"Ana\"}");

        assertNull(vendedor.getId());
        assertEquals("Ana", vendedor.getNombre());
    }
}
