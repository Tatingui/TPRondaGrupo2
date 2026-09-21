package com.example.tprondagrupo2.data.repository;

import static org.junit.Assert.*;

import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.network.PublicationDetailApiService;
import com.example.tprondagrupo2.network.PublicationFavoriteApiService;
import com.example.tprondagrupo2.network.PublicationReadApiService;
import com.example.tprondagrupo2.network.PublicationWriteApiService;
import com.example.tprondagrupo2.support.FakeCall;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class PublicationRepositoryTest {

    @Test
    public void cargaDetalleConElIdSolicitado() {
        FakeCall<Publicacion> call = new FakeCall<>();
        Resultado<Publicacion> result = new Resultado<>();
        PublicationRepository repository = repository("getPublication", call);
        repository.getDetail("17", result);
        Publicacion publicacion = new Publicacion();
        publicacion.setId("17");
        call.respond(Response.success(publicacion));
        assertSame(publicacion, result.value);
        assertNull(result.error);
    }

    @Test
    public void distingueErroresHttpDelDetalle() {
        int[] codes = {401, 403, 404, 500};
        PublicationDetailSource.LoadError[] errors = {
                PublicationDetailSource.LoadError.UNAUTHORIZED, PublicationDetailSource.LoadError.FORBIDDEN,
                PublicationDetailSource.LoadError.NOT_FOUND, PublicationDetailSource.LoadError.SERVER};
        for (int i = 0; i < codes.length; i++) {
            FakeCall<Publicacion> call = new FakeCall<>();
            Resultado<Publicacion> result = new Resultado<>();
            repository("getPublication", call).getDetail("17", result);
            call.respond(Response.error(codes[i], ResponseBody.create(null, "{}")));
            assertEquals(errors[i], result.error);
        }
    }

    @Test
    public void cuerpoVacioNoEsUnDetalleValido() {
        FakeCall<Publicacion> call = new FakeCall<>();
        Resultado<Publicacion> result = new Resultado<>();
        repository("getPublication", call).getDetail("17", result);
        call.respond(Response.success(null));
        assertEquals(PublicationDetailSource.LoadError.SERVER, result.error);
    }

    @Test
    public void errorDeTransporteEsNetwork() {
        FakeCall<Publicacion> call = new FakeCall<>();
        Resultado<Publicacion> result = new Resultado<>();
        repository("getPublication", call).getDetail("17", result);
        call.fail(new java.io.IOException("offline"));
        assertEquals(PublicationDetailSource.LoadError.NETWORK, result.error);
    }

    @Test
    public void cancelacionNoEntregaResultadosNiErroresDeRed() {
        FakeCall<Publicacion> call = new FakeCall<>();
        Resultado<Publicacion> result = new Resultado<>();
        PublicationDetailSource.Request request = repository("getPublication", call).getDetail("17", result);
        request.cancel();
        call.respond(Response.success(new Publicacion()));
        call.fail(new java.io.IOException("Canceled"));
        assertTrue(call.isCanceled());
        assertEquals(0, result.entregas);
    }

    @Test
    public void preguntasVaciasSonUnaRespuestaValida() {
        FakeCall<List<Pregunta>> call = new FakeCall<>();
        Resultado<List<Pregunta>> result = new Resultado<>();
        repository("getQuestions", call).getQuestions("17", result);
        call.respond(Response.success(Collections.emptyList()));
        assertTrue(result.value.isEmpty());
        assertNull(result.error);
    }

    @Test
    public void visitaPuedeResponderSinCuerpo() {
        FakeCall<Void> call = new FakeCall<>();
        repository("recordView", call).recordView("17");
        assertTrue(call.isExecuted());
        call.respond(Response.success(204, null));
    }

    private PublicationRepository repository(String method, FakeCall<?> call) {
        PublicationReadApiService readApi = proxy(PublicationReadApiService.class, method, call);
        PublicationWriteApiService writeApi = proxy(PublicationWriteApiService.class, method, call);
        PublicationFavoriteApiService favoriteApi = proxy(PublicationFavoriteApiService.class, method, call);
        PublicationDetailApiService detailApi = proxy(PublicationDetailApiService.class, method, call);
        return new PublicationRepository(readApi, writeApi, favoriteApi, detailApi);
    }

    private <T> T proxy(Class<T> serviceType, String method, FakeCall<?> call) {
        return serviceType.cast(Proxy.newProxyInstance(
                serviceType.getClassLoader(), new Class<?>[]{serviceType},
                (proxy, invoked, args) -> {
                    if (!method.equals(invoked.getName())) {
                        throw new AssertionError("Método inesperado: " + invoked.getName());
                    }
                    assertEquals("17", args[0]);
                    return call;
                }));
    }

    private static class Resultado<T> implements PublicationDetailSource.Result<T> {
        T value;
        PublicationDetailSource.LoadError error;
        int entregas;
        @Override public void onSuccess(T value) { this.value = value; entregas++; }
        @Override public void onError(PublicationDetailSource.LoadError error) { this.error = error; entregas++; }
    }
}
