package com.example.tprondagrupo2.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {
    private static final String PREF_NAME = "RondaPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_KEEP_SESSION = "keep_session";

    private static final String ENCRYPTED_PREF_NAME = "encrypted_ronda_prefs";
    private static final String KEY_ENCRYPTED_TOKEN = "encrypted_token";

    private final SharedPreferences prefs;
    private final Context context;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ── Token normal (SharedPreferences plano) ──

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    // ── Flag biométrico ──

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    // ── Flag mantener sesión ──

    public boolean isKeepSession() {
        return prefs.getBoolean(KEY_KEEP_SESSION, false);
    }

    public void setKeepSession(boolean keep) {
        prefs.edit().putBoolean(KEY_KEEP_SESSION, keep).apply();
    }

    // ── Token encriptado (EncryptedSharedPreferences) ──

    public void saveEncryptedToken(String token) {
        try {
            buildEncryptedPrefs().edit().putString(KEY_ENCRYPTED_TOKEN, token).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getEncryptedToken() {
        try {
            return buildEncryptedPrefs().getString(KEY_ENCRYPTED_TOKEN, null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearEncryptedToken() {
        try {
            buildEncryptedPrefs().edit().remove(KEY_ENCRYPTED_TOKEN).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SharedPreferences buildEncryptedPrefs() throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        return EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}
