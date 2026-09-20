package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.network.PublicationApiService;
import com.example.tprondagrupo2.network.PublicationPageResponse;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para operaciones de publicaciones y favoritos.
 *
 * Patron Repository: los Fragments no llaman a Retrofit directamente.
 */
public class PublicationRepository {

    public interface FavoritesCallback {
        void onSuccess(List<Publicacion> favorites);
        void onError(String message);
        void onNetworkError();
    }

    public interface ToggleFavoriteCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface PublicationPageCallback {
        void onSuccess(PublicationPageResponse page);
        void onError(String message);
        void onNetworkError();
    }

    private final PublicationApiService apiService;

    @Inject
    public PublicationRepository(PublicationApiService apiService) {
        this.apiService = apiService;
    }

    public void getFavorites(FavoritesCallback callback) {
        apiService.getFavorites().enqueue(new Callback<List<Publicacion>>() {
            @Override
            public void onResponse(@NonNull Call<List<Publicacion>> call,
                                   @NonNull Response<List<Publicacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error cargando favoritos");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Publicacion>> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    public void toggleFavorite(String pubId, boolean currentlyFavorite, ToggleFavoriteCallback callback) {
        Call<Void> call = currentlyFavorite
                ? apiService.unmarkAsFavorite(pubId)
                : apiService.markAsFavorite(pubId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al cambiar favorito");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError("Error de conexion");
            }
        });
    }

    public void getPublications(String search, Long categoryId, Double minPrice, Double maxPrice,
                                String status, String location, int page, int size, String sort,
                                PublicationPageCallback callback) {
        apiService.getPublications(search, categoryId, minPrice, maxPrice, status, location, page, size, sort)
                .enqueue(new Callback<PublicationPageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<PublicationPageResponse> call,
                                           @NonNull Response<PublicationPageResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Error cargando publicaciones");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PublicationPageResponse> call, @NonNull Throwable t) {
                        callback.onNetworkError();
                    }
                });
    }
}
