package com.example.tprondagrupo2.ui.detalle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource;
import com.example.tprondagrupo2.data.repository.PublicationDetailSource.LoadError;
import com.example.tprondagrupo2.data.repository.PublicationDetailSource.Request;
import com.example.tprondagrupo2.data.repository.PublicationDetailSource.Result;
import com.example.tprondagrupo2.model.Oferta;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

/** Conserva la lectura del detalle entre recreaciones; no retiene Context, vistas ni Retrofit. */
@HiltViewModel
public final class DetalleViewModel extends ViewModel {
    private final PublicationDetailSource source;
    private final MutableLiveData<DetalleUiState> estado = new MutableLiveData<>();
    private Publicacion publicacion;
    private Publicacion pendienteCache;
    private List<Pregunta> preguntas = Collections.emptyList();
    private boolean iniciado, cerrado, detalleConfirmado, cargando, cargandoPreguntas;
    private boolean visitaLocalPendiente;
    private boolean revalidar;
    private int fotoSeleccionada;
    private LoadError error, errorPreguntas;
    private long generacionDetalle, generacionPreguntas, revisionFavorito, revisionOferta;
    private Request detalleRequest, preguntasRequest, visitaRequest;

    @Inject
    public DetalleViewModel(PublicationDetailSource source) {
        this.source = source;
    }

    public LiveData<DetalleUiState> getEstado() { return estado; }

    public void inicializar(Publicacion inicial) {
        if (cerrado) return;
        if (iniciado) {
            if (revalidar) { revalidar = false; recargarDetalle(); }
            return;
        }
        iniciado = true;
        publicacion = inicial;
        if (inicial == null || inicial.getId() == null || inicial.getId().trim().isEmpty()) {
            error = LoadError.NOT_FOUND;
            publicar();
            return;
        }
        pendienteCache = inicial;
        visitaLocalPendiente = true;
        visitaRequest = source.recordView(inicial.getId());
        recargarDetalle();
    }

    public void recargarDetalle() {
        if (cerrado || publicacion == null || publicacion.getId() == null
                || publicacion.getId().trim().isEmpty()) return;
        final String id = publicacion.getId();
        final long version = ++generacionDetalle;
        final long favoritoAlInicio = revisionFavorito;
        final long ofertaAlInicio = revisionOferta;
        cancelar(detalleRequest);
        invalidarPreguntas();
        cargando = true;
        error = null;
        publicar();
        detalleRequest = source.getDetail(id, new Result<Publicacion>() {
            @Override
            public void onSuccess(Publicacion completa) {
                if (!esActual(version)) return;
                if (completa == null || !Objects.equals(id, completa.getId())) {
                    onError(LoadError.SERVER);
                    return;
                }
                // Una lectura iniciada antes de una acción no debe deshacer su resultado.
                if (revisionFavorito != favoritoAlInicio) completa.setFavorite(publicacion.isFavorite());
                if (revisionOferta != ofertaAlInicio) completa.setMyOffer(publicacion.getMyOffer());
                publicacion = completa;
                pendienteCache = completa;
                detalleConfirmado = true;
                cargando = false;
                publicar();
                recargarPreguntas();
            }

            @Override
            public void onError(LoadError fallo) {
                if (!esActual(version)) return;
                cargando = false;
                error = fallo;
                if (fallo == LoadError.NOT_FOUND || fallo == LoadError.UNAUTHORIZED || fallo == LoadError.FORBIDDEN) {
                    detalleConfirmado = false;
                    preguntas = Collections.emptyList();
                }
                publicar();
            }
        });
    }

    public void recargarPreguntas() {
        if (cerrado || !detalleConfirmado || cargando) return;
        final long version = ++generacionPreguntas;
        cancelar(preguntasRequest);
        cargandoPreguntas = true;
        errorPreguntas = null;
        publicar();
        preguntasRequest = source.getQuestions(publicacion.getId(), new Result<List<Pregunta>>() {
            @Override
            public void onSuccess(List<Pregunta> nuevas) {
                if (cerrado || version != generacionPreguntas) return;
                if (nuevas == null) { onError(LoadError.SERVER); return; }
                preguntas = nuevas;
                cargandoPreguntas = false;
                publicar();
            }

            @Override
            public void onError(LoadError fallo) {
                if (cerrado || version != generacionPreguntas) return;
                cargandoPreguntas = false;
                errorPreguntas = fallo;
                publicar();
            }
        });
    }

    /** Puente con las acciones del Fragment hasta su extracción en el incremento 5. */
    public void actualizarFavorito(boolean favorito) {
        if (cerrado || publicacion == null) return;
        revisionFavorito++;
        publicacion.setFavorite(favorito);
        publicar();
    }

    public void actualizarOferta(Oferta oferta) {
        if (cerrado || publicacion == null) return;
        revisionOferta++;
        publicacion.setMyOffer(oferta);
        publicar();
    }

    /** Efecto consumible: no repetir la escritura de caché al recrear la vista. */
    public Publicacion consumirCachePendiente() {
        Publicacion pendiente = pendienteCache;
        pendienteCache = null;
        return pendiente;
    }

    public boolean consumirVisitaLocal() {
        boolean pendiente = visitaLocalPendiente;
        visitaLocalPendiente = false;
        return pendiente;
    }

    /** Una escritura cancelada puede haber llegado al servidor: reconciliar al volver. */
    public void revalidarAlVolver() { revalidar = true; }
    public int getFotoSeleccionada() { return fotoSeleccionada; }
    public void seleccionarFoto(int posicion) { fotoSeleccionada = Math.max(0, posicion); }

    private boolean esActual(long version) { return !cerrado && version == generacionDetalle; }

    private void invalidarPreguntas() {
        generacionPreguntas++;
        cancelar(preguntasRequest);
        cargandoPreguntas = false;
        errorPreguntas = null;
    }

    private void publicar() {
        estado.setValue(new DetalleUiState(publicacion, preguntas, detalleConfirmado,
                cargando, error, cargandoPreguntas, errorPreguntas));
    }

    private void cancelar(Request request) { if (request != null) request.cancel(); }

    @Override
    protected void onCleared() {
        cerrado = true;
        cancelar(detalleRequest);
        cancelar(preguntasRequest);
        cancelar(visitaRequest);
    }
}
