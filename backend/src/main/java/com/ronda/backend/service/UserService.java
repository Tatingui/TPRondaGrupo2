package com.ronda.backend.service;

import com.ronda.backend.dto.UserProfileResponse;
import com.ronda.backend.dto.UserProfileUpdateRequest;
import com.ronda.backend.model.User;
import com.ronda.backend.repository.UserRepository;
import com.ronda.backend.repository.OfferRepository;
import com.ronda.backend.repository.PublicationRepository;
import com.ronda.backend.repository.QuestionRepository;
import com.ronda.backend.repository.RatingRepository;
import com.ronda.backend.repository.SavedSearchRepository;
import com.ronda.backend.repository.TransactionRepository;
import com.ronda.backend.repository.UserFavoriteRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final PublicationRepository publicationRepository;
    private final QuestionRepository questionRepository;
    private final RatingRepository ratingRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final TransactionRepository transactionRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final TransactionService transactionService;

    public UserService(UserRepository userRepository, TransactionService transactionService,
                       OfferRepository offerRepository, PublicationRepository publicationRepository,
                       QuestionRepository questionRepository, RatingRepository ratingRepository,
                       SavedSearchRepository savedSearchRepository, TransactionRepository transactionRepository,
                       UserFavoriteRepository userFavoriteRepository) {
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.offerRepository = offerRepository;
        this.publicationRepository = publicationRepository;
        this.questionRepository = questionRepository;
        this.ratingRepository = ratingRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.transactionRepository = transactionRepository;
        this.userFavoriteRepository = userFavoriteRepository;
    }

    /**
     * Devuelve el perfil del usuario identificado por email (extraido del JWT).
     */
    public UserProfileResponse getProfile(String email) {
        Optional<User> encontrado = userRepository.findByEmail(email);
        if (encontrado.isEmpty()) {
            return null;
        }

        User user = encontrado.get();
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setNombre(user.getNombre());
        response.setEmail(user.getEmail());
        response.setTelefono(user.getTelefono());
        response.setZona(user.getZona());
        response.setProfileImageUrl(user.getProfileImageUrl());

        response.setMiembroDesde(formatMiembroDesde(user.getCreatedAt()));

        // Reputación real calculada desde calificaciones recibidas
        response.setReputacion(transactionService.getAverageRating(user.getId()));
        response.setCantidadOpiniones(transactionService.getRatingCount(user.getId()));
        response.setCantidadVentas(transactionService.getSaleCount(user.getId()));
        response.setCantidadCompras(transactionService.getPurchaseCount(user.getId()));

        return response;
    }

    /**
     * Formatea la fecha de creacion como "Septiembre 2026". Null si no hay fecha.
     */
    public static String formatMiembroDesde(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }
        String mes = createdAt.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "AR"));
        // Capitalizar primera letra
        mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
        return mes + " " + createdAt.getYear();
    }

    /**
     * Actualiza los campos editables del perfil. Solo pisa los que vienen != null.
     */
    public UserProfileResponse updateProfile(String email, UserProfileUpdateRequest request) {
        Optional<User> encontrado = userRepository.findByEmail(email);
        if (encontrado.isEmpty()) {
            return null;
        }

        User user = encontrado.get();

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            user.setNombre(request.getNombre().trim());
        }
        if (request.getTelefono() != null) {
            user.setTelefono(request.getTelefono().trim());
        }
        if (request.getZona() != null) {
            user.setZona(request.getZona().trim());
        }

        userRepository.save(user);

        // Devolvemos el perfil actualizado
        return getProfile(email);
    }

    /**
     * Actualiza solo la foto de perfil del usuario.
     */
    public UserProfileResponse updateProfileImage(String email, String imageUrl) {
        Optional<User> encontrado = userRepository.findByEmail(email);
        if (encontrado.isEmpty()) {
            return null;
        }
        User user = encontrado.get();
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
        return getProfile(email);
    }

    /**
     * Borra la cuenta del usuario y todos sus datos asociados.
     */
    @Transactional
    public boolean deleteAccount(String email) {
        Optional<User> encontrado = userRepository.findByEmail(email);
        if (encontrado.isEmpty()) {
            return false;
        }
        User user = encontrado.get();

        ratingRepository.deleteAll(ratingRepository.findByFromUser(user));
        ratingRepository.deleteAll(ratingRepository.findByToUser(user));
        transactionRepository.deleteAll(transactionRepository.findByBuyer(user));
        transactionRepository.deleteAll(transactionRepository.findBySeller(user));
        offerRepository.deleteAll(offerRepository.findByBuyer(user));
        offerRepository.deleteAll(offerRepository.findBySeller(user));
        questionRepository.deleteAll(questionRepository.findByAsker(user));
        userFavoriteRepository.deleteAll(userFavoriteRepository.findByUser(user));
        savedSearchRepository.deleteAll(savedSearchRepository.findByUserOrderByCreatedAtDesc(user));
        publicationRepository.deleteAll(publicationRepository.findBySeller(user));
        userRepository.delete(user);
        return true;
    }
}