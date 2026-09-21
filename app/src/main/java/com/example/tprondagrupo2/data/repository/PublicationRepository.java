package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.network.PublicationApiService;
import com.example.tprondagrupo2.network.PublicationPageResponse;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para operaciones de publicaciones y favoritos.
 *
 * Patron Repository: los Fragments no llaman a Retrofit directamente.
 */
@Singleton
public class PublicationRepository implements PublicationDetailSource {

    public interface FavoritesCallback extends RepoCallback<List<Publicacion>> {}
    public interface PublicationPageCallback extends RepoCallback<PublicationPageResponse> {}

    public interface ToggleFavoriteCallback extends RepoCallback<Void> {
        void onSuccess();
        @Override
        default void onSuccess(Void result) {
            onSuccess();
        }
    }

    private final PublicationApiService apiService;

    @Inject
    public PublicationRepository(PublicationApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Request getDetail(String id, Result<Publicacion> result) {
        return enqueueDetail(apiService.getPublication(id), result, true);
    }

    @Override
    public Request getQuestions(String id, Result<List<Pregunta>> result) {
        return enqueueDetail(apiService.getQuestions(id), result, true);
    }

    @Override
    public Request recordView(String id) {
        return enqueueDetail(apiService.recordView(id), new Result<Void>() {
            @Override public void onSuccess(Void value) { }
            @Override public void onError(LoadError error) { /* Operación no bloqueante. */ }
        }, false);
    }

    private <T> Request enqueueDetail(Call<T> call, Result<T> result, boolean requiresBody) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (call.isCanceled()) {
                    if (response.errorBody() != null) response.errorBody().close();
                    return;
                }
                if (response.isSuccessful() && (!requiresBody || response.body() != null)) {
                    result.onSuccess(response.body());
                } else {
                    if (response.errorBody() != null) response.errorBody().close();
                    result.onError(mapError(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable error) {
                if (!call.isCanceled()) result.onError(LoadError.NETWORK);
            }
        });
        return call::cancel;
    }

    private LoadError mapError(int code) {
        if (code == 404) return LoadError.NOT_FOUND;
        if (code == 401) return LoadError.UNAUTHORIZED;
        if (code == 403) return LoadError.FORBIDDEN;
        return LoadError.SERVER;
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
