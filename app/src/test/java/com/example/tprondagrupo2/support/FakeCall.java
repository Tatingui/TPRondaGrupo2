package com.example.tprondagrupo2.support;

import java.io.IOException;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Permite entregar respuestas tardías incluso después de cancelar, sin usar la red. */
public final class FakeCall<T> implements Call<T> {
    private Callback<T> callback;
    private boolean canceled;
    public int cancelCount;

    public void respond(Response<T> response) { callback.onResponse(this, response); }
    public void fail(Throwable error) { callback.onFailure(this, error); }

    @Override public void enqueue(Callback<T> callback) { this.callback = callback; }
    @Override public boolean isExecuted() { return callback != null; }
    @Override public void cancel() { canceled = true; cancelCount++; }
    @Override public boolean isCanceled() { return canceled; }
    @Override public Call<T> clone() { return new FakeCall<>(); }
    @Override public Timeout timeout() { return Timeout.NONE; }
    @Override public Request request() { throw new UnsupportedOperationException(); }
    @Override public Response<T> execute() throws IOException { throw new UnsupportedOperationException(); }
}
