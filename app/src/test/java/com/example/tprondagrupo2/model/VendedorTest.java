package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.tprondagrupo2.ui.detalle.VendedorViewBinder;

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
        assertNotNull(new Vendedor());
    }

    @Test
    public void testCamposPorDefecto() {
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
        Vendedor v = new Vendedor(7L, "Juan Pérez", 4.5, 342, 128, "Marzo 2023", "Palermo");

        assertEquals(Long.valueOf(7L), v.getId());
        assertEquals("Juan Pérez", v.getNombre());
        assertEquals(4.5, v.getReputacion(), 0.0001);
        assertEquals(342, v.getCantidadVentas());
        assertEquals(128, v.getCantidadOpiniones());
        assertEquals("Marzo 2023", v.getMiembroDesde());
        assertEquals("Palermo", v.getUbicacion());
    }

    @Test
    public void testSettersYGetters() {
        vendedor.setId(99L);
        vendedor.setNombre("Ana Gómez");
        vendedor.setReputacion(3.8);
        vendedor.setCantidadVentas(50);
        vendedor.setCantidadOpiniones(20);
        vendedor.setMiembroDesde("Enero 2024");
        vendedor.setUbicacion("Belgrano");

        assertEquals(Long.valueOf(99L), vendedor.getId());
        assertEquals("Ana Gómez", vendedor.getNombre());
        assertEquals(3.8, vendedor.getReputacion(), 0.0001);
        assertEquals(50, vendedor.getCantidadVentas());
        assertEquals(20, vendedor.getCantidadOpiniones());
        assertEquals("Enero 2024", vendedor.getMiembroDesde());
        assertEquals("Belgrano", vendedor.getUbicacion());
    }

    @Test
    public void testObtenerInicialDevuelvePrimeraLetraEnMayuscula() {
        assertEquals("J", VendedorViewBinder.obtenerInicial("juan"));
    }

    @Test
    public void testObtenerInicialConNombreConEspacios() {
        assertEquals("A", VendedorViewBinder.obtenerInicial("  ana maría  "));
    }

    @Test
    public void testObtenerInicialConNombreNullDevuelveInterrogacion() {
        assertEquals("?", VendedorViewBinder.obtenerInicial(null));
    }

    @Test
    public void testObtenerInicialConNombreVacioDevuelveInterrogacion() {
        assertEquals("?", VendedorViewBinder.obtenerInicial("   "));
    }

    @Test
    public void testNivelSinCalificacionesCuandoNoHayOpiniones() {
        vendedor.setReputacion(5.0);
        vendedor.setCantidadOpiniones(0);
        assertEquals(VendedorViewBinder.NivelReputacion.SIN_CALIFICACIONES, VendedorViewBinder.calcularNivel(vendedor));
    }

    @Test
    public void testNivelExcelente() {
        vendedor.setReputacion(4.5);
        vendedor.setCantidadOpiniones(10);
        assertEquals(VendedorViewBinder.NivelReputacion.EXCELENTE, VendedorViewBinder.calcularNivel(vendedor));
    }

    @Test
    public void testNivelBueno() {
        vendedor.setReputacion(3.5);
        vendedor.setCantidadOpiniones(10);
        assertEquals(VendedorViewBinder.NivelReputacion.BUENO, VendedorViewBinder.calcularNivel(vendedor));
    }

    @Test
    public void testNivelRegular() {
        vendedor.setReputacion(2.5);
        vendedor.setCantidadOpiniones(10);
        assertEquals(VendedorViewBinder.NivelReputacion.REGULAR, VendedorViewBinder.calcularNivel(vendedor));
    }

    @Test
    public void testNivelMalo() {
        vendedor.setReputacion(1.5);
        vendedor.setCantidadOpiniones(10);
        assertEquals(VendedorViewBinder.NivelReputacion.MALO, VendedorViewBinder.calcularNivel(vendedor));
    }

    @Test
    public void testNivelExpoConEtiquetaYColorNoNulos() {
        for (VendedorViewBinder.NivelReputacion nivel : VendedorViewBinder.NivelReputacion.values()) {
            assertNotNull(nivel.getEtiqueta());
            assertEquals(0xFF000000, nivel.getColor() & 0xFF000000);
        }
    }

    @Test
    public void testComprasEmpiezaEnCeroYSePuedeSetear() {
        Vendedor v = new Vendedor(1L, "Ana", 0, 0, 0, "Septiembre 2026", "Palermo");
        assertEquals(0, v.getCantidadCompras());

        v.setCantidadCompras(3);
        assertEquals(3, v.getCantidadCompras());
    }

    @Test
    public void testSinCalificacionesMuestraEseNivel() {
        Vendedor v = new Vendedor(1L, "Ana", 0, 0, 0, "Septiembre 2026", "Palermo");
        assertEquals(VendedorViewBinder.NivelReputacion.SIN_CALIFICACIONES, VendedorViewBinder.calcularNivel(v));
    }
}
