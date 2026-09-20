package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.support.ImmediateMainThreadRule;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class SessionManagerTest {
    @Rule public final ImmediateMainThreadRule mainThread = new ImmediateMainThreadRule();

    @Test public void notificaYPermiteConsumirElEventoDeSesionExpirada() {
        SessionManager manager = new SessionManager();
        assertNull(manager.onSessionExpired().getValue());
        manager.notifySessionExpired();
        assertEquals(Boolean.TRUE, manager.onSessionExpired().getValue());
        manager.clearExpiredFlag();
        assertEquals(Boolean.FALSE, manager.onSessionExpired().getValue());
    }

    @Test public void instanciasDeTestNoCompartenEstadoGlobal() {
        SessionManager first = new SessionManager();
        SessionManager second = new SessionManager();
        first.notifySessionExpired();
        assertNull(second.onSessionExpired().getValue());
    }
}
