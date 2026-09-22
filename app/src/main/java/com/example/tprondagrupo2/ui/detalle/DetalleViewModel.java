package com.example.tprondagrupo2.ui.detalle;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource;
import com.example.tprondagrupo2.data.repository.PublicationDetailSource.LoadError;
import com.example.tprondagrupo2.data.repository.PublicationDetailSource.Request;
import com.example.tprondagrupo2.data.repository.PublicationDetailSource.Result;
import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.OfertaRequest;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.TextoRequest;
import com.example.tprondagrupo2.network.FavoritesDataStoreManager;
import com.example.tprondagrupo2.network.PublicationFavoriteApiService;
import com.example.tprondagrupo2.network.PublicationWriteApiService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Conserva la lectura del detalle entre recreaciones; no retiene Context, vistas ni Retrofit. */
@HiltViewModel
public final class DetalleViewModel extends ViewModel {
    private final PublicationDetailSource source;
    private final PublicationWriteApiService writeApi;
    private final PublicationFavoriteApiService favoriteApi;
    private final FavoritesDataStoreManager favoritesDataStore;
    private static final Gson GSON = new Gson();

    private final MutableLiveData<DetalleUiState> estado = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

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

    // Flags de acciones de escritura en curso
    private boolean favoritoEnCurso, ofertaEnCurso, gestionEnCurso;
    private final List<Call<?>> writeCalls = new ArrayList<>();

    @Inject
    public DetalleViewModel(PublicationDetailSource source,
                            PublicationWriteApiService writeApi,
                            PublicationFavoriteApiService favoriteApi,
                            FavoritesDataStoreManager favoritesDataStore) {
        this.source = source;
        this.writeApi = writeApi;
        this.favoriteApi = favoriteApi;
        this.favoritesDataStore = favoritesDataStore;
    }

    public LiveData<DetalleUiState> getEstado() { return estado; }
    public LiveData<String> getToastMessage() { return toastMessage; }

    // ────────── Lectura del detalle ──────────

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

    // ────────── Acciones de escritura ──────────

