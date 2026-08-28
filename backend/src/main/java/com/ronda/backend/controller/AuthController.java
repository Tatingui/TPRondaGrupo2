package com.ronda.backend.controller;

import com.ronda.backend.dto.AuthResponse;
import com.ronda.backend.dto.LoginRequest;
import com.ronda.backend.dto.OtpRequest;
import com.ronda.backend.dto.OtpSendRequest;
import com.ronda.backend.dto.RegisterRequest;
import com.ronda.backend.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Con context-path=/api, estas rutas quedan en /api/auth/...
 * que es exactamente lo que declara AuthApiService en el cliente Android.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/otp/send")
    public ResponseEntity<AuthResponse> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/otp/resend")
    public ResponseEntity<AuthResponse> resendOtp(@Valid @RequestBody OtpSendRequest request) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }
}
