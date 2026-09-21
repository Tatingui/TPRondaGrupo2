package com.example.tprondagrupo2.ui.detalle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource;
import com.example.tprondagrupo2.network.FavoritesDataStoreManager;
import com.example.tprondagrupo2.network.PublicationFavoriteApiService;
import com.example.tprondagrupo2.network.PublicationWriteApiService;

/** Fábrica exclusiva de tests JVM para verificar ViewModelStore con una fuente simulada. */
public final class DetalleViewModelFactory implements ViewModelProvider.Factory {
    private final PublicationDetailSource source;
    private final PublicationWriteApiService writeApi;
    private final PublicationFavoriteApiService favoriteApi;
    private final FavoritesDataStoreManager favoritesDataStore;

    public DetalleViewModelFactory(PublicationDetailSource source,
                                   PublicationWriteApiService writeApi,
                                   PublicationFavoriteApiService favoriteApi,
                                   FavoritesDataStoreManager favoritesDataStore) {
        this.source = source;
        this.writeApi = writeApi;
        this.favoriteApi = favoriteApi;
        this.favoritesDataStore = favoritesDataStore;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass != DetalleViewModel.class) {
            throw new IllegalArgumentException("ViewModel no soportado: " + modelClass.getName());
        }
        return modelClass.cast(new DetalleViewModel(source, writeApi, favoriteApi, favoritesDataStore));
    }
}
