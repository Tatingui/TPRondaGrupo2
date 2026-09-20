package com.example.tprondagrupo2.ui.detalle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.ActivityNotFoundException;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class AndroidMapaLauncherTest {

    @Test
    public void convierteUriYPaqueteEnIntentView() {
        AtomicReference<Intent> recibido = new AtomicReference<>();
        AndroidMapaLauncher launcher = new AndroidMapaLauncher(recibido::set);

        assertTrue(launcher.abrir("https://www.google.com/maps/dir/?api=1&destination=Calle%201", "com.google.android.apps.maps"));
        assertEquals(Intent.ACTION_VIEW, recibido.get().getAction());
        assertEquals("Calle 1", recibido.get().getData().getQueryParameter("destination"));
        assertEquals("com.google.android.apps.maps", recibido.get().getPackage());

        assertTrue(launcher.abrir("geo:0,0?q=Calle%201", null));
        assertEquals("geo", recibido.get().getData().getScheme());
        assertNull(recibido.get().getPackage());
    }

    @Test
    public void devuelveFalseCuandoAndroidNoEncuentraUnaApp() {
        AndroidMapaLauncher launcher = new AndroidMapaLauncher(intent -> {
            throw new ActivityNotFoundException();
        });
        assertFalse(launcher.abrir("geo:0,0?q=Calle%201", null));
    }
}
