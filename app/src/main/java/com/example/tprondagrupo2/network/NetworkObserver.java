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

    private final ConnectivityManager connectivityManager;
    private final MutableLiveData<Boolean> isConnected;

    public NetworkObserver(Context context) {
        Context appContext = context.getApplicationContext();
        connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        isConnected = new MutableLiveData<>(hasValidatedInternet(connectivityManager));
        registerCallback();
    }

    private static boolean hasValidatedInternet(ConnectivityManager connectivityManager) {
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private void registerCallback() {
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                // A veces onAvailable se dispara antes de que se valide el internet.
                // Lo marcamos temporalmente como true, pero onCapabilitiesChanged es mas preciso.
                isConnected.postValue(true);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                // Soportamos explicitamente VPN y Bluetooth
                boolean validTransport = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH);
                
                isConnected.postValue(hasInternet && validTransport);
            }

            @Override
            public void onLost(@NonNull Network network) {
                isConnected.postValue(false);
            }
        });
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public boolean isCurrentlyConnected() {
        return hasValidatedInternet(connectivityManager);
    }

    /**
     * Consulta puntual que no registra un callback. Usar cuando solo se necesita
     * validar la red antes de una operacion y no observar cambios posteriores.
     */
    public static boolean isCurrentlyConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return hasValidatedInternet(manager);
    }
}
