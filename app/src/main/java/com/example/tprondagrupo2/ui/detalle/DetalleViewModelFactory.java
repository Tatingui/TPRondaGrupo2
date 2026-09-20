package com.example.tprondagrupo2.ui.detalle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource;

/** Reutiliza el repositorio existente; no requiere anotaciones ni módulos Hilt nuevos. */
public final class DetalleViewModelFactory implements ViewModelProvider.Factory {
    private final PublicationDetailSource source;

    public DetalleViewModelFactory(PublicationDetailSource source) { this.source = source; }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass != DetalleViewModel.class) {
            throw new IllegalArgumentException("ViewModel no soportado: " + modelClass.getName());
        }
        return modelClass.cast(new DetalleViewModel(source));
    }
}
