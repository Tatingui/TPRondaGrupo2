package com.ronda.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ronda.backend.config.JwtUtil;
import com.ronda.backend.model.Category;
import com.ronda.backend.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DeliveryFlowTest {
    private static final String ADDRESS = "Av. Carabobo 27, C1406DGA Ciudad Autónoma de Buenos Aires";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired EntityManager em;
    @Autowired JwtUtil jwt;
    private String seller, buyer, outsider;
    private Long categoryId;

    @BeforeEach
    void setup() {
        seller = user("seller-delivery@test.com");
        buyer = user("buyer-delivery@test.com");
        outsider = user("outsider-delivery@test.com");
        Category category = new Category("Entrega test");
        em.persist(category);
        categoryId = category.getId();
        em.flush();
    }

    private String user(String email) {
        em.persist(new User("Test", email, "not-a-real-password"));
        return "Bearer " + jwt.generateToken(email);
    }

    private String publication(String address) throws Exception {
        return json.writeValueAsString(Map.of("title", "Auriculares", "description", "Test",
                "price", 100, "status", "NEW", "location", "Flores", "categoryId", categoryId,
                "address", address));
    }

    @Test
    void publicarOfertarAceptarYRecuperarDireccionSinExponerlaATerceros() throws Exception {
        String created = mvc.perform(post("/publications").header("Authorization", seller)
                        .contentType(MediaType.APPLICATION_JSON).content(publication("  " + ADDRESS + "  ")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.address").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        long publicationId = json.readTree(created).get("id").asLong();
        em.flush();
        em.clear();
        mvc.perform(get("/publications/" + publicationId).header("Authorization", buyer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.addressVisible").value(false))
                .andExpect(jsonPath("$.address").doesNotExist());

        String offer = mvc.perform(post("/publications/" + publicationId + "/offers")
                        .header("Authorization", buyer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":90}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long offerId = json.readTree(offer).get("id").asLong();
        mvc.perform(get("/offers/received").header("Authorization", seller))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(offerId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
        mvc.perform(get("/offers/received").header("Authorization", outsider))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(put("/transactions/offers/" + offerId + "/accept").header("Authorization", outsider))
                .andExpect(status().isForbidden());
        mvc.perform(put("/transactions/offers/" + offerId + "/accept").header("Authorization", seller))
                .andExpect(status().isCreated());
        em.flush();
        em.clear();
        mvc.perform(get("/offers/sent").header("Authorization", buyer))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("ACCEPTED"));
        mvc.perform(get("/publications/" + publicationId).header("Authorization", buyer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.addressVisible").value(true))
                .andExpect(jsonPath("$.address").value(ADDRESS)).andExpect(jsonPath("$.state").value("SOLD"));
        mvc.perform(get("/publications/" + publicationId).header("Authorization", outsider))
                .andExpect(status().isOk()).andExpect(jsonPath("$.addressVisible").value(false))
                .andExpect(jsonPath("$.address").doesNotExist());
        mvc.perform(get("/publications/" + publicationId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.address").doesNotExist());
    }

    @Test
    void rechazaDireccionVaciaOLinkEnLugarDeDireccionPostal() throws Exception {
        for (String address : new String[]{" ", "https://maps.app.goo.gl/ejemplo", "www.google.com/maps", "x".repeat(256)}) {
            mvc.perform(post("/publications").header("Authorization", seller)
                            .contentType(MediaType.APPLICATION_JSON).content(publication(address)))
                    .andExpect(status().isBadRequest());
        }
    }
}
