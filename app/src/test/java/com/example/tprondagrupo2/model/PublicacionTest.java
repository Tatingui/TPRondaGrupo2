package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

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
        assertNotNull(publicacion.getImageUrls());
        assertTrue(publicacion.getImageUrls().isEmpty());
    }

    @Test
    public void testCamposSonNullPorDefecto() {
        assertNull(publicacion.getId());
        assertNull(publicacion.getTitle());
        assertNull(publicacion.getDescription());
        assertNull(publicacion.getCategoryName());
        assertNull(publicacion.getStatus());
        assertNull(publicacion.getCreatedAt());
    }

    @Test
    public void testPrecioEsCeroPorDefecto() {
        assertEquals(0.0, publicacion.getPrice(), 0.0001);
    }

    @Test
    public void testConstructorCompletoAsignaTodosLosCampos() {
        List<String> fotos = Arrays.asList("foto1.jpg", "foto2.jpg");
        Publicacion p = new Publicacion("10", "Bicicleta", fotos, "Descripción larga",
                "Deportes", "Usado", 185000, "20/08/2026");

        assertEquals("10", p.getId());
        assertEquals("Bicicleta", p.getTitle());
        assertEquals(fotos, p.getImageUrls());
        assertEquals("Descripción larga", p.getDescription());
        assertEquals("Deportes", p.getCategoryName());
        assertEquals("Usado", p.getStatus());
        assertEquals(185000, p.getPrice(), 0.0001);
        assertEquals("20/08/2026", p.getCreatedAt());
    }

    @Test
    public void testConstructorCompletoConFotosNullUsaListaVacia() {
        Publicacion p = new Publicacion("1", "Titulo", null, "Desc",
                "Cat", "Nuevo", 100, "01/01/2026");

        assertNotNull(p.getImageUrls());
        assertTrue(p.getImageUrls().isEmpty());
    }

    @Test
    public void testSettersYGetters() {
        List<String> fotos = new ArrayList<>();
        fotos.add("una.jpg");

        publicacion.setId("99");
        publicacion.setTitle("Notebook");
        publicacion.setImageUrls(fotos);
        publicacion.setDescription("Casi nueva");
        publicacion.setCategoryName("Tecnología");
        publicacion.setStatus("Nuevo");
        publicacion.setPrice(450000.50);
        publicacion.setCreatedAt("15/07/2026");

        assertEquals("99", publicacion.getId());
        assertEquals("Notebook", publicacion.getTitle());
        assertEquals(fotos, publicacion.getImageUrls());
        assertEquals("Casi nueva", publicacion.getDescription());
        assertEquals("Tecnología", publicacion.getCategoryName());
        assertEquals("Nuevo", publicacion.getStatus());
        assertEquals(450000.50, publicacion.getPrice(), 0.0001);
        assertEquals("15/07/2026", publicacion.getCreatedAt());
    }

    @Test
    public void testGettersYSettersIngles() {
        publicacion.setId(123L);
        publicacion.setTitle("Pelota");
        publicacion.setImageUrls(Arrays.asList("p1.jpg", "p2.jpg"));
        publicacion.setDescription("De fútbol");
        publicacion.setCategoryName("Deportes");
        publicacion.setStatus("NUEVO");
        publicacion.setPrice(1500.0);
        publicacion.setCreatedAt("2026-08-01");
        publicacion.setSellerId(5L);
        publicacion.setSellerName("Carlos");
        publicacion.setLocation("Palermo");

        assertEquals("123", publicacion.getId());
        assertEquals(Long.valueOf(123L), publicacion.getIdLong());
        assertEquals("Pelota", publicacion.getTitle());
        assertEquals("p1.jpg", publicacion.getFirstImageUrl());
        assertEquals(2, publicacion.getImageUrls().size());
        assertEquals("De fútbol", publicacion.getDescription());
        assertEquals("Deportes", publicacion.getCategoryName());
        assertEquals("NUEVO", publicacion.getStatus());
        assertEquals(1500.0, publicacion.getPrice(), 0.0001);
        assertEquals("2026-08-01", publicacion.getCreatedAt());
        assertEquals(Long.valueOf(5L), publicacion.getSellerId());
        assertEquals("Carlos", publicacion.getSellerName());
        assertEquals("Palermo", publicacion.getLocation());
        assertNotNull(publicacion.getVendedor());
        assertEquals("Carlos", publicacion.getVendedor().getNombre());
    }

    @Test
    public void testDeserializacionGsonBackend() {
        Gson gson = new Gson();
        String json = "{"
                + "\"id\":42,"
                + "\"title\":\"Sillón\","
                + "\"description\":\"Cómodo\","
                + "\"price\":25000.0,"
                + "\"status\":\"COMO_NUEVO\","
                + "\"location\":\"Belgrano\","
                + "\"categoryName\":\"Hogar\","
                + "\"imageUrls\":[\"sillon1.jpg\"],"
                + "\"createdAt\":\"2026-08-20T10:00:00\","
                + "\"sellerId\":7,"
                + "\"sellerName\":\"María\","
                + "\"isFavorite\":true"
                + "}";

        Publicacion p = gson.fromJson(json, Publicacion.class);

        assertNotNull(p);
        assertEquals("42", p.getId());
        assertEquals(Long.valueOf(42L), p.getIdLong());
        assertEquals("Sillón", p.getTitle());
        assertEquals("sillon1.jpg", p.getFirstImageUrl());
        assertEquals(25000.0, p.getPrice(), 0.0001);
        assertEquals("COMO_NUEVO", p.getStatus());
        assertEquals("Hogar", p.getCategoryName());
        assertEquals("María", p.getSellerName());
        assertEquals("Belgrano", p.getLocation());
        assertTrue(p.isFavorite());
        assertNotNull(p.getVendedor());
        assertEquals("María", p.getVendedor().getNombre());
    }

    @Test
    public void testSetFotosConNullDejaListaVacia() {
        publicacion.setImageUrls(null);

        assertNotNull(publicacion.getImageUrls());
        assertTrue(publicacion.getImageUrls().isEmpty());
    }

    @Test
    public void testGetCantidadFotos() {
        assertEquals(0, publicacion.getCantidadFotos());

        publicacion.setImageUrls(Arrays.asList("a.jpg", "b.jpg", "c.jpg"));

        assertEquals(3, publicacion.getCantidadFotos());
    }

    @Test
    public void testVendedorEsNullPorDefecto() {
        assertNull(publicacion.getVendedor());
    }

    @Test
    public void testConstructorConVendedorLoAsigna() {
        Vendedor vendedor = new Vendedor("7", "Juan Pérez", 4.5, 342, 128, "Marzo 2023", "Palermo");
        Publicacion p = new Publicacion("10", "Bicicleta", Arrays.asList("f.jpg"), "Desc",
                "Deportes", "Usado", 185000, "20/08/2026", vendedor);

        assertNotNull(p.getVendedor());
        assertEquals("Juan Pérez", p.getVendedor().getNombre());
        assertEquals("Juan Pérez", p.getSellerName());
        assertEquals("Palermo", p.getLocation());
    }

    @Test
    public void testConstructorSinVendedorDejaVendedorNull() {
        Publicacion p = new Publicacion("10", "Bicicleta", Arrays.asList("f.jpg"), "Desc",
                "Deportes", "Usado", 185000, "20/08/2026");

        assertNull(p.getVendedor());
    }

    @Test
    public void testSetVendedor() {
        Vendedor vendedor = new Vendedor();
        vendedor.setNombre("Ana");
        publicacion.setVendedor(vendedor);

        assertNotNull(publicacion.getVendedor());
        assertEquals("Ana", publicacion.getVendedor().getNombre());
    }
}
