package com.ronda.backend.service;

import com.ronda.backend.dto.UserProfileResponse;
import com.ronda.backend.dto.UserProfileUpdateRequest;
import com.ronda.backend.model.User;
import com.ronda.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public UserService(UserRepository userRepository, TransactionService transactionService) {
        this.userRepository = userRepository;
        this.transactionService = transactionService;
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
            user.setNombre(request.getNombre());
        }
        if (request.getTelefono() != null) {
            user.setTelefono(request.getTelefono());
        }
        if (request.getZona() != null) {
            user.setZona(request.getZona());
        }

        userRepository.save(user);

        // Devolvemos el perfil actualizado
        return getProfile(email);
    }
}
