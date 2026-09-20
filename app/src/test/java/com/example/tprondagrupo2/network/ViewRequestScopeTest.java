package com.example.tprondagrupo2.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.tprondagrupo2.support.FakeCall;

import org.junit.Test;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewRequestScopeTest {

    @Test
    public void cerrarCancelaPendientesEIgnoraRespuestaYErrorTardios() {
        ViewRequestScope scope = new ViewRequestScope();
        FakeCall<String> primera = new FakeCall<>();
        FakeCall<String> segunda = new FakeCall<>();
        Resultado resultado = new Resultado();
        scope.enqueue(primera, resultado);
        scope.enqueue(segunda, resultado);

        scope.close();
        scope.close();
        primera.respond(Response.success("vieja"));
        segunda.fail(new RuntimeException());

        assertEquals(1, primera.cancelCount);
        assertEquals(1, segunda.cancelCount);
        assertEquals(0, resultado.entregas);
    }

    @Test
    public void noCancelaOperacionesTerminadas() {
        ViewRequestScope scope = new ViewRequestScope();
        FakeCall<String> call = new FakeCall<>();
        Resultado resultado = new Resultado();
        scope.enqueue(call, resultado);
        call.respond(Response.success("ok"));
        scope.close();

        assertEquals(1, resultado.entregas);
        assertFalse(call.isCanceled());
    }

    @Test
    public void scopesDeOtrasVistasSiguenFuncionando() {
        ViewRequestScope anterior = new ViewRequestScope();
        ViewRequestScope actual = new ViewRequestScope();
        FakeCall<String> call = new FakeCall<>();
        Resultado resultado = new Resultado();
        actual.enqueue(call, resultado);
        anterior.close();
        call.fail(new RuntimeException());

        assertEquals(1, resultado.entregas);
        assertFalse(call.isCanceled());
    }

    @Test
    public void scopeCerradoNoIniciaMasSolicitudes() {
        ViewRequestScope scope = new ViewRequestScope();
        scope.close();
        FakeCall<String> call = new FakeCall<>();
        scope.enqueue(call, new Resultado());

        assertTrue(call.isCanceled());
        assertFalse(call.isExecuted());
    }

    private static class Resultado implements Callback<String> {
        int entregas;
        @Override public void onResponse(Call<String> call, Response<String> response) { entregas++; }
        @Override public void onFailure(Call<String> call, Throwable error) { entregas++; }
    }
}
