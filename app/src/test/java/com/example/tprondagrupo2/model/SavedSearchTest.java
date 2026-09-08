package com.example.tprondagrupo2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SavedSearchTest {

    @Test
    public void testGetDisplayTitleWithQuery() {
        SavedSearch search = new SavedSearch();
        search.setQuery("Zapatillas");
        assertEquals("Zapatillas", search.getDisplayTitle());
    }

    @Test
    public void testGetDisplayTitleWithCategoryOnly() {
        SavedSearch search = new SavedSearch();
        search.setCategoryName("Deportes");
        assertEquals("Deportes", search.getDisplayTitle());
    }

    @Test
    public void testGetDisplayTitleDefault() {
        SavedSearch search = new SavedSearch();
        assertEquals("Búsqueda guardada", search.getDisplayTitle());
    }

    @Test
    public void testHasAnyFilterOrQuery() {
        SavedSearch emptySearch = new SavedSearch();
        assertFalse(emptySearch.hasAnyFilterOrQuery());

        SavedSearch activeSearch = new SavedSearch();
        activeSearch.setQuery("Celular");
        assertTrue(activeSearch.hasAnyFilterOrQuery());
    }

    @Test
    public void testGetSummaryFilters() {
        SavedSearch search = new SavedSearch();
        search.setQuery("Bicicleta");
        search.setCategoryName("Deportes");
        search.setLocationName("Belgrano");
        search.setMinPrice(1000.0);
        search.setMaxPrice(5000.0);

        String summary = search.getSummaryFilters();
        assertTrue(summary.contains("Categoría: Deportes"));
        assertTrue(summary.contains("Zona: Belgrano"));
        assertTrue(summary.contains("$1000 - $5000"));
    }
}
