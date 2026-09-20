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
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>();

    public NetworkObserver(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        checkInitialStatus();
        registerCallback();
    }

    private void checkInitialStatus() {
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            isConnected.postValue(false);
            return;
        }
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        boolean hasInternet = capabilities != null && 
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        isConnected.postValue(hasInternet);
    }

    private void registerCallback() {
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                isConnected.postValue(true);
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
        Boolean value = isConnected.getValue();
        return value != null && value;
    }
}
