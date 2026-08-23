package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PublicacionTest {

    private Publicacion publicacion;

    @Before
    public void setUp() {
        publicacion = new Publicacion();
    }

    @Test
    public void testConstructorVacioNoLanzaExcepcion() {
        // Valida que el constructor sin argumentos que necesita Gson crea la instancia
        assertNotNull(new Publicacion());
    }

    @Test
    public void testConstructorVacioInicializaListaDeFotos() {
        // La lista de fotos nunca debe ser null para evitar NullPointer al recorrerla
        assertNotNull(publicacion.getFotos());
        assertTrue(publicacion.getFotos().isEmpty());
    }

    @Test
    public void testCamposSonNullPorDefecto() {
        // Valida que los String arrancan en null antes de que Gson los complete
        assertNull(publicacion.getId());
        assertNull(publicacion.getTitulo());
        assertNull(publicacion.getDescripcion());
        assertNull(publicacion.getCategoria());
        assertNull(publicacion.getEstado());
        assertNull(publicacion.getFechaPublicacion());
    }

    @Test
    public void testPrecioEsCeroPorDefecto() {
        // Valida que el double arranca en 0, el valor por defecto de Java
        assertEquals(0.0, publicacion.getPrecio(), 0.0001);
    }

    @Test
    public void testConstructorCompletoAsignaTodosLosCampos() {
        List<String> fotos = Arrays.asList("foto1.jpg", "foto2.jpg");
        Publicacion p = new Publicacion("10", "Bicicleta", fotos, "Descripción larga",
                "Deportes", "Usado", 185000, "20/08/2026");

        assertEquals("10", p.getId());
        assertEquals("Bicicleta", p.getTitulo());
        assertEquals(fotos, p.getFotos());
        assertEquals("Descripción larga", p.getDescripcion());
        assertEquals("Deportes", p.getCategoria());
        assertEquals("Usado", p.getEstado());
        assertEquals(185000, p.getPrecio(), 0.0001);
        assertEquals("20/08/2026", p.getFechaPublicacion());
    }

    @Test
    public void testConstructorCompletoConFotosNullUsaListaVacia() {
        // Si llega null en fotos, el constructor debe dejar una lista vacía usable
        Publicacion p = new Publicacion("1", "Titulo", null, "Desc",
                "Cat", "Nuevo", 100, "01/01/2026");

        assertNotNull(p.getFotos());
        assertTrue(p.getFotos().isEmpty());
    }

    @Test
    public void testSettersYGetters() {
        List<String> fotos = new ArrayList<>();
        fotos.add("una.jpg");

        publicacion.setId("99");
        publicacion.setTitulo("Notebook");
        publicacion.setFotos(fotos);
        publicacion.setDescripcion("Casi nueva");
        publicacion.setCategoria("Tecnología");
        publicacion.setEstado("Nuevo");
        publicacion.setPrecio(450000.50);
        publicacion.setFechaPublicacion("15/07/2026");

        assertEquals("99", publicacion.getId());
        assertEquals("Notebook", publicacion.getTitulo());
        assertEquals(fotos, publicacion.getFotos());
        assertEquals("Casi nueva", publicacion.getDescripcion());
        assertEquals("Tecnología", publicacion.getCategoria());
        assertEquals("Nuevo", publicacion.getEstado());
        assertEquals(450000.50, publicacion.getPrecio(), 0.0001);
        assertEquals("15/07/2026", publicacion.getFechaPublicacion());
    }

    @Test
    public void testSetFotosConNullDejaListaVacia() {
        // El setter también debe blindar contra null
        publicacion.setFotos(null);

        assertNotNull(publicacion.getFotos());
        assertTrue(publicacion.getFotos().isEmpty());
    }

    @Test
    public void testGetCantidadFotos() {
        // Valida el conteo usado por el indicador de la galería
        assertEquals(0, publicacion.getCantidadFotos());

        publicacion.setFotos(Arrays.asList("a.jpg", "b.jpg", "c.jpg"));

        assertEquals(3, publicacion.getCantidadFotos());
    }
}
