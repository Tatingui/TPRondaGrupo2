package com.example.tprondagrupo2.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.OfferRespondRequest;
import com.example.tprondagrupo2.model.OperacionHistorial;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.network.HistorialApiService;
import com.example.tprondagrupo2.network.OfferApiService;
import com.example.tprondagrupo2.network.PublicationReadApiService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class MyOffersViewModel extends ViewModel {

    private final OfferApiService offerApiService;
    private final HistorialApiService historialApiService;
    private final PublicationReadApiService publicationReadApiService;

    private final MutableLiveData<List<Offer>> offers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> emptyMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Publicacion> navigateToDetail = new MutableLiveData<>();

    private Call<List<Offer>> loadingCall;

    @Inject
    public MyOffersViewModel(OfferApiService offerApiService,
                             HistorialApiService historialApiService,
                             PublicationReadApiService publicationReadApiService) {
        this.offerApiService = offerApiService;
        this.historialApiService = historialApiService;
        this.publicationReadApiService = publicationReadApiService;
    }

    public LiveData<List<Offer>> getOffers() { return offers; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getEmptyMessage() { return emptyMessage; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Publicacion> getNavigateToDetail() { return navigateToDetail; }

    public void fetchOffers(boolean isReceivedTab) {
        cancelLoading();
        offers.setValue(new ArrayList<>());
        loading.setValue(true);
        emptyMessage.setValue(null);
        errorMessage.setValue(null);

        Call<List<Offer>> call = isReceivedTab ? offerApiService.getReceivedOffers() : offerApiService.getSentOffers();
        loadingCall = call;

        call.enqueue(new Callback<List<Offer>>() {
            @Override
            public void onResponse(Call<List<Offer>> call, Response<List<Offer>> response) {
                if (call != loadingCall) return;
                loadingCall = null;
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Offer> result = response.body();
                    offers.setValue(result);
                    if (result.isEmpty()) {
                        emptyMessage.setValue("No hay ofertas para mostrar.");
                    }
                } else {
                    errorMessage.setValue("Error cargando ofertas: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Offer>> call, Throwable t) {
                if (call != loadingCall) return;
                loadingCall = null;
                if (!call.isCanceled()) {
                    loading.setValue(false);
                    errorMessage.setValue("Error de conexión al cargar ofertas");
                }
            }
        });
    }

    private void cancelLoading() {
        Call<List<Offer>> previous = loadingCall;
        loadingCall = null;
        if (previous != null) previous.cancel();
    }

    @Override
    protected void onCleared() {
        cancelLoading();
        super.onCleared();
    }

    public void acceptOffer(Offer offer, boolean isReceivedTab) {
        if (offer == null || offer.getId() == null) return;
        loading.setValue(true);
        historialApiService.acceptOffer(offer.getId()).enqueue(new Callback<OperacionHistorial>() {
            @Override
            public void onResponse(Call<OperacionHistorial> call, Response<OperacionHistorial> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    toastMessage.setValue("Oferta aceptada");
                    fetchOffers(isReceivedTab);
                    fetchPublicationForDetail(offer.getPublicationId());
                } else {
                    toastMessage.setValue("Error al aceptar oferta");
                }
            }

            @Override
            public void onFailure(Call<OperacionHistorial> call, Throwable t) {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void rejectOffer(Long offerId, boolean isReceivedTab) {
        if (offerId == null) return;
        loading.setValue(true);
        OfferRespondRequest request = new OfferRespondRequest("REJECTED", null);
        offerApiService.respondOffer(offerId, request).enqueue(new Callback<Offer>() {
            @Override
            public void onResponse(Call<Offer> call, Response<Offer> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    toastMessage.setValue("Oferta rechazada");
                    fetchOffers(isReceivedTab);
                } else {
                    toastMessage.setValue("Error al rechazar oferta");
                }
            }

            @Override
            public void onFailure(Call<Offer> call, Throwable t) {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void sendCounterOffer(Long offerId, double newPrice, boolean isReceivedTab) {
        if (offerId == null) return;
        loading.setValue(true);
        OfferRespondRequest request = new OfferRespondRequest("COUNTER_OFFER", newPrice);
        offerApiService.respondOffer(offerId, request).enqueue(new Callback<Offer>() {
            @Override
            public void onResponse(Call<Offer> call, Response<Offer> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    toastMessage.setValue("Contra-oferta enviada");
                    fetchOffers(isReceivedTab);
                } else {
                    toastMessage.setValue("Error al enviar contra-oferta");
                }
            }

            @Override
            public void onFailure(Call<Offer> call, Throwable t) {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void fetchPublicationForDetail(Long publicationId) {
        if (publicationId == null) return;
        publicationReadApiService.getPublication(String.valueOf(publicationId)).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    navigateToDetail.setValue(response.body());
                } else {
                    toastMessage.setValue("No se pudo abrir la publicación (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Publicacion> call, Throwable t) {
                toastMessage.setValue("No se pudo abrir el detalle. Reintentá desde la oferta.");
            }
        });
    }

    public void onNavigatedToDetail() {
        navigateToDetail.setValue(null);
    }
}
