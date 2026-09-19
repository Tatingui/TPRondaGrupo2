package com.ronda.backend.service;

import com.ronda.backend.dto.SellerDTO;
import com.ronda.backend.dto.UserProfileResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Hasta el punto 9 (calificaciones) la reputacion queda en 0,
 * aunque el usuario tenga publicaciones vendidas.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserReputationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private Publication vendida;

    @BeforeEach
    public void setup() {
        Category category = new Category("CategoriaReputacion");
        entityManager.persist(category);

        user = new User("Usuario Reputacion", "reputacion@test.com", "pass");
        entityManager.persist(user);

        vendida = new Publication();
        vendida.setTitle("Mesa");
        vendida.setDescription("Mesa de madera");
        vendida.setPrice(100.0);
        vendida.setStatus(PublicationStatus.USED);
        vendida.setState(PublicationState.SOLD);
        vendida.setLocation("Palermo");
        vendida.setCategory(category);
        vendida.setSeller(user);
        entityManager.persist(vendida);
        entityManager.flush();
    }

    @Test
    public void elPerfilPropioTieneLaReputacionEnCero() {
        UserProfileResponse perfil = userService.getProfile(user.getEmail());

        assertEquals(0.0, perfil.getReputacion());
        assertEquals(0, perfil.getCantidadOpiniones());
        assertEquals(0, perfil.getCantidadVentas());
        assertEquals(0, perfil.getCantidadCompras());
    }

    @Test
    public void elVendedorDelDetalleTieneLaReputacionEnCero() {
        SellerDTO vendedor = publicationService.getById(vendida.getId(), null).getVendedor();

        assertEquals(0.0, vendedor.getReputacion());
        assertEquals(0, vendedor.getCantidadOpiniones());
        assertEquals(0, vendedor.getCantidadVentas());
        assertEquals(0, vendedor.getCantidadCompras());
    }
}
