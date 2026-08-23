package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class VendedorTest {

    private Vendedor vendedor;

    @Before
    public void setUp() {
        vendedor = new Vendedor();
    }

    @Test
    public void testConstructorVacioNoLanzaExcepcion() {
        // Constructor sin argumentos requerido por Gson
        assertNotNull(new Vendedor());
    }

    @Test
    public void testCamposPorDefecto() {
        // Los String arrancan en null y los numéricos en 0
        assertNull(vendedor.getId());
        assertNull(vendedor.getNombre());
        assertNull(vendedor.getMiembroDesde());
        assertNull(vendedor.getUbicacion());
        assertEquals(0.0, vendedor.getReputacion(), 0.0001);
        assertEquals(0, vendedor.getCantidadVentas());
        assertEquals(0, vendedor.getCantidadOpiniones());
    }

    @Test
    public void testConstructorCompletoAsignaTodosLosCampos() {
        Vendedor v = new Vendedor("7", "Juan Pérez", 4.5, 342, 128, "Marzo 2023", "Palermo");

        assertEquals("7", v.getId());
        assertEquals("Juan Pérez", v.getNombre());
        assertEquals(4.5, v.getReputacion(), 0.0001);
        assertEquals(342, v.getCantidadVentas());
        assertEquals(128, v.getCantidadOpiniones());
        assertEquals("Marzo 2023", v.getMiembroDesde());
        assertEquals("Palermo", v.getUbicacion());
    }

    @Test
    public void testSettersYGetters() {
        vendedor.setId("99");
        vendedor.setNombre("Ana Gómez");
        vendedor.setReputacion(3.8);
        vendedor.setCantidadVentas(50);
        vendedor.setCantidadOpiniones(20);
        vendedor.setMiembroDesde("Enero 2024");
        vendedor.setUbicacion("Belgrano");

        assertEquals("99", vendedor.getId());
        assertEquals("Ana Gómez", vendedor.getNombre());
        assertEquals(3.8, vendedor.getReputacion(), 0.0001);
        assertEquals(50, vendedor.getCantidadVentas());
        assertEquals(20, vendedor.getCantidadOpiniones());
        assertEquals("Enero 2024", vendedor.getMiembroDesde());
        assertEquals("Belgrano", vendedor.getUbicacion());
    }

    @Test
    public void testGetInicialDevuelvePrimeraLetraEnMayuscula() {
        vendedor.setNombre("juan");
        assertEquals("J", vendedor.getInicial());
    }

    @Test
    public void testGetInicialConNombreConEspacios() {
        vendedor.setNombre("  ana maría  ");
        assertEquals("A", vendedor.getInicial());
    }

    @Test
    public void testGetInicialConNombreNullDevuelveInterrogacion() {
        vendedor.setNombre(null);
        assertEquals("?", vendedor.getInicial());
    }

    @Test
    public void testGetInicialConNombreVacioDevuelveInterrogacion() {
        vendedor.setNombre("   ");
        assertEquals("?", vendedor.getInicial());
    }

    @Test
    public void testNivelSinCalificacionesCuandoNoHayOpiniones() {
        // Aunque tenga puntaje, sin opiniones no se muestra reputación real
        vendedor.setReputacion(5.0);
        vendedor.setCantidadOpiniones(0);
        assertEquals(Vendedor.NivelReputacion.SIN_CALIFICACIONES, vendedor.getNivel());
    }

    @Test
    public void testNivelExcelente() {
        vendedor.setReputacion(4.5);
        vendedor.setCantidadOpiniones(10);
        assertEquals(Vendedor.NivelReputacion.EXCELENTE, vendedor.getNivel());
    }

    @Test
    public void testNivelBueno() {
        vendedor.setReputacion(3.5);
        vendedor.setCantidadOpiniones(10);
        assertEquals(Vendedor.NivelReputacion.BUENO, vendedor.getNivel());
    }

    @Test
    public void testNivelRegular() {
        vendedor.setReputacion(2.5);
        vendedor.setCantidadOpiniones(10);
        assertEquals(Vendedor.NivelReputacion.REGULAR, vendedor.getNivel());
    }

    @Test
    public void testNivelMalo() {
        vendedor.setReputacion(1.5);
        vendedor.setCantidadOpiniones(10);
        assertEquals(Vendedor.NivelReputacion.MALO, vendedor.getNivel());
    }

    @Test
    public void testNivelExpoConEtiquetaYColorNoNulos() {
        // Cada nivel debe tener etiqueta y color definidos para pintar la UI
        for (Vendedor.NivelReputacion nivel : Vendedor.NivelReputacion.values()) {
            assertNotNull(nivel.getEtiqueta());
            // El color es un int ARGB; validamos que tenga canal alfa (no transparente)
            assertEquals(0xFF000000, nivel.getColor() & 0xFF000000);
        }
    }
}
