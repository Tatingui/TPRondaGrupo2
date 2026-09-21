package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.OfferRespondRequest;
import com.example.tprondagrupo2.model.OperacionHistorial;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.network.HistorialApiService;
import com.example.tprondagrupo2.network.OfferApiService;
import com.example.tprondagrupo2.network.PublicationReadApiService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para operaciones de ofertas (punto 7 del TPO).
 *
 * Encapsula las llamadas a OfferApiService, HistorialApiService (para aceptar)
 * y PublicationReadApiService (para navegar al detalle desde una oferta).
 * Los ViewModels consumen este repositorio en lugar de los ApiServices directos.
 */
@Singleton
public class OfferRepository {

    private final OfferApiService offerApiService;
    private final HistorialApiService historialApiService;
    private final PublicationReadApiService publicationReadApiService;

    @Inject
    public OfferRepository(OfferApiService offerApiService,
                           HistorialApiService historialApiService,
                           PublicationReadApiService publicationReadApiService) {
        this.offerApiService = offerApiService;
        this.historialApiService = historialApiService;
        this.publicationReadApiService = publicationReadApiService;
    }

    // ── Listado de ofertas ──

    public Call<List<Offer>> getSentOffers() {
        return offerApiService.getSentOffers();
    }

    public Call<List<Offer>> getReceivedOffers() {
        return offerApiService.getReceivedOffers();
    }

    public void fetchOffers(boolean received, RepoCallback<List<Offer>> callback) {
        Call<List<Offer>> call = received ? getReceivedOffers() : getSentOffers();
        call.enqueue(new Callback<List<Offer>>() {
            @Override
            public void onResponse(@NonNull Call<List<Offer>> c,
                                   @NonNull Response<List<Offer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error cargando ofertas: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Offer>> c, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    // ── Acciones sobre ofertas ──

    public void acceptOffer(Long offerId, RepoCallback<OperacionHistorial> callback) {
        historialApiService.acceptOffer(offerId).enqueue(new Callback<OperacionHistorial>() {
            @Override
            public void onResponse(@NonNull Call<OperacionHistorial> c,
                                   @NonNull Response<OperacionHistorial> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al aceptar oferta");
                }
            }

            @Override
            public void onFailure(@NonNull Call<OperacionHistorial> c, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    public void rejectOffer(Long offerId, RepoCallback<Offer> callback) {
        OfferRespondRequest request = new OfferRespondRequest("REJECTED", null);
        offerApiService.respondOffer(offerId, request).enqueue(new Callback<Offer>() {
            @Override
            public void onResponse(@NonNull Call<Offer> c,
                                   @NonNull Response<Offer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al rechazar oferta");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Offer> c, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    public void sendCounterOffer(Long offerId, double newPrice, RepoCallback<Offer> callback) {
        OfferRespondRequest request = new OfferRespondRequest("COUNTER_OFFER", newPrice);
        offerApiService.respondOffer(offerId, request).enqueue(new Callback<Offer>() {
            @Override
            public void onResponse(@NonNull Call<Offer> c,
                                   @NonNull Response<Offer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al enviar contra-oferta");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Offer> c, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    // ── Consulta de publicación (para navegar al detalle) ──

    public void fetchPublication(Long publicationId, RepoCallback<Publicacion> callback) {
        if (publicationId == null) {
            callback.onError("ID de publicación inválido");
            return;
        }
        publicationReadApiService.getPublication(String.valueOf(publicationId))
                .enqueue(new Callback<Publicacion>() {
                    @Override
                    public void onResponse(@NonNull Call<Publicacion> c,
                                           @NonNull Response<Publicacion> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("No se pudo abrir la publicación (HTTP " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Publicacion> c, @NonNull Throwable t) {
                        callback.onNetworkError();
                    }
                });
    }
}
