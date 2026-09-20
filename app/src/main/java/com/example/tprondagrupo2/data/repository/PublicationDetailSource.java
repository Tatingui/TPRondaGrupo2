package com.example.tprondagrupo2.data.repository;

import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;

import java.util.List;

/** Contrato de lectura del detalle; callbacks en el hilo principal y cancelación por operación. */
public interface PublicationDetailSource {
    enum LoadError { NETWORK, NOT_FOUND, UNAUTHORIZED, FORBIDDEN, SERVER }

    interface Request {
        void cancel();
    }

    interface Result<T> {
        void onSuccess(T value);
        void onError(LoadError error);
    }

    Request getDetail(String id, Result<Publicacion> result);
    Request getQuestions(String id, Result<List<Pregunta>> result);
    Request recordView(String id);
}
