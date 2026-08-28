package com.ronda.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ronda.backend.config.JwtUtil;
import com.ronda.backend.dto.AuthResponse;
import com.ronda.backend.dto.LoginRequest;
import com.ronda.backend.dto.OtpRequest;
import com.ronda.backend.dto.OtpSendRequest;
import com.ronda.backend.dto.RegisterRequest;
import com.ronda.backend.model.User;
import com.ronda.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "test@mail.com";
    private static final String PASSWORD = "1234";
    private static final String HASH = "hashFalsoDePrueba";
    private static final String TOKEN = "token.de.prueba";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User usuarioExistente() {
        User user = new User("Juan Perez", EMAIL, HASH);
        user.setId(1L);
        return user;
    }

    // ---------------- login ----------------

    @Test
    void testLoginExitoso() {
        // Usuario existe y la password coincide: devuelve token
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioExistente()));
        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
        when(jwtUtil.generateToken(EMAIL)).thenReturn(TOKEN);

        AuthResponse response = authService.login(new LoginRequest(EMAIL, PASSWORD));

        assertTrue(response.isSuccess());
        assertEquals(TOKEN, response.getToken());
        assertEquals("Login exitoso", response.getMessage());
    }

    @Test
    void testLoginEmailInexistente() {
        // No existe el usuario: no se debe generar ningun token
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        AuthResponse response = authService.login(new LoginRequest(EMAIL, PASSWORD));

        assertFalse(response.isSuccess());
        assertNull(response.getToken());
        assertEquals("Usuario no encontrado", response.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testLoginPasswordIncorrecta() {
        // La password no matchea el hash: se rechaza sin emitir token
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioExistente()));
        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(false);

        AuthResponse response = authService.login(new LoginRequest(EMAIL, PASSWORD));

        assertFalse(response.isSuccess());
        assertNull(response.getToken());
        assertEquals("Contraseña incorrecta", response.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    // ---------------- register ----------------

    @Test
    void testRegisterExitoso() {
        // Email libre: se guarda el usuario con la password hasheada y un OTP
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASH);

        AuthResponse response = authService.register(
                new RegisterRequest("Juan Perez", EMAIL, PASSWORD));

        assertTrue(response.isSuccess());
        // Todavia no esta verificado, por eso no viaja token
        assertNull(response.getToken());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User guardado = captor.getValue();

        assertEquals(EMAIL, guardado.getEmail());
        assertEquals(HASH, guardado.getPassword());
        assertNotNull(guardado.getOtpCode());
        assertEquals(6, guardado.getOtpCode().length());
        assertNotNull(guardado.getOtpExpiresAt());
        assertFalse(guardado.isEmailVerified());
    }

    @Test
    void testRegisterEmailDuplicado() {
        // Email ya registrado: no se guarda nada
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        AuthResponse response = authService.register(
                new RegisterRequest("Juan Perez", EMAIL, PASSWORD));

        assertFalse(response.isSuccess());
        assertEquals("El email ya está registrado", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterNuncaGuardaLaPasswordEnTextoPlano() {
        // Chequeo de seguridad: lo persistido tiene que ser el hash
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASH);

        authService.register(new RegisterRequest("Juan Perez", EMAIL, PASSWORD));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertNotEquals(PASSWORD, captor.getValue().getPassword());
    }

    // ---------------- verifyOtp ----------------

    @Test
    void testVerifyOtpExitoso() {
        // Codigo correcto y vigente: verifica la cuenta, limpia el OTP y da token
        User user = usuarioExistente();
        user.setOtpCode("123456");
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(EMAIL)).thenReturn(TOKEN);

        AuthResponse response = authService.verifyOtp(new OtpRequest(EMAIL, "123456"));

        assertTrue(response.isSuccess());
        assertEquals(TOKEN, response.getToken());
        assertTrue(user.isEmailVerified());
        assertNull(user.getOtpCode());
        assertNull(user.getOtpExpiresAt());
        verify(userRepository).save(user);
    }

    @Test
    void testVerifyOtpCodigoIncorrecto() {
        // Codigo distinto al guardado: no verifica ni emite token
        User user = usuarioExistente();
        user.setOtpCode("123456");
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthResponse response = authService.verifyOtp(new OtpRequest(EMAIL, "999999"));

        assertFalse(response.isSuccess());
        assertEquals("Código incorrecto", response.getMessage());
        assertFalse(user.isEmailVerified());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyOtpCodigoExpirado() {
        // Codigo correcto pero vencido: se rechaza
        User user = usuarioExistente();
        user.setOtpCode("123456");
        user.setOtpExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthResponse response = authService.verifyOtp(new OtpRequest(EMAIL, "123456"));

        assertFalse(response.isSuccess());
        assertEquals("El código expiró. Solicitá uno nuevo.", response.getMessage());
        assertFalse(user.isEmailVerified());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyOtpUsuarioInexistente() {
        // Sin usuario no hay nada que verificar
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        AuthResponse response = authService.verifyOtp(new OtpRequest(EMAIL, "123456"));

        assertFalse(response.isSuccess());
        assertEquals("Usuario no encontrado", response.getMessage());
    }

    @Test
    void testVerifyOtpSinCodigoPendiente() {
        // El usuario nunca pidio un OTP (otpCode en null)
        User user = usuarioExistente();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthResponse response = authService.verifyOtp(new OtpRequest(EMAIL, "123456"));

        assertFalse(response.isSuccess());
        assertEquals("Código incorrecto", response.getMessage());
    }

    // ---------------- sendOtp / resendOtp ----------------

    @Test
    void testSendOtpGeneraCodigoNuevo() {
        // Asigna un OTP de 6 digitos con vencimiento y persiste
        User user = usuarioExistente();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthResponse response = authService.sendOtp(new OtpSendRequest(EMAIL));

        assertTrue(response.isSuccess());
        assertNull(response.getToken());
        assertEquals("Código enviado", response.getMessage());
        assertEquals(6, user.getOtpCode().length());
        assertTrue(user.getOtpExpiresAt().isAfter(LocalDateTime.now()));
        verify(userRepository).save(user);
    }

    @Test
    void testSendOtpUsuarioInexistente() {
        // Sin usuario no se genera ningun codigo
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        AuthResponse response = authService.sendOtp(new OtpSendRequest(EMAIL));

        assertFalse(response.isSuccess());
        assertEquals("Usuario no encontrado", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testResendOtpReemplazaElCodigoAnterior() {
        // El codigo viejo debe quedar invalidado por el nuevo
        User user = usuarioExistente();
        user.setOtpCode("111111");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthResponse response = authService.resendOtp(new OtpSendRequest(EMAIL));

        assertTrue(response.isSuccess());
        assertEquals("Código reenviado", response.getMessage());
        assertEquals(6, user.getOtpCode().length());
        verify(userRepository).save(user);
    }
}
