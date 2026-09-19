package com.ronda.backend.repository;

import com.ronda.backend.model.Offer;
import com.ronda.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByBuyer(User buyer);
    List<Offer> findBySeller(User seller);
    List<Offer> findByPublicationId(Long publicationId);
    Optional<Offer> findFirstByPublicationIdAndBuyerIdOrderByCreatedAtDesc(Long publicationId, Long buyerId);
}
