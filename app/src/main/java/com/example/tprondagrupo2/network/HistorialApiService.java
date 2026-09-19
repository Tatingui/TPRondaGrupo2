package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.Calificacion;
import com.example.tprondagrupo2.model.CalificacionRequest;
import com.example.tprondagrupo2.model.OperacionHistorial;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interfaz Retrofit para operaciones del punto 9 (Historial y Calificaciones).
 * Todos los endpoints requieren JWT (el interceptor de OkHttpClient lo agrega).
 */
public interface HistorialApiService {

    // ── Gestión de ofertas ──

    @PUT("transactions/offers/{offerId}/accept")
    Call<OperacionHistorial> acceptOffer(@Path("offerId") Long offerId);

    @PUT("transactions/offers/{offerId}/reject")
    Call<Void> rejectOffer(@Path("offerId") Long offerId);

    // ── Entrega ──

    @POST("transactions/{id}/delivery")
    Call<OperacionHistorial> confirmDelivery(@Path("id") Long transactionId);

    // ── Historial ──

    @GET("transactions/history")
    Call<HistorialPageResponse> getHistory(
            @Query("tipo") String tipo,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("transactions/history")
    Call<HistorialPageResponse> getHistoryWithDates(
            @Query("tipo") String tipo,
            @Query("from") String from,
            @Query("to") String to,
            @Query("page") int page,
            @Query("size") int size
    );

    // ── Calificaciones ──

    @POST("transactions/{id}/rating")
    Call<Calificacion> rate(@Path("id") Long transactionId, @Body CalificacionRequest request);

    @GET("transactions/{id}/rating")
    Call<Calificacion> getMyRating(@Path("id") Long transactionId);
}
