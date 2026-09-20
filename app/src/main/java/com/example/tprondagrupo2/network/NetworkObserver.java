package com.example.tprondagrupo2.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NetworkObserver {

    interface Source {
        boolean isConnected();
        void start(Runnable onChange);
        void stop();
    }

    private final Source source;
    private final LiveData<Boolean> isConnected;

    public NetworkObserver(Context context) {
        this(new AndroidSource(context));
    }

    NetworkObserver(Source source) {
        this.source = source;
        isConnected = new MutableLiveData<Boolean>(source.isConnected()) {
            @Override
            protected void onActive() {
                source.start(() -> postValue(source.isConnected()));
                setValue(source.isConnected());
            }

            @Override
            protected void onInactive() {
                source.stop();
            }
        };
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public boolean isCurrentlyConnected() {
        return source.isConnected();
    }

    /** Consulta puntual, sin registrar callbacks. */
    public static boolean isCurrentlyConnected(Context context) {
        return new AndroidSource(context).isConnected();
    }

    private static final class AndroidSource implements Source {
        private final ConnectivityManager manager;
        private ConnectivityManager.NetworkCallback callback;

        AndroidSource(Context context) {
            manager = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        @Override
        public boolean isConnected() {
            if (manager == null) return false;
            Network active = manager.getActiveNetwork();
            NetworkCapabilities caps = active == null ? null : manager.getNetworkCapabilities(active);
            return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }

        @Override
        public void start(Runnable onChange) {
            if (manager == null || callback != null) return;
            callback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) { onChange.run(); }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network,
                                                  @NonNull NetworkCapabilities capabilities) {
                    onChange.run();
                }

                @Override
                public void onLost(@NonNull Network network) { onChange.run(); }
            };
            // Consultar la red activa evita marcar offline al perder una red secundaria.
            // La validación es independiente del transporte (incluye VPN y Bluetooth).
            manager.registerNetworkCallback(new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), callback);
        }

        @Override
        public void stop() {
            if (callback == null) return;
            manager.unregisterNetworkCallback(callback);
            callback = null;
        }
    }
}
