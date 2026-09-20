package com.example.tprondagrupo2;

import android.app.Application;

import com.example.tprondagrupo2.data.ThemePreferenceManager;
import com.example.tprondagrupo2.network.TokenManager;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class RondaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TokenManager.setContext(this);
        new ThemePreferenceManager(this).applyTheme();
    }
}
