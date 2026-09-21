package com.example.tprondagrupo2.ui.historial;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.data.repository.HistorialRepository;
import com.example.tprondagrupo2.model.Calificacion;
import com.example.tprondagrupo2.model.CalificacionRequest;
import com.example.tprondagrupo2.model.OperacionHistorial;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HistorialViewModel extends ViewModel {

    private final HistorialRepository historialRepository;

    private final MutableLiveData<List<OperacionHistorial>> operaciones = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> actionSuccess = new MutableLiveData<>();

    @Inject
    public HistorialViewModel(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    public LiveData<List<OperacionHistorial>> getOperaciones() { return operaciones; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getActionSuccess() { return actionSuccess; }

    public void loadHistory(String tipo, String from, String to) {
        loading.setValue(true);
        HistorialRepository.HistorialCallback callback = new HistorialRepository.HistorialCallback() {
            @Override
            public void onSuccess(List<OperacionHistorial> list, int totalPages, boolean isLast) {
                loading.setValue(false);
                operaciones.setValue(list);
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                error.setValue("Error de red");
            }
        };

        if (from == null && to == null) {
            historialRepository.getHistory(tipo, 0, 50, callback);
        } else {
            historialRepository.getHistoryWithDates(tipo, from, to, 0, 50, callback);
        }
    }

    public void confirmDelivery(Long transactionId) {
        loading.setValue(true);
        historialRepository.confirmDelivery(transactionId, new HistorialRepository.OperacionCallback() {
            @Override
            public void onSuccess(OperacionHistorial result) {
                loading.setValue(false);
                actionSuccess.setValue("Entrega confirmada con éxito");
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                error.setValue("Error de red");
            }
        });
    }

    public void rate(Long transactionId, int puntaje, String comentario) {
        loading.setValue(true);
        CalificacionRequest req = new CalificacionRequest(puntaje, comentario);
        historialRepository.rate(transactionId, req, new HistorialRepository.CalificacionCallback() {
            @Override
            public void onSuccess(Calificacion result) {
                loading.setValue(false);
                actionSuccess.setValue("Calificación enviada correctamente");
            }

            @Override
            public void onEmpty() {
                loading.setValue(false);
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                error.setValue("Error de red");
            }
        });
    }

    public void acceptOffer(Long offerId) {
        loading.setValue(true);
        historialRepository.acceptOffer(offerId, new HistorialRepository.OperacionCallback() {
            @Override
            public void onSuccess(OperacionHistorial result) {
                loading.setValue(false);
                actionSuccess.setValue("Oferta aceptada");
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                error.setValue("Error de red");
            }
        });
    }

    public void rejectOffer(Long offerId) {
        loading.setValue(true);
        historialRepository.rejectOffer(offerId, new HistorialRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                actionSuccess.setValue("Oferta rechazada");
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                error.setValue("Error de red");
            }
        });
    }
}
