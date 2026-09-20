package com.example.tprondagrupo2.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.Observer;

import com.example.tprondagrupo2.support.ImmediateMainThreadRule;

import org.junit.Rule;
import org.junit.Test;

public class NetworkObserverTest {
    @Rule public final ImmediateMainThreadRule mainThread = new ImmediateMainThreadRule();

    @Test
    public void consultaPuntualNoRegistraYReflejaEstadoActual() {
        FakeSource source = new FakeSource();
        NetworkObserver observer = new NetworkObserver(source);
        assertFalse(observer.isCurrentlyConnected());
        source.connected = true;
        assertTrue(observer.isCurrentlyConnected());
        assertEquals(0, source.starts);
    }

    @Test
    public void registraUnaVezYLiberaAlSalirElUltimoObservador() {
        FakeSource source = new FakeSource();
        NetworkObserver observer = new NetworkObserver(source);
        Observer<Boolean> primera = value -> { };
        Observer<Boolean> segunda = value -> { };
        observer.getIsConnected().observeForever(primera);
        observer.getIsConnected().observeForever(segunda);
        assertEquals(1, source.starts);

        observer.getIsConnected().removeObserver(primera);
        assertEquals(0, source.stops);
        observer.getIsConnected().removeObserver(segunda);
        assertEquals(1, source.stops);

        source.connected = true;
        observer.getIsConnected().observeForever(primera);
        assertEquals(2, source.starts);
        assertEquals(Boolean.TRUE, observer.getIsConnected().getValue());
        observer.getIsConnected().removeObserver(primera);
        assertEquals(2, source.stops);
    }

    @Test
    public void alCambiarLaRedReconsultaElEstadoEnVezDeSuponerConectividad() {
        FakeSource source = new FakeSource();
        NetworkObserver observer = new NetworkObserver(source);
        Observer<Boolean> vista = value -> { };
        observer.getIsConnected().observeForever(vista);
        source.connected = true;
        source.listener.run();
        assertEquals(Boolean.TRUE, observer.getIsConnected().getValue());
        source.connected = false;
        source.listener.run();
        assertEquals(Boolean.FALSE, observer.getIsConnected().getValue());
        observer.getIsConnected().removeObserver(vista);
    }

    private static class FakeSource implements NetworkObserver.Source {
        int starts;
        int stops;
        boolean connected;
        Runnable listener;
        @Override public boolean isConnected() { return connected; }
        @Override public void start(Runnable listener) { this.listener = listener; starts++; }
        @Override public void stop() { listener = null; stops++; }
    }
}
