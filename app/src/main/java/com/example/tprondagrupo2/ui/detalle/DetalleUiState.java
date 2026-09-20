package com.example.tprondagrupo2.ui.detalle;

import com.example.tprondagrupo2.data.repository.PublicationDetailSource.LoadError;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Estado de lectura. La UI consume los modelos; las actualizaciones se notifican al ViewModel. */
public final class DetalleUiState {
    private final Publicacion publicacion;
    private final List<Pregunta> preguntas;
    private final boolean detalleConfirmado;
    private final boolean cargando;
    private final LoadError error;
    private final boolean cargandoPreguntas;
    private final LoadError errorPreguntas;

    DetalleUiState(Publicacion publicacion, List<Pregunta> preguntas, boolean detalleConfirmado,
                   boolean cargando, LoadError error, boolean cargandoPreguntas, LoadError errorPreguntas) {
        this.publicacion = publicacion;
        this.preguntas = Collections.unmodifiableList(new ArrayList<>(preguntas));
        this.detalleConfirmado = detalleConfirmado;
        this.cargando = cargando;
        this.error = error;
        this.cargandoPreguntas = cargandoPreguntas;
        this.errorPreguntas = errorPreguntas;
    }

    public Publicacion getPublicacion() { return publicacion; }
    public List<Pregunta> getPreguntas() { return preguntas; }
    public boolean isDetalleConfirmado() { return detalleConfirmado; }
    public boolean isCargando() { return cargando; }
    public LoadError getError() { return error; }
    public boolean isCargandoPreguntas() { return cargandoPreguntas; }
    public LoadError getErrorPreguntas() { return errorPreguntas; }
}
