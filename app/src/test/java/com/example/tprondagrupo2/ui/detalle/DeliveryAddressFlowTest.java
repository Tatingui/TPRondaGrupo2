package com.example.tprondagrupo2.ui.detalle;

import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.PublicationCreateRequest;
import com.example.tprondagrupo2.ui.publish.DeliveryAddress;
import com.google.gson.Gson;
import org.junit.Test;
import java.util.Collections;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class DeliveryAddressFlowTest {
    private static final String ADDRESS = "Av. Carabobo 27, C1406DGA Ciudad Autónoma de Buenos Aires";

    @Test public void conservaDireccionEnJsonDePublicacionYBorradorSinInventarCoordenadas() {
        Gson gson = new Gson();
        PublicationCreateRequest request = new PublicationCreateRequest("Test", "Test", 100.0,
                "NEW", "Flores", 1L, Collections.emptyList());
        request.setAddress(ADDRESS);
        String payload = gson.toJson(request);
        PublicationCreateRequest restored = gson.fromJson(payload, PublicationCreateRequest.class);
        assertEquals(ADDRESS, restored.getAddress());
        assertEquals("Flores", restored.getLocation());
        assertFalse(payload.contains("latitude"));
        assertFalse(payload.contains("longitude"));
        assertNull(gson.fromJson("{\"location\":\"Flores\"}", PublicationCreateRequest.class).getAddress());
    }

    @Test public void destinoAutorizadoUsaTextoCompletoCopiadoDeMaps() {
        Publicacion publication = new Gson().fromJson("{\"addressVisible\":true,\"owner\":false,\"state\":\"SOLD\",\"address\":\"" + ADDRESS + "\"}", Publicacion.class);
        String destination = new ComoLlegarResolver().resolver(publication);
        assertEquals(ADDRESS, destination);
        ArrayList<String> uris = new ArrayList<>();
        new MapaNavigator((uri, app) -> { uris.add(uri); return true; }).abrir(destination);
        assertTrue(uris.get(0).contains("Av.%20Carabobo%2027"));
        assertTrue(uris.get(0).contains("Buenos%20Aires"));
    }

    @Test public void pideTextoPostalNoUnEnlace() {
        assertTrue(DeliveryAddress.isValid(ADDRESS));
        assertFalse(DeliveryAddress.isValid(null));
        assertFalse(DeliveryAddress.isValid("  "));
        assertFalse(DeliveryAddress.isValid("https://maps.app.goo.gl/test"));
        assertFalse(DeliveryAddress.isValid("www.google.com/maps"));
        assertFalse(DeliveryAddress.isValid(String.join("", Collections.nCopies(256, "a"))));
    }
}
