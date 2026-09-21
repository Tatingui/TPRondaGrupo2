package com.example.tprondagrupo2.ui.detalle;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource.LoadError;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Estado de lectura y acciones. La UI consume los modelos; las actualizaciones se notifican al ViewModel. */
public final class DetalleUiState {
    private final Publicacion publicacion;
    private final List<Pregunta> preguntas;
    private final boolean detalleConfirmado;
    private final boolean cargando;
    private final LoadError error;
    private final boolean cargandoPreguntas;
    private final LoadError errorPreguntas;
    private final boolean favoritoEnCurso;
    private final boolean ofertaEnCurso;
    private final boolean gestionEnCurso;

    DetalleUiState(Publicacion publicacion, List<Pregunta> preguntas, boolean detalleConfirmado,
                   boolean cargando, LoadError error, boolean cargandoPreguntas, LoadError errorPreguntas,
                   boolean favoritoEnCurso, boolean ofertaEnCurso, boolean gestionEnCurso) {
        this.publicacion = publicacion;
        this.preguntas = Collections.unmodifiableList(new ArrayList<>(preguntas));
        this.detalleConfirmado = detalleConfirmado;
        this.cargando = cargando;
        this.error = error;
        this.cargandoPreguntas = cargandoPreguntas;
        this.errorPreguntas = errorPreguntas;
        this.favoritoEnCurso = favoritoEnCurso;
        this.ofertaEnCurso = ofertaEnCurso;
        this.gestionEnCurso = gestionEnCurso;
    }

    public Publicacion getPublicacion() { return publicacion; }
    public List<Pregunta> getPreguntas() { return preguntas; }
    public boolean isDetalleConfirmado() { return detalleConfirmado; }
    public boolean isCargando() { return cargando; }
    public LoadError getError() { return error; }
    public boolean isCargandoPreguntas() { return cargandoPreguntas; }
    public LoadError getErrorPreguntas() { return errorPreguntas; }
    public boolean isFavoritoEnCurso() { return favoritoEnCurso; }
    public boolean isOfertaEnCurso() { return ofertaEnCurso; }
    public boolean isGestionEnCurso() { return gestionEnCurso; }
}
