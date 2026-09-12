package com.ronda.backend.controller;

import com.ronda.backend.dto.UserProfileResponse;
import com.ronda.backend.dto.UserProfileUpdateRequest;
import com.ronda.backend.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
            @RequestBody UserProfileUpdateRequest request) {
        String email = getEmailFromToken();
        UserProfileResponse updated = userService.updateProfile(email, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    private String getEmailFromToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
