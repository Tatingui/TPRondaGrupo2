package com.ronda.backend.controller;

import com.ronda.backend.dto.PublicationCreateDTO;
import com.ronda.backend.dto.OfferCreateRequest;
import com.ronda.backend.dto.OfferDTO;
import com.ronda.backend.dto.PublicationDTO;
import com.ronda.backend.dto.PublicationDetailDTO;
import com.ronda.backend.dto.QuestionDTO;
import com.ronda.backend.dto.TextRequest;
import com.ronda.backend.model.PublicationState;
import com.ronda.backend.model.PublicationStatus;
import com.ronda.backend.service.PublicationInteractionService;
import com.ronda.backend.service.PublicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/publications")
public class PublicationController {

    private final PublicationService publicationService;
    private final PublicationInteractionService interactionService;

    public PublicationController(PublicationService publicationService,
                                 PublicationInteractionService interactionService) {
        this.publicationService = publicationService;
        this.interactionService = interactionService;
    }

    @GetMapping
    public ResponseEntity<Page<PublicationDTO>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) PublicationStatus status,
            @RequestParam(required = false) String location,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<PublicationDTO> publications = publicationService.findAll(
                search, categoryId, minPrice, maxPrice, status, location, pageable);
        
        return ResponseEntity.ok(publications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicationDetailDTO> getById(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(publicationService.getById(id, email));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionDTO>> getQuestions(@PathVariable Long id) {
        return ResponseEntity.ok(interactionService.getQuestions(id));
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<QuestionDTO> ask(@PathVariable Long id, @Valid @RequestBody TextRequest request,
                                           Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(201).body(interactionService.ask(id, request.getText(), authentication.getName()));
    }

    @PutMapping("/questions/{questionId}/answer")
    public ResponseEntity<QuestionDTO> answer(@PathVariable Long questionId, @Valid @RequestBody TextRequest request,
                                              Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(interactionService.answer(questionId, request.getText(), authentication.getName()));
    }

    @PostMapping("/{id}/offers")
    public ResponseEntity<OfferDTO> makeOffer(@PathVariable Long id, @Valid @RequestBody OfferCreateRequest request,
                                              Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(201).body(interactionService.makeOffer(id, request, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<PublicationDTO> createPublication(@Valid @RequestBody PublicationCreateDTO dto, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PublicationDTO created = publicationService.createPublication(dto, authentication.getName());
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/my")
    public ResponseEntity<List<PublicationDTO>> getMyPublications(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PublicationDTO> myPublications = publicationService.getMyPublications(authentication.getName());
        return ResponseEntity.ok(myPublications);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PublicationDTO> updateStatus(@PathVariable Long id, @RequestParam PublicationState state, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PublicationDTO updated = publicationService.updateStatus(id, state, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> markAsFavorite(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        publicationService.markAsFavorite(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<Void> unmarkAsFavorite(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        publicationService.unmarkAsFavorite(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<PublicationDTO>> getFavorites(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(publicationService.getFavorites(authentication.getName()));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> recordView(@PathVariable Long id, Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            publicationService.updateLastSeenPrice(id, authentication.getName());
        }
        return ResponseEntity.ok().build();
    }
}
