package com.ronda.backend.service;

import com.ronda.backend.dto.OfferCreateDTO;
import com.ronda.backend.dto.OfferDTO;
import com.ronda.backend.dto.OfferRespondDTO;
import com.ronda.backend.exception.ResourceNotFoundException;
import com.ronda.backend.model.Offer;
import com.ronda.backend.model.OfferStatus;
import com.ronda.backend.model.Publication;
import com.ronda.backend.model.User;
import com.ronda.backend.repository.OfferRepository;
import com.ronda.backend.repository.PublicationRepository;
import com.ronda.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final PublicationRepository publicationRepository;

    public OfferService(OfferRepository offerRepository,
                        UserRepository userRepository,
                        PublicationRepository publicationRepository) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.publicationRepository = publicationRepository;
    }

    @Transactional
    public OfferDTO createOffer(OfferCreateDTO dto, String buyerEmail) {
        if (buyerEmail == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Comprador no encontrado"));

        Publication publication = publicationRepository.findById(dto.getPublicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        if (publication.getSeller() != null && publication.getSeller().getId().equals(buyer.getId())) {
            throw new RuntimeException("No puedes hacer una oferta en tu propia publicación");
        }

        Offer offer = new Offer();
        offer.setPublication(publication);
        offer.setBuyer(buyer);
        offer.setSeller(publication.getSeller());
        offer.setOfferedPrice(dto.getOfferedPrice());
        offer.setMessage(dto.getMessage());
        offer.setStatus(OfferStatus.PENDING);

        Offer saved = offerRepository.save(offer);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<OfferDTO> getSentOffers(String email) {
        if (email == null) return List.of();
        User buyer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        checkAndUpdateExpirations();

        return offerRepository.findByBuyer(buyer).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfferDTO> getReceivedOffers(String email) {
        if (email == null) return List.of();
        User seller = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        checkAndUpdateExpirations();

        return offerRepository.findBySeller(seller).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OfferDTO respondOffer(Long offerId, OfferRespondDTO dto, String sellerEmail) {
        if (sellerEmail == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta no encontrada"));

        if (offer.getSeller() == null || !offer.getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("No autorizado para responder esta oferta");
        }

        if (offer.getStatus() != OfferStatus.PENDING && offer.getStatus() != OfferStatus.COUNTER_OFFER) {
            throw new RuntimeException("Esta oferta ya no está pendiente de respuesta");
        }

        if (dto.getStatus() == OfferStatus.COUNTER_OFFER) {
            if (dto.getNewPrice() == null || dto.getNewPrice() <= 0) {
                throw new RuntimeException("Debe especificar un nuevo precio válido para la contraoferta");
            }
            offer.setOfferedPrice(dto.getNewPrice());
            offer.setStatus(OfferStatus.COUNTER_OFFER);
        } else {
            offer.setStatus(dto.getStatus());
        }

        Offer updated = offerRepository.save(offer);
        return convertToDTO(updated);
    }

    private void checkAndUpdateExpirations() {
        List<Offer> pendingOffers = offerRepository.findAll().stream()
                .filter(o -> o.getStatus() == OfferStatus.PENDING || o.getStatus() == OfferStatus.COUNTER_OFFER)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        for (Offer offer : pendingOffers) {
            if (offer.getExpiresAt() != null && offer.getExpiresAt().isBefore(now)) {
                offer.setStatus(OfferStatus.EXPIRED);
                offerRepository.save(offer);
            }
        }
    }

    private OfferDTO convertToDTO(Offer offer) {
        if (offer == null) return null;
        OfferDTO dto = new OfferDTO();
        dto.setId(offer.getId());
        if (offer.getPublication() != null) {
            dto.setPublicationId(offer.getPublication().getId());
            dto.setPublicationTitle(offer.getPublication().getTitle());
            dto.setPublicationOriginalPrice(offer.getPublication().getPrice());
            if (offer.getPublication().getImageUrls() != null && !offer.getPublication().getImageUrls().isEmpty()) {
                dto.setPublicationImage(offer.getPublication().getImageUrls().get(0));
            }
        }
        if (offer.getBuyer() != null) {
            dto.setBuyerId(offer.getBuyer().getId());
            dto.setBuyerName(offer.getBuyer().getNombre());
        }
        if (offer.getSeller() != null) {
            dto.setSellerId(offer.getSeller().getId());
            dto.setSellerName(offer.getSeller().getNombre());
        }
        dto.setOfferedPrice(offer.getOfferedPrice());
        dto.setMessage(offer.getMessage());
        dto.setStatus(offer.getStatus());
        dto.setExpiresAt(offer.getExpiresAt());
        dto.setCreatedAt(offer.getCreatedAt());
        return dto;
    }
}
