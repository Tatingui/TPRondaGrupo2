package com.ronda.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronda.backend.dto.PublicProfileDTO;
import com.ronda.backend.exception.ResourceNotFoundException;
import com.ronda.backend.model.Category;
import com.ronda.backend.model.Publication;
import com.ronda.backend.model.PublicationState;
import com.ronda.backend.model.PublicationStatus;
import com.ronda.backend.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class PerfilPublicoTest {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private User user;

    @BeforeEach
    public void setup() {
        Category category = new Category("CategoriaPerfil");
        entityManager.persist(category);

        user = new User("Ana Perfil", "ana_perfil@test.com", "pass");
        user.setTelefono("1122334455");
        user.setZona("Palermo");
        entityManager.persist(user);

        crearPublicacion("Activa 1", PublicationState.ACTIVE, category);
        crearPublicacion("Activa 2", PublicationState.ACTIVE, category);
        crearPublicacion("Pausada", PublicationState.PAUSED, category);
        crearPublicacion("Vendida", PublicationState.SOLD, category);
        entityManager.flush();
    }

    private void crearPublicacion(String titulo, PublicationState state, Category category) {
        Publication p = new Publication();
        p.setTitle(titulo);
        p.setDescription("Descripcion");
        p.setPrice(100.0);
        p.setStatus(PublicationStatus.USED);
        p.setState(state);
        p.setLocation("Palermo");
        p.setCategory(category);
        p.setSeller(user);
        entityManager.persist(p);
    }

    @Test
    public void traeSoloLasPublicacionesActivas() {
        PublicProfileDTO perfil = publicationService.getPerfilPublico(user.getId(), null);

        assertEquals(2, perfil.getPublicacionesActivas().size());
        assertTrue(perfil.getPublicacionesActivas().stream()
                .allMatch(p -> p.getState() == PublicationState.ACTIVE));
    }

    @Test
    public void traeReputacionAntiguedadYZona() {
        PublicProfileDTO perfil = publicationService.getPerfilPublico(user.getId(), null);

        assertEquals("Ana Perfil", perfil.getNombre());
        assertEquals("Palermo", perfil.getUbicacion());
        assertNotNull(perfil.getMiembroDesde());
        assertEquals(0.0, perfil.getReputacion());
        assertEquals(0, perfil.getCantidadVentas());
        assertEquals(0, perfil.getCantidadCompras());
    }

    @Test
    public void noExponeEmailNiTelefono() throws Exception {
        PublicProfileDTO perfil = publicationService.getPerfilPublico(user.getId(), null);

        String json = objectMapper.writeValueAsString(perfil);
        assertFalse(json.contains("ana_perfil@test.com"));
        assertFalse(json.contains("1122334455"));
    }

    @Test
    public void usuarioInexistenteDa404() {
        assertThrows(ResourceNotFoundException.class,
                () -> publicationService.getPerfilPublico(999999L, null));
    }
}
