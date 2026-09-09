package com.ronda.backend.service;

import com.ronda.backend.dto.SavedSearchDTO;
import com.ronda.backend.exception.ResourceNotFoundException;
import com.ronda.backend.model.SavedSearch;
import com.ronda.backend.model.User;
import com.ronda.backend.repository.SavedSearchRepository;
import com.ronda.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final UserRepository userRepository;

    public SavedSearchService(SavedSearchRepository savedSearchRepository, UserRepository userRepository) {
        this.savedSearchRepository = savedSearchRepository;
        this.userRepository = userRepository;
    }

    public List<SavedSearchDTO> getSavedSearches(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return savedSearchRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SavedSearchDTO saveSearch(SavedSearchDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        SavedSearch entity = new SavedSearch();
        entity.setUser(user);
        entity.setQuery(dto.getQuery());
        entity.setCategoryId(dto.getCategoryId());
        entity.setCategoryName(dto.getCategoryName());
        entity.setCondition(dto.getCondition());
        entity.setConditionName(dto.getConditionName());
        entity.setLocation(dto.getLocation());
        entity.setLocationName(dto.getLocationName());
        entity.setMinPrice(dto.getMinPrice());
        entity.setMaxPrice(dto.getMaxPrice());
        entity.setSort(dto.getSort());
        entity.setSortName(dto.getSortName());

        SavedSearch saved = savedSearchRepository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteSearch(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        SavedSearch savedSearch = savedSearchRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Búsqueda guardada no encontrada"));

        savedSearchRepository.delete(savedSearch);
    }

    private SavedSearchDTO convertToDTO(SavedSearch search) {
        SavedSearchDTO dto = new SavedSearchDTO();
        dto.setId(search.getId());
        dto.setQuery(search.getQuery());
        dto.setCategoryId(search.getCategoryId());
        dto.setCategoryName(search.getCategoryName());
        dto.setCondition(search.getCondition());
        dto.setConditionName(search.getConditionName());
        dto.setLocation(search.getLocation());
        dto.setLocationName(search.getLocationName());
        dto.setMinPrice(search.getMinPrice());
        dto.setMaxPrice(search.getMaxPrice());
        dto.setSort(search.getSort());
        dto.setSortName(search.getSortName());
        dto.setCreatedAt(search.getCreatedAt());
        return dto;
    }
}
