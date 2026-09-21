package com.ronda.backend.service;

import com.ronda.backend.dto.PublicationCreateDTO;
import com.ronda.backend.dto.PublicationDTO;
import com.ronda.backend.dto.PublicationDetailDTO;
import com.ronda.backend.dto.PublicProfileDTO;
import com.ronda.backend.dto.SellerDTO;
import com.ronda.backend.exception.ForbiddenException;
import com.ronda.backend.exception.ResourceNotFoundException;
import com.ronda.backend.model.Category;
import com.ronda.backend.model.Offer;
import com.ronda.backend.model.OfferStatus;
import com.ronda.backend.model.Publication;
import com.ronda.backend.model.PublicationState;
import com.ronda.backend.model.PublicationStatus;
import com.ronda.backend.model.User;
import com.ronda.backend.model.UserFavorite;
import com.ronda.backend.repository.CategoryRepository;
import com.ronda.backend.repository.OfferRepository;
import com.ronda.backend.repository.PublicationRepository;
import com.ronda.backend.repository.QuestionRepository;
import com.ronda.backend.repository.RatingRepository;
import com.ronda.backend.repository.TransactionRepository;
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
    private final OfferRepository offerRepository;
    private final TransactionService transactionService;
    private final QuestionRepository questionRepository;
    private final RatingRepository ratingRepository;
    private final TransactionRepository transactionRepository;

    public PublicationService(PublicationRepository publicationRepository,
                              UserRepository userRepository,
                              UserFavoriteRepository userFavoriteRepository,
                              CategoryRepository categoryRepository,
                              OfferRepository offerRepository,
                              TransactionService transactionService,
                              QuestionRepository questionRepository,
                              RatingRepository ratingRepository,
                              TransactionRepository transactionRepository) {
        this.publicationRepository = publicationRepository;
        this.userRepository = userRepository;
        this.userFavoriteRepository = userFavoriteRepository;
        this.categoryRepository = categoryRepository;
        this.offerRepository = offerRepository;
        this.transactionService = transactionService;
        this.questionRepository = questionRepository;
        this.ratingRepository = ratingRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<PublicationDTO> findAll(String search, Long categoryId, Double minPrice, Double maxPrice, 
                                       PublicationStatus status, String location, Pageable pageable) {
        
        String currentUserEmail = getCurrentUserEmail();
        Set<Long> favoriteIds = getFavoriteIdsForUser(currentUserEmail);

        Specification<Publication> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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

    /**
     * Detalle de una publicacion segun quien mira (email es null si no hay sesion).
     */
    @Transactional(readOnly = true)
    public PublicationDetailDTO getById(Long publicationId, String email) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        PublicationDetailDTO dto = new PublicationDetailDTO();
        llenarDTO(dto, publication, getFavoriteIdsForUser(email));
        dto.setVendedor(convertirVendedor(publication.getSeller()));

        User viewer = null;
        if (email != null) {
            viewer = userRepository.findByEmail(email).orElse(null);
        }

        boolean esVendedor = viewer != null && publication.getSeller() != null
                && publication.getSeller().getId().equals(viewer.getId());
        dto.setOwner(esVendedor);

        // La aceptada determina acceso; una oferta posterior no revoca la compra.
        Offer miOferta = null;
        if (viewer != null && !esVendedor) {
            final Long buyerId = viewer.getId();
            miOferta = offerRepository
                    .findFirstByPublicationIdAndBuyerIdAndStatusOrderByCreatedAtDesc(publicationId, buyerId, OfferStatus.ACCEPTED)
                    .or(() -> offerRepository.findFirstByPublicationIdAndBuyerIdOrderByCreatedAtDesc(publicationId, buyerId))
                    .orElse(null);
            dto.setMyOffer(PublicationInteractionService.convertirOferta(miOferta));
        }

        // La direccion exacta solo la ve el vendedor o el comprador con la oferta aceptada
        boolean ofertaAceptada = miOferta != null && miOferta.getEffectiveStatus() == OfferStatus.ACCEPTED;
        if (esVendedor || ofertaAceptada) {
            dto.setAddressVisible(true);
            dto.setAddress(publication.getAddress());
            dto.setLatitude(publication.getLatitude());
            dto.setLongitude(publication.getLongitude());
        }
        return dto;
    }

    /**
     * Perfil publico de un usuario: reputacion, antiguedad, zona y sus publicaciones activas.
     * email es de quien mira (para marcar sus favoritos); puede ser null.
     */
    @Transactional(readOnly = true)
    public PublicProfileDTO getPerfilPublico(Long userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        PublicProfileDTO dto = new PublicProfileDTO();
        llenarVendedor(dto, user);

        Set<Long> favoriteIds = getFavoriteIdsForUser(email);
        List<PublicationDTO> activas = publicationRepository
                .findBySellerIdAndStateOrderByCreatedAtDesc(userId, PublicationState.ACTIVE).stream()
                .map(pub -> convertToDTO(pub, favoriteIds))
                .collect(Collectors.toList());
        dto.setPublicacionesActivas(activas);
        return dto;
    }

    private SellerDTO convertirVendedor(User seller) {
        if (seller == null) return null;
        SellerDTO dto = new SellerDTO();
        llenarVendedor(dto, seller);
        return dto;
    }

    /** Copia los datos publicos del usuario (sirve tambien para el perfil publico). */
    private void llenarVendedor(SellerDTO dto, User seller) {
        dto.setId(seller.getId());
        dto.setNombre(seller.getNombre());
        dto.setUbicacion(seller.getZona());
        dto.setMiembroDesde(UserService.formatMiembroDesde(seller.getCreatedAt()));
        // Reputación real calculada desde calificaciones recibidas
        dto.setReputacion(transactionService.getAverageRating(seller.getId()));
        dto.setCantidadOpiniones(transactionService.getRatingCount(seller.getId()));
        dto.setCantidadVentas(transactionService.getSaleCount(seller.getId()));
        dto.setCantidadCompras(transactionService.getPurchaseCount(seller.getId()));
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
        publication.setAddress(dto.getAddress() == null ? null : dto.getAddress().trim());
        publication.setLatitude(dto.getLatitude());
        publication.setLongitude(dto.getLongitude());
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
            throw new ForbiddenException("No autorizado para modificar esta publicación");
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

    @Transactional
    public void updateLastSeenPrice(Long publicationId, String email) {
        if (email == null) {
            return;
        }
        userRepository.findByEmail(email).ifPresent(user -> {
            userFavoriteRepository.findByUserIdAndPublicationId(user.getId(), publicationId).ifPresent(favorite -> {
                publicationRepository.findById(publicationId).ifPresent(pub -> {
                    if (pub.getPrice() != null) {
                        favorite.setLastSeenPrice(pub.getPrice());
                        userFavoriteRepository.save(favorite);
                    }
                });
            });
        });
    }


    @Transactional
    public void deletePublication(Long publicationId, String email) {
        if (email == null) {
            throw new ResourceNotFoundException("Usuario no autenticado");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        if (publication.getSeller() == null || !publication.getSeller().getId().equals(user.getId())) {
            throw new ForbiddenException("No autorizado para eliminar esta publicación");
        }

        // Eliminar ratings de las transacciones asociadas
        List<com.ronda.backend.model.Transaction> transactions = transactionRepository.findByPublicationId(publicationId);
        for (com.ronda.backend.model.Transaction tx : transactions) {
            ratingRepository.deleteByTransactionId(tx.getId());
        }

        // Eliminar en orden: transacciones, ofertas, preguntas, favoritos, publicación
        transactionRepository.deleteByPublicationId(publicationId);
        offerRepository.deleteByPublicationId(publicationId);
        questionRepository.deleteByPublicationId(publicationId);
        userFavoriteRepository.deleteByPublicationId(publicationId);
        publicationRepository.delete(publication);
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
                .filter(uf -> uf.getPublication() != null)
                .map(uf -> {
                    PublicationDTO dto = convertToDTO(uf.getPublication(), favoriteIds);
                    dto.setLastSeenPrice(uf.getLastSeenPrice());
                    return dto;
                })
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
        llenarDTO(dto, pub, favoriteIds);
        return dto;
    }

    /** Copia los datos de la publicacion al DTO (sirve tambien para el DTO de detalle). */
    private void llenarDTO(PublicationDTO dto, Publication pub, Set<Long> favoriteIds) {
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
    }
}
