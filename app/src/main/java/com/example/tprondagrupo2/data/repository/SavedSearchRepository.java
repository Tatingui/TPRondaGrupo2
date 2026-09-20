package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.network.SavedSearchApiService;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para operaciones de busquedas guardadas.
 *
 * Patron Repository: los Fragments no llaman a Retrofit directamente.
 */
public class SavedSearchRepository {

    public interface SavedSearchListCallback {
        void onSuccess(List<SavedSearch> searches);
        void onError(String message);
        void onNetworkError();
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
        void onNetworkError();
    }

    private final SavedSearchApiService apiService;

    @Inject
    public SavedSearchRepository(SavedSearchApiService apiService) {
        this.apiService = apiService;
    }

    public void getSavedSearches(SavedSearchListCallback callback) {
        apiService.getSavedSearches().enqueue(new Callback<List<SavedSearch>>() {
            @Override
            public void onResponse(@NonNull Call<List<SavedSearch>> call,
                                   @NonNull Response<List<SavedSearch>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error cargando busquedas guardadas");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SavedSearch>> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    public void deleteSearch(Long id, SimpleCallback callback) {
        apiService.deleteSearch(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al eliminar la busqueda");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }
}
