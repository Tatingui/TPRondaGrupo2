package com.example.tprondagrupo2.ui.detalle;

import static org.junit.Assert.assertEquals;

import com.example.tprondagrupo2.model.Publicacion;
import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ComoLlegarResolverTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> casos() {
        return Arrays.asList(new Object[][] {
                {"sin publicación", "null", null},
                {"sin permiso explícito", "{\"address\":\"Calle 1\"}", null},
                {"oculta con coordenadas", "{\"addressVisible\":false,\"latitude\":1,\"longitude\":2}", null},
                {"propietario", "{\"owner\":true,\"addressVisible\":true,\"address\":\"Calle 1\"}", null},
                {"activa autorizada", datos("\"state\":\"ACTIVE\",\"address\":\"Calle 1\""), "Calle 1"},
                {"vendida autorizada", datos("\"state\":\"SOLD\",\"address\":\"Calle 1\""), "Calle 1"},
                {"pausada autorizada", datos("\"state\":\"PAUSED\",\"address\":\"Calle 1\""), "Calle 1"},
                {"coordenadas prioritarias", datos("\"address\":\"Calle 1\",\"latitude\":-34.588,\"longitude\":-58.411"), "-34.588,-58.411"},
                {"punto cero válido", datos("\"latitude\":0,\"longitude\":0"), "0.0,0.0"},
                {"límites positivos", datos("\"latitude\":90,\"longitude\":180"), "90.0,180.0"},
                {"límites negativos", datos("\"latitude\":-90,\"longitude\":-180"), "-90.0,-180.0"},
                {"latitud fuera de rango", datos("\"latitude\":90.1,\"longitude\":1,\"address\":\"Calle 1\""), "Calle 1"},
                {"longitud fuera de rango", datos("\"latitude\":1,\"longitude\":-180.1,\"address\":\"Calle 1\""), "Calle 1"},
                {"latitud incompleta", datos("\"latitude\":1,\"address\":\"Calle 1\""), "Calle 1"},
                {"longitud incompleta", datos("\"longitude\":1,\"address\":\"Calle 1\""), "Calle 1"},
                {"NaN", datos("\"latitude\":\"NaN\",\"longitude\":1,\"address\":\"Calle 1\""), "Calle 1"},
                {"infinito", datos("\"latitude\":1,\"longitude\":\"Infinity\",\"address\":\"Calle 1\""), "Calle 1"},
                {"dirección recortada", datos("\"address\":\"  Av. Santa Fe 3253  \""), "Av. Santa Fe 3253"},
                {"dirección vacía", datos("\"address\":\"   \""), null},
                {"sin destino", datos("\"address\":null"), null},
                {"coordenadas inválidas sin dirección", datos("\"latitude\":200,\"longitude\":1"), null}
        });
    }

    private static String datos(String campos) {
        return "{\"addressVisible\":true," + campos + "}";
    }

    private final String json;
    private final String esperado;

    public ComoLlegarResolverTest(String nombre, String json, String esperado) {
        this.json = json;
        this.esperado = esperado;
    }

    @Test
    public void resuelveSoloUnDestinoAutorizadoYValido() {
        Publicacion publicacion = new Gson().fromJson(json, Publicacion.class);
        assertEquals(esperado, new ComoLlegarResolver().resolver(publicacion));
    }
}
