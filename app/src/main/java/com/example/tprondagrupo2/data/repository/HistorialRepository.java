package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.model.Calificacion;
import com.example.tprondagrupo2.model.CalificacionRequest;
import com.example.tprondagrupo2.model.OperacionHistorial;
import com.example.tprondagrupo2.network.HistorialApiService;
import com.example.tprondagrupo2.network.HistorialPageResponse;

import java.util.List;

import javax.inject.Inject;

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

    /** Callback para resultados paginados del historial de transacciones. */
    public interface HistorialCallback {
        void onSuccess(List<OperacionHistorial> operaciones, int totalPages, boolean isLast);
        void onError(String message);
        void onNetworkError();
    }

    /** Callback para resultados de operaciones sobre una transacción individual. */
    public interface OperacionCallback {
        void onSuccess(OperacionHistorial operacion);
        void onError(String message);
        void onNetworkError();
    }

    /** Callback para el envío o consulta de calificaciones. */
    public interface CalificacionCallback {
        void onSuccess(Calificacion calificacion);
        void onEmpty();          // No calificó aún (204)
        void onError(String message);
        void onNetworkError();
    }

    /** Callback para operaciones simples sin valor de retorno. */
    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
        void onNetworkError();
    }

    private final HistorialApiService apiService;

    @Inject
    public HistorialRepository(HistorialApiService apiService) {
        this.apiService = apiService;
    }

    // ── Gestión de ofertas ──

    /**
     * Acepta una oferta recibida para una publicación.
     *
     * @param offerId Identificador de la oferta.
     * @param callback Callback de respuesta.
     */
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

    /**
     * Rechaza una oferta recibida para una publicación.
     *
     * @param offerId Identificador de la oferta.
     * @param callback Callback de respuesta.
     */
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

    /**
     * Confirma la entrega de una transacción concretada.
     *
     * @param transactionId Identificador de la transacción.
     * @param callback Callback de respuesta.
     */
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

    /**
     * Obtiene el historial de operaciones paginado según tipo (COMPRA o VENTA).
     *
     * @param tipo Tipo de operación ("COMPRA" o "VENTA").
     * @param page Número de página.
     * @param size Tamaño de página.
     * @param callback Callback con la lista de operaciones.
     */
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

    /**
     * Obtiene el historial de operaciones filtrado por rango de fechas ISO ("from" y "to").
     *
     * @param tipo Tipo de operación ("COMPRA" o "VENTA").
     * @param from Fecha límite inferior en ISO format.
     * @param to Fecha límite superior en ISO format.
     * @param page Número de página.
     * @param size Tamaño de página.
     * @param callback Callback con la lista de operaciones filtradas.
     */
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

    /**
     * Envía una calificación para la contraparte de una transacción concretada.
     *
     * @param transactionId Identificador de la transacción.
     * @param request Datos de la calificación (estrellas y comentario opcional).
     * @param callback Callback de respuesta.
     */
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

    /**
     * Consulta la calificación enviada por el usuario autenticado para una transacción específica.
     *
     * @param transactionId Identificador de la transacción.
     * @param callback Callback de respuesta.
     */
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

    /**
     * Extrae el mensaje de error del cuerpo de la respuesta HTTP.
     */
    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (Exception ignored) { }
        return "Error " + response.code();
    }
}
