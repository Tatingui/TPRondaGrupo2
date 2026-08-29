package com.ronda.backend.service;

import com.ronda.backend.dto.PublicationDTO;
import com.ronda.backend.model.Category;
import com.ronda.backend.model.Publication;
import com.ronda.backend.model.PublicationStatus;
import com.ronda.backend.model.User;
import com.ronda.backend.repository.PublicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@org.springframework.test.context.ActiveProfiles("test")
public class PublicationServiceTest {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Category catDeportes;
    private User user;

    @BeforeEach
    public void setup() {
        catDeportes = new Category("CategoriaTest");
        entityManager.persist(catDeportes);

        user = new User("Juan Test", "juan_test@test.com", "pass");
        entityManager.persist(user);

        Publication p1 = new Publication();
        p1.setTitle("Bicicleta");
        p1.setDescription("Rodado 29");
        p1.setPrice(100.0);
        p1.setStatus(PublicationStatus.NUEVO);
        p1.setLocation("Palermo");
        p1.setCategory(catDeportes);
        p1.setSeller(user);
        entityManager.persist(p1);

        Publication p2 = new Publication();
        p2.setTitle("Pelota");
        p2.setDescription("Fútbol");
        p2.setPrice(50.0);
        p2.setStatus(PublicationStatus.USADO);
        p2.setLocation("Almagro");
        p2.setCategory(catDeportes);
        p2.setSeller(user);
        entityManager.persist(p2);

        entityManager.flush();
    }

    @Test
    public void testFilterBySearch() {
        Page<PublicationDTO> result = publicationService.findAll("Bici", null, null, null, null, null, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("Bicicleta", result.getContent().get(0).getTitle());
    }

    @Test
    public void testFilterByPriceRange() {
        Page<PublicationDTO> result = publicationService.findAll(null, null, 40.0, 60.0, null, null, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("Pelota", result.getContent().get(0).getTitle());
    }

    @Test
    public void testFilterByStatus() {
        Page<PublicationDTO> result = publicationService.findAll(null, null, null, null, PublicationStatus.USADO, null, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("Pelota", result.getContent().get(0).getTitle());
    }
}
