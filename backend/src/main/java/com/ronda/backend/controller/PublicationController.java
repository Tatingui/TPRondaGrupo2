package com.ronda.backend.controller;

import com.ronda.backend.dto.PublicationDTO;
import com.ronda.backend.model.PublicationStatus;
import com.ronda.backend.service.PublicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/publications")
public class PublicationController {

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @GetMapping
    public ResponseEntity<Page<PublicationDTO>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) PublicationStatus status,
            @RequestParam(required = false) String location,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        
        Page<PublicationDTO> publications = publicationService.findAll(
                search, categoryId, minPrice, maxPrice, status, location, pageable);
        
        return ResponseEntity.ok(publications);
    }
}
