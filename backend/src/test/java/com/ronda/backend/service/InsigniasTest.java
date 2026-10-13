package com.ronda.backend.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsigniasTest {

    @Test
    public void otorgarInsigniaVecinoSolidarioCon5Operaciones() {
        List<String> insignias = UserService.calcularInsignias(3, 2, 4.0);

        assertEquals(1, insignias.size());
        assertTrue(insignias.contains("Vecino solidario"));
    }

    @Test
    public void otorgarDobleInsigniaCon5OperacionesYExcelenteReputacion() {
        List<String> insignias = UserService.calcularInsignias(4, 2, 4.8);

        assertEquals(2, insignias.size());
        assertTrue(insignias.contains("Vecino solidario"));
        assertTrue(insignias.contains("Excelente reputación"));
    }

    @Test
    public void sinInsigniasSiTieneMenosDeCincoOperaciones() {
        List<String> insignias = UserService.calcularInsignias(2, 1, 4.0);

        assertTrue(insignias.isEmpty());
    }
}
