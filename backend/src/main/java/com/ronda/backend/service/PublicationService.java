package com.ronda.backend.service;

import com.ronda.backend.dto.PublicationDTO;
import com.ronda.backend.exception.ResourceNotFoundException;
import com.ronda.backend.model.Publication;
import com.ronda.backend.model.PublicationStatus;
import com.ronda.backend.model.User;
import com.ronda.backend.repository.PublicationRepository;
import com.ronda.backend.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;

    public PublicationService(PublicationRepository publicationRepository, UserRepository userRepository) {
        this.publicationRepository = publicationRepository;
        this.userRepository = userRepository;
    }

    public Page<PublicationDTO> findAll(String search, Long categoryId, Double minPrice, Double maxPrice, 
                                       PublicationStatus status, String location, Pageable pageable) {
        
        String currentUserEmail = getCurrentUserEmail();
        Set<Long> favoriteIds = getFavoriteIdsForUser(currentUserEmail);

        Specification<Publication> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (location != null && !location.isEmpty()) {
                predicates.add(cb.equal(root.get("location"), location));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return publicationRepository.findAll(spec, pageable).map(pub -> convertToDTO(pub, favoriteIds));
    }

    @Transactional
    public void markAsFavorite(Long publicationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        user.getFavorites().add(publication);
        userRepository.save(user);
    }

    @Transactional
    public void unmarkAsFavorite(Long publicationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        user.getFavorites().remove(publication);
        userRepository.save(user);
    }

    public List<PublicationDTO> getFavorites(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        Set<Publication> favorites = user.getFavorites();
        Set<Long> favoriteIds = favorites.stream().map(Publication::getId).collect(Collectors.toSet());

        return favorites.stream()
                .map(pub -> convertToDTO(pub, favoriteIds))
                .collect(Collectors.toList());
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    private Set<Long> getFavoriteIdsForUser(String email) {
        if (email == null) return Collections.emptySet();
        return userRepository.findByEmail(email)
                .map(user -> user.getFavorites().stream()
                        .map(Publication::getId)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    private PublicationDTO convertToDTO(Publication pub, Set<Long> favoriteIds) {
        PublicationDTO dto = new PublicationDTO();
        dto.setId(pub.getId());
        dto.setTitle(pub.getTitle());
        dto.setDescription(pub.getDescription());
        dto.setPrice(pub.getPrice());
        dto.setStatus(pub.getStatus());
        dto.setLocation(pub.getLocation());
        dto.setCategoryName(pub.getCategory().getName());
        dto.setImageUrls(pub.getImageUrls());
        dto.setCreatedAt(pub.getCreatedAt());
        dto.setSellerId(pub.getSeller().getId());
        dto.setSellerName(pub.getSeller().getNombre());
        dto.setFavorite(favoriteIds.contains(pub.getId()));
        return dto;
    }
}