    public void enviarPregunta(String texto) {
        if (cerrado || publicacion == null) return;
        Call<Pregunta> call = writeApi.askQuestion(publicacion.getId(), new TextoRequest(texto));
        trackCall(call);
        call.enqueue(new Callback<Pregunta>() {
            @Override
            public void onResponse(Call<Pregunta> call, Response<Pregunta> response) {
                untrackCall(call);
                if (cerrado) return;
                if (response.isSuccessful()) {
                    toastMessage.setValue("Pregunta enviada");
                    recargarPreguntas();
                } else {
                    toastMessage.setValue(mensajeDeError(response, "No se pudo enviar la pregunta"));
                }
            }

            @Override
            public void onFailure(Call<Pregunta> call, Throwable t) {
                untrackCall(call);
                if (!cerrado) toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void responderPregunta(Long preguntaId, String texto) {
        if (cerrado) return;
        Call<Pregunta> call = writeApi.answerQuestion(preguntaId, new TextoRequest(texto));
        trackCall(call);
        call.enqueue(new Callback<Pregunta>() {
            @Override
            public void onResponse(Call<Pregunta> call, Response<Pregunta> response) {
                untrackCall(call);
                if (cerrado) return;
                if (response.isSuccessful()) {
                    toastMessage.setValue("Respuesta enviada");
                    recargarPreguntas();
                } else {
                    toastMessage.setValue(mensajeDeError(response, "No se pudo enviar la respuesta"));
                }
            }

            @Override
            public void onFailure(Call<Pregunta> call, Throwable t) {
                untrackCall(call);
                if (!cerrado) toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void enviarOferta(double monto, @Nullable String mensaje) {
        if (cerrado || publicacion == null || ofertaEnCurso) return;
        ofertaEnCurso = true;
        publicar();
        Call<Offer> call = writeApi.makeOffer(publicacion.getId(), new OfertaRequest(monto, mensaje));
        trackCall(call);
        call.enqueue(new Callback<Offer>() {
            @Override
            public void onResponse(Call<Offer> call, Response<Offer> response) {
                untrackCall(call);
                if (cerrado) return;
                ofertaEnCurso = false;
                if (response.isSuccessful() && response.body() != null) {
                    toastMessage.setValue("Oferta enviada");
                    actualizarOferta(response.body());
                } else {
                    toastMessage.setValue(mensajeDeError(response, "No se pudo enviar la oferta"));
                    publicar();
                }
            }

            @Override
            public void onFailure(Call<Offer> call, Throwable t) {
                untrackCall(call);
                if (cerrado) return;
                ofertaEnCurso = false;
                toastMessage.setValue("Error de conexión");
                publicar();
            }
        });
    }

    public void cambiarEstadoPublicacion(String nuevoEstado) {
        if (cerrado || publicacion == null || gestionEnCurso) return;
        Long id = publicacion.getIdLong();
        if (id == null) return;
        gestionEnCurso = true;
        publicar();
        Call<Publicacion> call = writeApi.updatePublicationStatus(id, nuevoEstado);
        trackCall(call);
        call.enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                untrackCall(call);
                if (cerrado) return;
                gestionEnCurso = false;
                if (response.isSuccessful()) {
                    toastMessage.setValue("Publicación actualizada");
                    recargarDetalle();
                } else {
                    toastMessage.setValue(mensajeDeError(response, "No se pudo actualizar la publicación"));
                    publicar();
                }
            }

            @Override
            public void onFailure(Call<Publicacion> call, Throwable t) {
                untrackCall(call);
                if (cerrado) return;
                gestionEnCurso = false;
                toastMessage.setValue("Error de conexión");
                publicar();
            }
        });
    }

    public void toggleFavorite() {
        if (cerrado || publicacion == null || favoritoEnCurso) return;
        final boolean wasFavorite = publicacion.isFavorite();
        final String pubId = publicacion.getId();
        favoritoEnCurso = true;
        publicar();

        Call<Void> call = wasFavorite
                ? favoriteApi.unmarkAsFavorite(pubId)
                : favoriteApi.markAsFavorite(pubId);
        trackCall(call);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                untrackCall(call);
                if (cerrado) return;
                favoritoEnCurso = false;
                if (response.isSuccessful()) {
                    actualizarFavorito(!wasFavorite);
                    if (!wasFavorite) {
                        favoritesDataStore.addFavorite(pubId);
                    } else {
                        favoritesDataStore.removeFavorite(pubId);
                    }
                    toastMessage.setValue(!wasFavorite ? "Agregado a favoritos" : "Eliminado de favoritos");
                } else {
                    toastMessage.setValue(mensajeDeError(response, "Error al actualizar favorito"));
                    publicar();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                untrackCall(call);
                if (cerrado) return;
                favoritoEnCurso = false;
                toastMessage.setValue("Error de conexión");
                publicar();
            }
        });
    }

    // ────────── Estado local ──────────

    /** Puente con las acciones del Fragment hasta su extracción en el incremento 5. */
    public void actualizarFavorito(boolean favorito) {
        if (cerrado || publicacion == null) return;
        revisionFavorito++;
        publicacion.setFavorite(favorito);
        publicar();
    }

    public void actualizarOferta(Offer oferta) {
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

    /** Persiste en DataStore que la publicación fue vista (sin cambios pendientes). */
    public void registrarVistaLocal(String pubId) {
        if (pubId != null) favoritesDataStore.setHasUpdates(pubId, false);
    }

    /** Una escritura cancelada puede haber llegado al servidor: reconciliar al volver. */
    public void revalidarAlVolver() { revalidar = true; }
    public int getFotoSeleccionada() { return fotoSeleccionada; }
    public void seleccionarFoto(int posicion) { fotoSeleccionada = Math.max(0, posicion); }

    public boolean hasPendingWrites() { return !writeCalls.isEmpty(); }

    // ────────── Internos ──────────

    private boolean esActual(long version) { return !cerrado && version == generacionDetalle; }

    private void invalidarPreguntas() {
        generacionPreguntas++;
        cancelar(preguntasRequest);
        cargandoPreguntas = false;
        errorPreguntas = null;
    }

    private void publicar() {
        estado.setValue(new DetalleUiState(publicacion, preguntas, detalleConfirmado,
                cargando, error, cargandoPreguntas, errorPreguntas,
                favoritoEnCurso, ofertaEnCurso, gestionEnCurso));
    }

    private void cancelar(Request request) { if (request != null) request.cancel(); }

    private void trackCall(Call<?> call) { writeCalls.add(call); }
    private void untrackCall(Call<?> call) { writeCalls.remove(call); }

    private String mensajeDeError(Response<?> response, String mensajePorDefecto) {
        if (response.code() == 401) {
            return "Sesión vencida o inválida. Iniciá sesión de nuevo.";
        }
        if (response.errorBody() != null) {
            try {
                AuthResponse err = GSON.fromJson(response.errorBody().string(), AuthResponse.class);
                if (err != null && err.getMessage() != null && !err.getMessage().isEmpty()) {
                    return err.getMessage();
                }
            } catch (Exception ignored) {}
        }
        return mensajePorDefecto;
    }

    @Override
    protected void onCleared() {
        cerrado = true;
        cancelar(detalleRequest);
        cancelar(preguntasRequest);
        cancelar(visitaRequest);
        for (Call<?> call : writeCalls) call.cancel();
        writeCalls.clear();
    }
}
