package com.ronda.backend.controller;

import com.ronda.backend.dto.OfferCreateDTO;
import com.ronda.backend.dto.OfferDTO;
import com.ronda.backend.dto.OfferRespondDTO;
import com.ronda.backend.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    public ResponseEntity<OfferDTO> createOffer(@Valid @RequestBody OfferCreateDTO dto, Authentication authentication) {
        OfferDTO created = offerService.createOffer(dto, authentication.getName());
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/sent")
    public ResponseEntity<List<OfferDTO>> getSentOffers(Authentication authentication) {
        List<OfferDTO> offers = offerService.getSentOffers(authentication.getName());
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/received")
    public ResponseEntity<List<OfferDTO>> getReceivedOffers(Authentication authentication) {
        List<OfferDTO> offers = offerService.getReceivedOffers(authentication.getName());
        return ResponseEntity.ok(offers);
    }

    @PatchMapping("/{id}/respond")
    public ResponseEntity<OfferDTO> respondOffer(@PathVariable Long id, @Valid @RequestBody OfferRespondDTO dto, Authentication authentication) {
        OfferDTO updated = offerService.respondOffer(id, dto, authentication.getName());
        return ResponseEntity.ok(updated);
    }
}
