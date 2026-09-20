package com.example.tprondagrupo2;

import android.app.Application;

import com.example.tprondagrupo2.data.ThemePreferenceManager;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class RondaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new ThemePreferenceManager(this).applyTheme();
    }
}
