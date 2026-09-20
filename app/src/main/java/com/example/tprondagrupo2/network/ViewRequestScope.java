package com.example.tprondagrupo2.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Solicitudes de una vista. Usar y cerrar en el hilo principal, igual que sus callbacks. */
public final class ViewRequestScope {
    private final Set<Call<?>> pendientes = new HashSet<>();
    private boolean cerrado;

    public boolean hasPending() { return !pendientes.isEmpty(); }

    public <T> void enqueue(Call<T> call, Callback<T> callback) {
        if (cerrado) {
            call.cancel();
            return;
        }
        pendientes.add(call);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (pendientes.remove(call) && !cerrado && !call.isCanceled()) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable error) {
                if (pendientes.remove(call) && !cerrado && !call.isCanceled()) {
                    callback.onFailure(call, error);
                }
            }
        });
    }

    public void close() {
        cerrado = true;
        ArrayList<Call<?>> cancelar = new ArrayList<>(pendientes);
        pendientes.clear();
        for (Call<?> call : cancelar) call.cancel();
    }
}
