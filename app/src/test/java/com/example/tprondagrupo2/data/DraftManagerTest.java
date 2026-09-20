package com.example.tprondagrupo2.data;

import android.content.SharedPreferences;
import com.example.tprondagrupo2.model.PublicationCreateRequest;
import org.junit.Test;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Collections;
import static org.junit.Assert.*;

public class DraftManagerTest {
    @Test public void conservaDireccionEnLaMismaSesion() {
        SharedPreferences prefs = preferences();
        save(prefs);
        assertEquals("Av. Carabobo 27, Buenos Aires", new DraftManager(prefs, "session-a").loadDraft().getAddress());
    }

    @Test public void otraSesionOSinSesionNoRecibeDireccionPrivada() {
        SharedPreferences prefs = preferences();
        save(prefs);
        assertNull(new DraftManager(prefs, "session-b").loadDraft().getAddress());
        assertNull(new DraftManager(prefs, null).loadDraft().getAddress());
        assertEquals("Flores", new DraftManager(prefs, "session-b").loadDraft().getLocation());
    }

    @Test public void limpiarBorradorEliminaContenido() {
        SharedPreferences prefs = preferences();
        save(prefs);
        DraftManager manager = new DraftManager(prefs, "session-a");
        manager.clearDraft();
        assertNull(manager.loadDraft());
    }

    private void save(SharedPreferences prefs) {
        PublicationCreateRequest draft = new PublicationCreateRequest("Test", "Test", 100.0,
                "NEW", "Flores", 1L, Collections.emptyList());
        draft.setAddress("Av. Carabobo 27, Buenos Aires");
        new DraftManager(prefs, "session-a").saveDraft(draft);
    }

    private SharedPreferences preferences() {
        HashMap<String, String> values = new HashMap<>();
        SharedPreferences.Editor editor = (SharedPreferences.Editor) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{SharedPreferences.Editor.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "putString": values.put((String) args[0], (String) args[1]); return proxy;
                        case "remove": values.remove((String) args[0]); return proxy;
                        case "apply": return null;
                        default: throw new UnsupportedOperationException(method.getName());
                    }
                });
        return (SharedPreferences) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{SharedPreferences.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getString": return values.getOrDefault((String) args[0], (String) args[1]);
                        case "edit": return editor;
                        default: throw new UnsupportedOperationException(method.getName());
                    }
                });
    }
}
