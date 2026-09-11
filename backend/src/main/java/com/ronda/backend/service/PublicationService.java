package com.ronda.backend.service;

import com.ronda.backend.dto.PublicationCreateDTO;
import com.ronda.backend.dto.PublicationDTO;
import com.ronda.backend.exception.ResourceNotFoundException;
import com.ronda.backend.model.Category;
import com.ronda.backend.model.Publication;
import com.ronda.backend.model.PublicationState;
import com.ronda.backend.model.PublicationStatus;
import com.ronda.backend.model.User;
import com.ronda.backend.model.UserFavorite;
import com.ronda.backend.repository.CategoryRepository;
import com.ronda.backend.repository.PublicationRepository;
import com.ronda.backend.repository.UserFavoriteRepository;
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
    private final UserFavoriteRepository userFavoriteRepository;
    private final CategoryRepository categoryRepository;

    public PublicationService(PublicationRepository publicationRepository,
                              UserRepository userRepository,
                              UserFavoriteRepository userFavoriteRepository,
                              CategoryRepository categoryRepository) {
        this.publicationRepository = publicationRepository;
        this.userRepository = userRepository;
        this.userFavoriteRepository = userFavoriteRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<PublicationDTO> findAll(String search, Long categoryId, Double minPrice, Double maxPrice, 
                                       PublicationStatus status, String location, Pageable pageable) {
        
        String currentUserEmail = getCurrentUserEmail();
        Set<Long> favoriteIds = getFavoriteIdsForUser(currentUserEmail);

        Specification<Publication> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Solo mostrar activas en la exploración general si se desea, o todas
            predicates.add(cb.equal(root.get("state"), PublicationState.ACTIVE));

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
    public PublicationDTO createPublication(PublicationCreateDTO dto, String email) {
        if (email == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        User seller = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Publication publication = new Publication();
        publication.setTitle(dto.getTitle());
        publication.setDescription(dto.getDescription());
        publication.setPrice(dto.getPrice());
        publication.setStatus(dto.getStatus());
        publication.setState(PublicationState.ACTIVE);
        publication.setLocation(dto.getLocation());
        publication.setCategory(category);
        publication.setSeller(seller);
        if (dto.getImageUrls() != null) {
            publication.setImageUrls(new ArrayList<>(dto.getImageUrls()));
        }

        Publication saved = publicationRepository.save(publication);
        return convertToDTO(saved, getFavoriteIdsForUser(email));
    }

    @Transactional(readOnly = true)
    public List<PublicationDTO> getMyPublications(String email) {
        if (email == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Set<Long> favoriteIds = getFavoriteIdsForUser(email);

        List<Publication> userPublications = publicationRepository.findBySeller(user);
        return userPublications.stream()
                .map(pub -> convertToDTO(pub, favoriteIds))
                .collect(Collectors.toList());
    }

    @Transactional
    public PublicationDTO updateStatus(Long publicationId, PublicationState state, String email) {
        if (email == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        if (publication.getSeller() == null || !publication.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("No autorizado para modificar esta publicación");
        }

        publication.setState(state);
        Publication updated = publicationRepository.save(publication);
        return convertToDTO(updated, getFavoriteIdsForUser(email));
    }

    @Transactional
    public void markAsFavorite(Long publicationId, String email) {
        if (email == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        if (!userFavoriteRepository.existsByUserIdAndPublicationId(user.getId(), publicationId)) {
            Double priceToSave = publication.getPrice() != null ? publication.getPrice() : 0.0;
            UserFavorite favorite = new UserFavorite(user, publication, priceToSave);
            userFavoriteRepository.save(favorite);
        }
    }

    @Transactional
    public void unmarkAsFavorite(Long publicationId, String email) {
        if (email == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        userFavoriteRepository.deleteByUserIdAndPublicationId(user.getId(), publicationId);
    }

    @Transactional(readOnly = true)
    public List<PublicationDTO> getFavorites(String email) {
        if (email == null) {
            return Collections.emptyList();
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        List<UserFavorite> favorites = userFavoriteRepository.findByUser(user);
        Set<Long> favoriteIds = favorites.stream()
                .filter(uf -> uf.getPublication() != null && uf.getPublication().getId() != null)
                .map(uf -> uf.getPublication().getId())
                .collect(Collectors.toSet());

        return favorites.stream()
                .map(uf -> uf.getPublication())
                .filter(pub -> pub != null)
                .map(pub -> convertToDTO(pub, favoriteIds))
                .collect(Collectors.toList());
    }

    private String getCurrentUserEmail() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    private Set<Long> getFavoriteIdsForUser(String email) {
        if (email == null) return Collections.emptySet();
        return userRepository.findByEmail(email)
                .map(user -> userFavoriteRepository.findByUser(user).stream()
                        .filter(uf -> uf.getPublication() != null && uf.getPublication().getId() != null)
                        .map(uf -> uf.getPublication().getId())
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    private PublicationDTO convertToDTO(Publication pub, Set<Long> favoriteIds) {
        if (pub == null) return null;
        PublicationDTO dto = new PublicationDTO();
        dto.setId(pub.getId());
        dto.setTitle(pub.getTitle());
        dto.setDescription(pub.getDescription());
        dto.setPrice(pub.getPrice());
        dto.setStatus(pub.getStatus());
        dto.setState(pub.getState());
        dto.setLocation(pub.getLocation());
        if (pub.getCategory() != null) {
            dto.setCategoryName(pub.getCategory().getName());
        }
        if (pub.getImageUrls() != null) {
            dto.setImageUrls(new ArrayList<>(pub.getImageUrls()));
        }
        dto.setCreatedAt(pub.getCreatedAt());
        if (pub.getSeller() != null) {
            dto.setSellerId(pub.getSeller().getId());
            dto.setSellerName(pub.getSeller().getNombre());
        }
        dto.setFavorite(favoriteIds != null && pub.getId() != null && favoriteIds.contains(pub.getId()));
        return dto;
    }
}
