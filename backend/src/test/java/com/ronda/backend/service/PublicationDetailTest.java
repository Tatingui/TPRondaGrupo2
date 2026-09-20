package com.ronda.backend.service;

import com.ronda.backend.dto.OfferCreateRequest;
import com.ronda.backend.dto.OfferDTO;
import com.ronda.backend.dto.PublicationDetailDTO;
import com.ronda.backend.dto.QuestionDTO;
import com.ronda.backend.exception.BadRequestException;
import com.ronda.backend.exception.ForbiddenException;
import com.ronda.backend.model.Category;
import com.ronda.backend.model.Offer;
import com.ronda.backend.model.OfferStatus;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class PublicationDetailTest {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private PublicationInteractionService interactionService;

    @Autowired
    private EntityManager entityManager;

    private User seller;
    private User buyer;
    private Publication publication;

    @BeforeEach
    public void setup() {
        Category category = new Category("CategoriaDetalle");
        entityManager.persist(category);

        seller = new User("Vendedor Test", "vendedor_detalle@test.com", "pass");
        entityManager.persist(seller);
        buyer = new User("Comprador Test", "comprador_detalle@test.com", "pass");
        entityManager.persist(buyer);

        publication = new Publication();
        publication.setTitle("Bicicleta");
        publication.setDescription("Rodado 29");
        publication.setPrice(100.0);
        publication.setStatus(PublicationStatus.USED);
        publication.setLocation("Palermo");
        publication.setAddress("Av. Santa Fe 3253");
        publication.setLatitude(-34.588);
        publication.setLongitude(-58.411);
        publication.setCategory(category);
        publication.setSeller(seller);
        entityManager.persist(publication);
        entityManager.flush();
    }

    @Test
    public void elVendedorVeLaDireccionYEsOwner() {
        PublicationDetailDTO dto = publicationService.getById(publication.getId(), seller.getEmail());

        assertTrue(dto.isOwner());
        assertTrue(dto.isAddressVisible());
        assertEquals("Av. Santa Fe 3253", dto.getAddress());
        assertEquals("Vendedor Test", dto.getVendedor().getNombre());
    }

    @Test
    public void unInteresadoNoVeLaDireccion() {
        PublicationDetailDTO dto = publicationService.getById(publication.getId(), buyer.getEmail());

        assertFalse(dto.isOwner());
        assertFalse(dto.isAddressVisible());
        assertNull(dto.getAddress());
        assertNull(dto.getLatitude());
    }

    @Test
    public void sinSesionNoVeLaDireccion() {
        PublicationDetailDTO dto = publicationService.getById(publication.getId(), null);

        assertFalse(dto.isOwner());
        assertNull(dto.getAddress());
    }

    @Test
    public void ofertaPendienteNoMuestraLaDireccion() {
        OfferCreateRequest request = new OfferCreateRequest();
        request.setAmount(80.0);
        interactionService.makeOffer(publication.getId(), request, buyer.getEmail());

        PublicationDetailDTO dto = publicationService.getById(publication.getId(), buyer.getEmail());

        assertEquals(OfferStatus.PENDING, dto.getMyOffer().getStatus());
        assertNull(dto.getAddress());
    }

    @Test
    public void ofertaAceptadaMuestraLaDireccion() {
        OfferCreateRequest request = new OfferCreateRequest();
        request.setAmount(80.0);
        OfferDTO created = interactionService.makeOffer(publication.getId(), request, buyer.getEmail());
        entityManager.find(Offer.class, created.getId()).setStatus(OfferStatus.ACCEPTED);
        entityManager.flush();

        PublicationDetailDTO dto = publicationService.getById(publication.getId(), buyer.getEmail());

        assertTrue(dto.isAddressVisible());
        assertEquals("Av. Santa Fe 3253", dto.getAddress());
    }

    @Test
    public void noSePuedenTenerDosOfertasPendientes() {
        OfferCreateRequest request = new OfferCreateRequest();
        request.setAmount(80.0);
        interactionService.makeOffer(publication.getId(), request, buyer.getEmail());

        assertThrows(BadRequestException.class,
                () -> interactionService.makeOffer(publication.getId(), request, buyer.getEmail()));
    }

    @Test
    public void unaOfertaPosteriorNoOcultaLaAceptadaDelMismoComprador() {
        Offer accepted = oferta(OfferStatus.ACCEPTED, java.time.LocalDateTime.now().minusDays(1));
        oferta(OfferStatus.REJECTED, java.time.LocalDateTime.now());
        publication.setState(PublicationState.SOLD);
        entityManager.flush();
        entityManager.clear();

        PublicationDetailDTO dto = publicationService.getById(publication.getId(), buyer.getEmail());
        assertTrue(dto.isAddressVisible());
        assertEquals("Av. Santa Fe 3253", dto.getAddress());
        assertEquals(accepted.getId(), dto.getMyOffer().getId());

        User outsider = new User("Tercero", "tercero-direccion@test.com", "pass");
        entityManager.persist(outsider);
        assertFalse(publicationService.getById(publication.getId(), outsider.getEmail()).isAddressVisible());
    }

    @Test
    public void publicacionAntiguaSinDireccionConservaAutorizacionPeroNoInventaDestinoDesdeZona() {
        oferta(OfferStatus.ACCEPTED, java.time.LocalDateTime.now());
        publication.setAddress(null);
        publication.setLatitude(null);
        publication.setLongitude(null);
        publication.setLocation("Av. Carabobo 07");
        entityManager.flush();
        PublicationDetailDTO dto = publicationService.getById(publication.getId(), buyer.getEmail());
        assertTrue(dto.isAddressVisible());
        assertNull(dto.getAddress());
        assertNull(dto.getLatitude());
        assertEquals("Av. Carabobo 07", dto.getLocation());
    }

    private Offer oferta(OfferStatus status, java.time.LocalDateTime createdAt) {
        Offer offer = new Offer();
        offer.setPublication(publication);
        offer.setSeller(seller);
        offer.setBuyer(buyer);
        offer.setAmount(80.0);
        offer.setStatus(status);
        offer.setCreatedAt(createdAt);
        entityManager.persist(offer);
        return offer;
    }

    @Test
    public void elVendedorNoPuedeOfertarNiPreguntarEnLoSuyo() {
        OfferCreateRequest request = new OfferCreateRequest();
        request.setAmount(80.0);

        assertThrows(BadRequestException.class,
                () -> interactionService.makeOffer(publication.getId(), request, seller.getEmail()));
        assertThrows(BadRequestException.class,
                () -> interactionService.ask(publication.getId(), "¿Sigue disponible?", seller.getEmail()));
    }

    @Test
    public void noSePuedeOfertarEnUnaPublicacionPausada() {
        publication.setState(PublicationState.PAUSED);
        entityManager.flush();
        OfferCreateRequest request = new OfferCreateRequest();
        request.setAmount(80.0);

        assertThrows(BadRequestException.class,
                () -> interactionService.makeOffer(publication.getId(), request, buyer.getEmail()));
    }

    @Test
    public void preguntarYResponder() {
        QuestionDTO question = interactionService.ask(publication.getId(), "¿Sigue disponible?", buyer.getEmail());

        assertThrows(ForbiddenException.class,
                () -> interactionService.answer(question.getId(), "Sí", buyer.getEmail()));

        interactionService.answer(question.getId(), "Sí, disponible", seller.getEmail());

        List<QuestionDTO> questions = interactionService.getQuestions(publication.getId());
        assertEquals(1, questions.size());
        assertEquals("Sí, disponible", questions.get(0).getAnswer());
        assertEquals("Comprador Test", questions.get(0).getAskerName());
    }
}
