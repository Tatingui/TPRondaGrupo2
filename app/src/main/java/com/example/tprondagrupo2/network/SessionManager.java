package com.example.tprondagrupo2.network;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Notifica a la UI cuando el token JWT venció (el backend respondió 401).
 * MainActivity observa este evento y redirige al login.
 */
public class SessionManager {

    private static SessionManager instance;

    private final MutableLiveData<Boolean> sessionExpired = new MutableLiveData<>();

    private SessionManager() {
        // Singleton
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Llamado desde el interceptor de OkHttp cuando el backend devuelve 401.
     */
    public void notifySessionExpired() {
        sessionExpired.postValue(true);
    }

    /**
     * MainActivity observa este LiveData para saber cuándo redirigir al login.
     */
    public LiveData<Boolean> onSessionExpired() {
        return sessionExpired;
    }

    /**
     * Resetea el estado después de redirigir, para que no se dispare de nuevo.
     */
    public void clearExpiredFlag() {
        sessionExpired.postValue(false);
    }
}
