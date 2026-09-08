package com.ronda.backend.controller;

import com.ronda.backend.dto.SavedSearchDTO;
import com.ronda.backend.service.SavedSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/saved-searches")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    public SavedSearchController(SavedSearchService savedSearchService) {
        this.savedSearchService = savedSearchService;
    }

    @GetMapping
    public ResponseEntity<List<SavedSearchDTO>> getSavedSearches(Authentication authentication) {
        return ResponseEntity.ok(savedSearchService.getSavedSearches(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<SavedSearchDTO> saveSearch(@RequestBody SavedSearchDTO dto, Authentication authentication) {
        return ResponseEntity.ok(savedSearchService.saveSearch(dto, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSearch(@PathVariable Long id, Authentication authentication) {
        savedSearchService.deleteSearch(id, authentication.getName());
        return ResponseEntity.ok().build();
    }
}
