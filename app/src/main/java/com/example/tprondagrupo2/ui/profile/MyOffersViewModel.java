package com.example.tprondagrupo2.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.data.repository.OfferRepository;
import com.example.tprondagrupo2.data.repository.RepoCallback;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.OperacionHistorial;
import com.example.tprondagrupo2.model.Publicacion;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MyOffersViewModel extends ViewModel {

    private final OfferRepository offerRepository;

    private final MutableLiveData<List<Offer>> offers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> emptyMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Publicacion> navigateToDetail = new MutableLiveData<>();

    @Inject
    public MyOffersViewModel(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public LiveData<List<Offer>> getOffers() { return offers; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getEmptyMessage() { return emptyMessage; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Publicacion> getNavigateToDetail() { return navigateToDetail; }

    public void fetchOffers(boolean isReceivedTab) {
        offers.setValue(new ArrayList<>());
        loading.setValue(true);
        emptyMessage.setValue(null);
        errorMessage.setValue(null);

        offerRepository.fetchOffers(isReceivedTab, new RepoCallback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> result) {
                loading.setValue(false);
                offers.setValue(result);
                if (result.isEmpty()) {
                    emptyMessage.setValue("No hay ofertas para mostrar.");
                }
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                errorMessage.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                errorMessage.setValue("Error de conexión al cargar ofertas");
            }
        });
    }

    public void acceptOffer(Offer offer, boolean isReceivedTab) {
        if (offer == null || offer.getId() == null) return;
        loading.setValue(true);

        offerRepository.acceptOffer(offer.getId(), new RepoCallback<OperacionHistorial>() {
            @Override
            public void onSuccess(OperacionHistorial result) {
                loading.setValue(false);
                toastMessage.setValue("Oferta aceptada");
                fetchOffers(isReceivedTab);
                fetchPublicationForDetail(offer.getPublicationId());
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                toastMessage.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void rejectOffer(Long offerId, boolean isReceivedTab) {
        if (offerId == null) return;
        loading.setValue(true);

        offerRepository.rejectOffer(offerId, new RepoCallback<Offer>() {
            @Override
            public void onSuccess(Offer result) {
                loading.setValue(false);
                toastMessage.setValue("Oferta rechazada");
                fetchOffers(isReceivedTab);
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                toastMessage.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void sendCounterOffer(Long offerId, double newPrice, boolean isReceivedTab) {
        if (offerId == null) return;
        loading.setValue(true);

        offerRepository.sendCounterOffer(offerId, newPrice, new RepoCallback<Offer>() {
            @Override
            public void onSuccess(Offer result) {
                loading.setValue(false);
                toastMessage.setValue("Contra-oferta enviada");
                fetchOffers(isReceivedTab);
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                toastMessage.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void fetchPublicationForDetail(Long publicationId) {
        offerRepository.fetchPublication(publicationId, new RepoCallback<Publicacion>() {
            @Override
            public void onSuccess(Publicacion result) {
                navigateToDetail.setValue(result);
            }

            @Override
            public void onError(String msg) {
                toastMessage.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                toastMessage.setValue("No se pudo abrir el detalle. Reintentá desde la oferta.");
            }
        });
    }

    public void onNavigatedToDetail() {
        navigateToDetail.setValue(null);
    }
}
