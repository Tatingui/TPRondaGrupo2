package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.model.Calificacion;
import com.example.tprondagrupo2.model.CalificacionRequest;
import com.example.tprondagrupo2.model.OperacionHistorial;
import com.example.tprondagrupo2.network.HistorialApiService;
import com.example.tprondagrupo2.network.HistorialPageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para operaciones del historial y calificaciones (punto 9).
 *
 * Mismo patrón que UserRepository: callbacks por operación, sin conocimiento
 * de la UI. Los Fragments/ViewModels consumen este repositorio.
 */
public class HistorialRepository {

    // ── Callbacks ──

    public interface HistorialCallback {
        void onSuccess(List<OperacionHistorial> operaciones, int totalPages, boolean isLast);
        void onError(String message);
        void onNetworkError();
    }

    public interface OperacionCallback {
        void onSuccess(OperacionHistorial operacion);
        void onError(String message);
        void onNetworkError();
    }

    public interface CalificacionCallback {
        void onSuccess(Calificacion calificacion);
        void onEmpty();          // No calificó aún (204)
        void onError(String message);
        void onNetworkError();
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
        void onNetworkError();
    }

    private final HistorialApiService apiService;

    public HistorialRepository(HistorialApiService apiService) {
        this.apiService = apiService;
    }

    // ── Gestión de ofertas ──

    public void acceptOffer(Long offerId, OperacionCallback callback) {
        apiService.acceptOffer(offerId).enqueue(new Callback<OperacionHistorial>() {
            @Override
            public void onResponse(@NonNull Call<OperacionHistorial> call,
                                   @NonNull Response<OperacionHistorial> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<OperacionHistorial> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    public void rejectOffer(Long offerId, SimpleCallback callback) {
        apiService.rejectOffer(offerId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    // ── Entrega ──

    public void confirmDelivery(Long transactionId, OperacionCallback callback) {
        apiService.confirmDelivery(transactionId).enqueue(new Callback<OperacionHistorial>() {
            @Override
            public void onResponse(@NonNull Call<OperacionHistorial> call,
                                   @NonNull Response<OperacionHistorial> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<OperacionHistorial> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    // ── Historial ──

    public void getHistory(String tipo, int page, int size, HistorialCallback callback) {
        apiService.getHistory(tipo, page, size).enqueue(new Callback<HistorialPageResponse>() {
            @Override
            public void onResponse(@NonNull Call<HistorialPageResponse> call,
                                   @NonNull Response<HistorialPageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HistorialPageResponse body = response.body();
                    callback.onSuccess(body.getContent(), body.getTotalPages(), body.isLast());
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistorialPageResponse> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    public void getHistoryWithDates(String tipo, String from, String to,
                                    int page, int size, HistorialCallback callback) {
        apiService.getHistoryWithDates(tipo, from, to, page, size)
                .enqueue(new Callback<HistorialPageResponse>() {
            @Override
            public void onResponse(@NonNull Call<HistorialPageResponse> call,
                                   @NonNull Response<HistorialPageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HistorialPageResponse body = response.body();
                    callback.onSuccess(body.getContent(), body.getTotalPages(), body.isLast());
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<HistorialPageResponse> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    // ── Calificaciones ──

    public void rate(Long transactionId, CalificacionRequest request, CalificacionCallback callback) {
        apiService.rate(transactionId, request).enqueue(new Callback<Calificacion>() {
            @Override
            public void onResponse(@NonNull Call<Calificacion> call,
                                   @NonNull Response<Calificacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Calificacion> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    public void getMyRating(Long transactionId, CalificacionCallback callback) {
        apiService.getMyRating(transactionId).enqueue(new Callback<Calificacion>() {
            @Override
            public void onResponse(@NonNull Call<Calificacion> call,
                                   @NonNull Response<Calificacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 204) {
                    callback.onEmpty();
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Calificacion> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    // ── Helper ──

    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (Exception ignored) { }
        return "Error " + response.code();
    }
}
