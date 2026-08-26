package com.ronda.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RondaBackendApplicationTest {

    @Test
    void mainClassExists() {
        // Verifica que la clase principal existe y no lanza excepcion al instanciarla.
        // No usamos @SpringBootTest a proposito: levantaria el contexto completo
        // y necesitaria MySQL corriendo.
        RondaBackendApplication app = new RondaBackendApplication();
        assertNotNull(app);
    }
}
