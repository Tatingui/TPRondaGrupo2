package com.example.tprondagrupo2;

import android.app.Application;

import com.example.tprondagrupo2.network.TokenManager;

public class RondaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TokenManager.setContext(this);
    }
}
