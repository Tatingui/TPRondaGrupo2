package com.ronda.backend.controller;

import com.ronda.backend.dto.PublicProfileDTO;
import com.ronda.backend.dto.UserProfileResponse;
import com.ronda.backend.dto.UserProfileUpdateRequest;
import com.ronda.backend.service.PublicationService;
import com.ronda.backend.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserService userService;
    private final PublicationService publicationService;

    public UserController(UserService userService, PublicationService publicationService) {
        this.userService = userService;
        this.publicationService = publicationService;
    }

    /**
     * GET /api/usuarios/{id}/publico
     * Perfil publico de cualquier usuario: reputacion, antiguedad y publicaciones activas.
     * No devuelve email ni telefono.
     */
    @GetMapping("/{id}/publico")
    public ResponseEntity<PublicProfileDTO> getPublicProfile(@PathVariable Long id) {
        return ResponseEntity.ok(publicationService.getPerfilPublico(id, getEmailFromToken()));
    }

    /**
     * GET /api/usuarios/me
     * Devuelve el perfil del usuario logueado (email extraido del JWT).
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        String email = getEmailFromToken();
        UserProfileResponse profile = userService.getProfile(email);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/usuarios/me
     * Actualiza los campos editables del perfil. Solo pisa los que vienen != null.
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateRequest request) {
        String email = getEmailFromToken();
        UserProfileResponse updated = userService.updateProfile(email, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/usuarios/me
     * Borra la cuenta del usuario logueado y todos sus datos.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        String email = getEmailFromToken();
        boolean deleted = userService.deleteAccount(email);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private String getEmailFromToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
