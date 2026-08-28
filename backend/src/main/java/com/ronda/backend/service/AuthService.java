package com.ronda.backend.service;

import com.ronda.backend.config.JwtUtil;
import com.ronda.backend.dto.AuthResponse;
import com.ronda.backend.dto.LoginRequest;
import com.ronda.backend.dto.OtpRequest;
import com.ronda.backend.dto.OtpSendRequest;
import com.ronda.backend.dto.RegisterRequest;
import com.ronda.backend.model.User;
import com.ronda.backend.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int OTP_VIGENCIA_MINUTOS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> encontrado = userRepository.findByEmail(request.getEmail());
        if (encontrado.isEmpty()) {
            return AuthResponse.error("Usuario no encontrado");
        }

        User user = encontrado.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AuthResponse.error("Contraseña incorrecta");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.ok(token, "Login exitoso");
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.error("El email ya está registrado");
        }

        User user = new User(
                request.getNombre(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()));

        asignarNuevoOtp(user);
        userRepository.save(user);

        // El token queda en null a proposito: la cuenta todavia no esta verificada
        return AuthResponse.ok(null, "Registro exitoso. Se envió el código de verificación.");
    }

    public AuthResponse sendOtp(OtpSendRequest request) {
        return regenerarOtp(request.getEmail(), "Código enviado");
    }

    public AuthResponse resendOtp(OtpSendRequest request) {
        return regenerarOtp(request.getEmail(), "Código reenviado");
    }

    public AuthResponse verifyOtp(OtpRequest request) {
        Optional<User> encontrado = userRepository.findByEmail(request.getEmail());
        if (encontrado.isEmpty()) {
            return AuthResponse.error("Usuario no encontrado");
        }

        User user = encontrado.get();

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getCode())) {
            return AuthResponse.error("Código incorrecto");
        }

        if (user.getOtpExpiresAt() == null
                || !user.getOtpExpiresAt().isAfter(LocalDateTime.now())) {
            return AuthResponse.error("El código expiró. Solicitá uno nuevo.");
        }

        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.ok(token, "Verificación exitosa");
    }

    /**
     * Busca el usuario, le asigna un OTP nuevo y lo persiste.
     * sendOtp y resendOtp comparten todo salvo el mensaje de respuesta.
     */
    private AuthResponse regenerarOtp(String email, String mensajeExito) {
        Optional<User> encontrado = userRepository.findByEmail(email);
        if (encontrado.isEmpty()) {
            return AuthResponse.error("Usuario no encontrado");
        }

        User user = encontrado.get();
        asignarNuevoOtp(user);
        userRepository.save(user);

        return AuthResponse.ok(null, mensajeExito);
    }

    private void asignarNuevoOtp(User user) {
        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(OTP_VIGENCIA_MINUTOS));

        // No hay envio real de mail ni SMS: el codigo se lee de la consola
        log.info("========== OTP para {}: {} ==========", user.getEmail(), otp);
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
