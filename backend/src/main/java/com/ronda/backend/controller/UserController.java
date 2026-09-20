package com.ronda.backend.controller;

import com.ronda.backend.dto.PublicProfileDTO;
import com.ronda.backend.dto.UserProfileResponse;
import com.ronda.backend.dto.UserProfileUpdateRequest;
import com.ronda.backend.service.FileStorageService;
import com.ronda.backend.service.PublicationService;
import com.ronda.backend.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserService userService;
    private final PublicationService publicationService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService, PublicationService publicationService,
                          FileStorageService fileStorageService) {
        this.userService = userService;
        this.publicationService = publicationService;
        this.fileStorageService = fileStorageService;
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
     * POST /api/usuarios/me/foto
     * Sube la foto de perfil como multipart. Devuelve la URL completa.
     */
    @PostMapping("/me/foto")
    public ResponseEntity<?> uploadProfilePhoto(@RequestPart("foto") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo está vacío"));
        }

        // Validar que sea imagen
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solo se permiten archivos de imagen"));
        }

        // Validar tamaño (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "La imagen no puede superar 5MB"));
        }

        try {
            String email = getEmailFromToken();
            String relativePath = fileStorageService.saveProfileImage(file);
            String imageUrl = "/uploads/" + relativePath;

            // Actualizar el usuario con la nueva URL
            UserProfileResponse updated = userService.updateProfileImage(email, imageUrl);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al guardar la imagen"));
        }
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
