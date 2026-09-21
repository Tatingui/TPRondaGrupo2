package com.example.tprondagrupo2.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.data.repository.PublicationRepository;
import com.example.tprondagrupo2.data.repository.RepoCallback;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.network.PublicationPageResponse;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final PublicationRepository publicationRepository;

    private final MutableLiveData<List<Publicacion>> publications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public HomeViewModel(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    public LiveData<List<Publicacion>> getPublications() {
        return publications;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadPublications(String search, Long categoryId, Double minPrice, Double maxPrice,
                                String status, String location, int page, int size, String sort) {
        loading.setValue(true);
        publicationRepository.getPublications(search, categoryId, minPrice, maxPrice, status, location, page, size, sort,
                new PublicationRepository.PublicationPageCallback() {
                    @Override
                    public void onSuccess(PublicationPageResponse pageResponse) {
                        loading.setValue(false);
                        if (pageResponse != null && pageResponse.getContent() != null) {
                            publications.setValue(pageResponse.getContent());
                        } else {
                            publications.setValue(null);
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        loading.setValue(false);
                        error.setValue(msg);
                    }

                    @Override
                    public void onNetworkError() {
                        loading.setValue(false);
                        error.setValue("Error de red al cargar publicaciones");
                    }
                });
    }
}
