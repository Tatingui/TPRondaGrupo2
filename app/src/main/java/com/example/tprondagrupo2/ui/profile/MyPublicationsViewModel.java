package com.example.tprondagrupo2.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.network.PublicationReadApiService;
import com.example.tprondagrupo2.network.PublicationWriteApiService;
import com.example.tprondagrupo2.util.PublicationConstants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class MyPublicationsViewModel extends ViewModel {

    private final PublicationReadApiService publicationReadApiService;
    private final PublicationWriteApiService publicationWriteApiService;

    private final MutableLiveData<List<Publicacion>> myPublications = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> emptyVisible = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    @Inject
    public MyPublicationsViewModel(PublicationReadApiService publicationReadApiService,
                                   PublicationWriteApiService publicationWriteApiService) {
        this.publicationReadApiService = publicationReadApiService;
        this.publicationWriteApiService = publicationWriteApiService;
    }

    public LiveData<List<Publicacion>> getMyPublications() { return myPublications; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getEmptyVisible() { return emptyVisible; }
    public LiveData<String> getToastMessage() { return toastMessage; }

    public void fetchMyPublications() {
        loading.setValue(true);
        emptyVisible.setValue(false);

        publicationReadApiService.getMyPublications().enqueue(new Callback<List<Publicacion>>() {
            @Override
            public void onResponse(Call<List<Publicacion>> call, Response<List<Publicacion>> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Publicacion> list = response.body();
                    myPublications.setValue(list);
                    emptyVisible.setValue(list.isEmpty());
                } else {
                    toastMessage.setValue("Error al cargar mis publicaciones");
                }
            }

            @Override
            public void onFailure(Call<List<Publicacion>> call, Throwable t) {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void updateStatus(Long id, String state) {
        if (id == null) return;
        loading.setValue(true);
        publicationWriteApiService.updatePublicationStatus(id, state).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    toastMessage.setValue("Estado actualizado a " + PublicationConstants.translateStatus(state));
                    fetchMyPublications();
                } else {
                    toastMessage.setValue("Error al actualizar estado");
                }
            }

            @Override
            public void onFailure(Call<Publicacion> call, Throwable t) {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void deletePublication(Publicacion pub) {
        if (pub == null || pub.getIdLong() == null) return;
        loading.setValue(true);
        publicationWriteApiService.deletePublication(pub.getIdLong()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    toastMessage.setValue("Publicación dada de baja");
                    fetchMyPublications();
                } else {
                    toastMessage.setValue("Error al dar de baja (código " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }
}
