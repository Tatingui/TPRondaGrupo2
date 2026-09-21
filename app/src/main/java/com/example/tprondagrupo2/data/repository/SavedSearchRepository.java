package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.network.SavedSearchApiService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para operaciones de busquedas guardadas.
 *
 * Patron Repository: los Fragments no llaman a Retrofit directamente.
 */
@Singleton
public class SavedSearchRepository {

    private final SavedSearchApiService apiService;

    @Inject
    public SavedSearchRepository(SavedSearchApiService apiService) {
        this.apiService = apiService;
    }

    public void getSavedSearches(RepoCallback<List<SavedSearch>> callback) {
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

    public void deleteSearch(Long id, RepoCallback<Void> callback) {
        apiService.deleteSearch(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
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
