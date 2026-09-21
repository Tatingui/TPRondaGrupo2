package com.example.tprondagrupo2.ui.publish;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.PublicationCreateRequest;
import com.example.tprondagrupo2.network.PublicationApiService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class PublishViewModel extends ViewModel {

    private final PublicationApiService publicationApiService;

    private final MutableLiveData<Publicacion> publishedResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public PublishViewModel(PublicationApiService publicationApiService) {
        this.publicationApiService = publicationApiService;
    }

    public LiveData<Publicacion> getPublishedResult() { return publishedResult; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void createPublication(PublicationCreateRequest request) {
        loading.setValue(true);
        publicationApiService.createPublication(request).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    publishedResult.setValue(response.body());
                } else {
                    error.setValue("Error al publicar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Publicacion> call, Throwable t) {
                loading.setValue(false);
                error.setValue("Error de conexión");
            }
        });
    }
}
