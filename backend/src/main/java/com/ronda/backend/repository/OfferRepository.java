package com.ronda.backend.repository;

import com.ronda.backend.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    // Ultima oferta que hizo un comprador en una publicacion
    Optional<Offer> findFirstByPublicationIdAndBuyerIdOrderByCreatedAtDesc(Long publicationId, Long buyerId);
}
