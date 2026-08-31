package com.example.tprondagrupo2.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "RondaPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static TokenManager instance;
    private static Context appContext;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void setContext(Context context) {
        appContext = context.getApplicationContext();
    }

    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            if (appContext == null) {
                throw new IllegalStateException("TokenManager must be initialized with setContext(Context) before use.");
            }
            instance = new TokenManager(appContext);
        }
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }
}
