package com.ronda.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    // Clave de al menos 32 bytes, requisito de HMAC-SHA256
    private static final String SECRET =
            "ClaveDePruebaParaLosTestsDeJwtUtilConLargoSuficiente1234567890";
    private static final long UN_DIA_MS = 86_400_000L;

    private static final String EMAIL = "test@mail.com";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // El constructor recibe los valores que en produccion inyecta @Value,
        // asi no hace falta levantar el contexto de Spring ni usar reflection
        jwtUtil = new JwtUtil(SECRET, UN_DIA_MS);
    }

    @Test
    void testGenerateTokenDevuelveUnStringNoVacio() {
        // Valida que se genera un token con las tres partes del formato JWT
        String token = jwtUtil.generateToken(EMAIL);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void testGetEmailFromTokenDevuelveElEmailOriginal() {
        // El email viaja como subject y tiene que volver intacto
        String token = jwtUtil.generateToken(EMAIL);

        assertEquals(EMAIL, jwtUtil.getEmailFromToken(token));
    }

    @Test
    void testValidateTokenDevuelveTrueParaTokenValido() {
        // Un token recien generado con la misma clave es valido
        String token = jwtUtil.generateToken(EMAIL);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateTokenDevuelveFalseParaBasura() {
        // Un string que ni siquiera tiene formato JWT
        assertFalse(jwtUtil.validateToken("esto-no-es-un-token"));
    }

    @Test
    void testValidateTokenDevuelveFalseParaTokenAdulterado() {
        // Cambiar un caracter del payload invalida la firma
        String token = jwtUtil.generateToken(EMAIL);
        String adulterado = token.substring(0, token.length() - 3) + "aaa";

        assertFalse(jwtUtil.validateToken(adulterado));
    }

    @Test
    void testValidateTokenDevuelveFalseParaTokenFirmadoConOtraClave() {
        // Un token valido pero emitido por otro servidor no debe aceptarse
        JwtUtil otroServidor = new JwtUtil(
                "OtraClaveCompletamenteDistintaConLargoSuficiente1234567890", UN_DIA_MS);
        String tokenAjeno = otroServidor.generateToken(EMAIL);

        assertFalse(jwtUtil.validateToken(tokenAjeno));
    }

    @Test
    void testValidateTokenDevuelveFalseParaTokenExpirado() {
        // Expiracion negativa: el token nace vencido
        JwtUtil vencido = new JwtUtil(SECRET, -1000L);
        String token = vencido.generateToken(EMAIL);

        assertFalse(jwtUtil.validateToken(token));
    }
}
