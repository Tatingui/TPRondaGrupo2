package com.example.tprondagrupo2.ui.publish;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.data.repository.PublicationRepository;
import com.example.tprondagrupo2.data.repository.RepoCallback;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.PublicationCreateRequest;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PublishViewModel extends ViewModel {

    private final PublicationRepository publicationRepository;

    private final MutableLiveData<Publicacion> publishedResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public PublishViewModel(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    public LiveData<Publicacion> getPublishedResult() { return publishedResult; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void createPublication(PublicationCreateRequest request) {
        loading.setValue(true);
        publicationRepository.createPublication(request, new RepoCallback<Publicacion>() {
            @Override
            public void onSuccess(Publicacion result) {
                loading.setValue(false);
                publishedResult.setValue(result);
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                error.setValue("Error de conexión");
            }
        });
    }
}
