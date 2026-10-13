package com.example.tprondagrupo2.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.tprondagrupo2.db.entity.PublicacionEntity;
import com.example.tprondagrupo2.model.Publicacion;

import org.junit.Test;

public class PublicacionEntityTest {

    @Test
    public void testMapeoStateEnEntityModel() {
        Publicacion p = new Publicacion();
        p.setId("100");
        p.setTitle("Test Publicación");
        p.setState("PAUSED");

        PublicacionEntity entity = PublicacionEntity.fromModel(p);
        assertEquals("PAUSED", entity.getState());

        Publicacion mapeada = entity.toModel();
        assertEquals("100", mapeada.getId());
        assertEquals("PAUSED", mapeada.getState());
        assertFalse(mapeada.estaActiva());
    }

    @Test
    public void testMapeoStateActiveEnEntityModel() {
        Publicacion p = new Publicacion();
        p.setId("101");
        p.setTitle("Test Activa");
        p.setState("ACTIVE");

        PublicacionEntity entity = PublicacionEntity.fromModel(p);
        assertEquals("ACTIVE", entity.getState());

        Publicacion mapeada = entity.toModel();
        assertTrue(mapeada.estaActiva());
    }
}
