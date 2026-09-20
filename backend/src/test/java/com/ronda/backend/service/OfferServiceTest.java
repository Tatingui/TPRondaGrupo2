package com.ronda.backend.service;

import com.ronda.backend.model.*;
import com.ronda.backend.repository.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OfferServiceTest {
    @Test
    void listarOfertasVencidasNoEscribeNiConsultaOfertasDeOtrosUsuarios() {
        OfferRepository offers = mock(OfferRepository.class);
        UserRepository users = mock(UserRepository.class);
        OfferService service = new OfferService(offers, users, mock(PublicationRepository.class));
        User user = new User("Test", "test@example.com", "pass");
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Offer pending = new Offer();
        pending.setStatus(OfferStatus.PENDING);
        pending.setExpiresAt(LocalDateTime.now().minusHours(1));
        Offer counter = new Offer();
        counter.setStatus(OfferStatus.COUNTER_OFFER);
        counter.setExpiresAt(LocalDateTime.now().minusHours(1));
        when(offers.findBySeller(user)).thenReturn(List.of(pending, counter));
        when(offers.findByBuyer(user)).thenReturn(List.of(pending, counter));

        service.getReceivedOffers(user.getEmail()).forEach(dto -> assertEquals(OfferStatus.EXPIRED, dto.getStatus()));
        service.getSentOffers(user.getEmail()).forEach(dto -> assertEquals(OfferStatus.EXPIRED, dto.getStatus()));
        assertEquals(OfferStatus.PENDING, pending.getStatus());
        assertEquals(OfferStatus.COUNTER_OFFER, counter.getStatus());
        verify(offers, never()).save(any());
        verify(offers, never()).findAll();
    }

    @Test
    void unaAceptadaNoExpiraYUnaPendienteVigenteSiguePendiente() {
        Offer offer = new Offer();
        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setExpiresAt(LocalDateTime.now().minusDays(1));
        assertEquals(OfferStatus.ACCEPTED, offer.getEffectiveStatus());
        offer.setStatus(OfferStatus.PENDING);
        offer.setExpiresAt(LocalDateTime.now().plusDays(1));
        assertEquals(OfferStatus.PENDING, offer.getEffectiveStatus());
    }
}
