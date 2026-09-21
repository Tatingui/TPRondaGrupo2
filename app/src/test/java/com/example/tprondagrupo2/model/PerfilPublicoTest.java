package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.tprondagrupo2.ui.detalle.VendedorViewBinder;
import com.google.gson.Gson;

import org.junit.Test;

public class PerfilPublicoTest {

    @Test
    public void testGsonLeeElPerfilPublicoDelBackend() {
        // Así lo manda GET /usuarios/{id}/publico (el id viene como número)
        String json = "{\"id\":2,\"nombre\":\"Maria Hogar\",\"reputacion\":0.0,"
                + "\"cantidadVentas\":0,\"cantidadCompras\":0,\"cantidadOpiniones\":0,"
                + "\"miembroDesde\":\"Septiembre 2026\",\"ubicacion\":\"Almagro\","
                + "\"publicacionesActivas\":[{\"id\":6,\"title\":\"Auriculares\",\"price\":120000.0}]}";

        PerfilPublico perfil = new Gson().fromJson(json, PerfilPublico.class);

        assertEquals(Long.valueOf(2L), perfil.getId());
        assertEquals("Maria Hogar", perfil.getNombre());
        assertEquals("Septiembre 2026", perfil.getMiembroDesde());
        assertEquals(VendedorViewBinder.NivelReputacion.SIN_CALIFICACIONES, VendedorViewBinder.calcularNivel(perfil));
        assertEquals(1, perfil.getPublicacionesActivas().size());
        assertEquals("Auriculares", perfil.getPublicacionesActivas().get(0).getTitle());
    }

    @Test
    public void testSinPublicacionesDevuelveListaVacia() {
        PerfilPublico perfil = new Gson().fromJson("{\"id\":3,\"nombre\":\"Tech\"}", PerfilPublico.class);

        assertNotNull(perfil.getPublicacionesActivas());
        assertTrue(perfil.getPublicacionesActivas().isEmpty());
    }
}
