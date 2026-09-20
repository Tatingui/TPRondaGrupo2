package com.example.tprondagrupo2.ui.detalle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MapaNavigatorTest {

    @Test
    public void abreGoogleMapsSinIntentarOtraAppSiFunciona() {
        FakeLanzador lanzador = new FakeLanzador(true);

        assertTrue(new MapaNavigator(lanzador).abrir("-34.588,-58.411"));

        assertEquals(1, lanzador.uris.size());
        assertEquals("https://www.google.com/maps/dir/?api=1&destination=-34.588%2C-58.411", lanzador.uris.get(0));
        assertEquals("com.google.android.apps.maps", lanzador.paquetes.get(0));
    }

    @Test
    public void usaGeoSiNoEstaGoogleMaps() {
        FakeLanzador lanzador = new FakeLanzador(false, true);

        assertTrue(new MapaNavigator(lanzador).abrir("Calle 1"));

        assertEquals(2, lanzador.uris.size());
        assertEquals("geo:0,0?q=Calle%201", lanzador.uris.get(1));
        assertNull(lanzador.paquetes.get(1));
    }

    @Test
    public void informaFalloSiNingunaAppAbreElDestino() {
        FakeLanzador lanzador = new FakeLanzador(false, false);

        assertFalse(new MapaNavigator(lanzador).abrir("Calle 1"));
        assertEquals(2, lanzador.uris.size());
    }

    @Test
    public void codificaDireccionSinInyectarParametros() {
        FakeLanzador lanzador = new FakeLanzador(false, true);
        String esperado = "Av.%20C%C3%B3rdoba%201%20%26%20Esqui%C3%BA%20%2B%202%2F3%20%23A%3F";

        new MapaNavigator(lanzador).abrir("  Av. Córdoba 1 & Esquiú + 2/3 #A?  ");

        assertEquals("https://www.google.com/maps/dir/?api=1&destination=" + esperado, lanzador.uris.get(0));
        assertEquals("geo:0,0?q=" + esperado, lanzador.uris.get(1));
    }

    @Test
    public void noIniciaNingunaAppSinDestino() {
        FakeLanzador lanzador = new FakeLanzador();
        MapaNavigator navigator = new MapaNavigator(lanzador);

        assertFalse(navigator.abrir(null));
        assertFalse(navigator.abrir("   "));
        assertTrue(lanzador.uris.isEmpty());
    }

    private static final class FakeLanzador implements MapaNavigator.Lanzador {
        final List<String> uris = new ArrayList<>();
        final List<String> paquetes = new ArrayList<>();
        final boolean[] resultados;

        FakeLanzador(boolean... resultados) {
            this.resultados = resultados;
        }

        @Override
        public boolean abrir(String uri, String paquete) {
            uris.add(uri);
            paquetes.add(paquete);
            return resultados[uris.size() - 1];
        }
    }
}
