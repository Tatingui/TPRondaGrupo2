package com.example.tprondagrupo2.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.example.tprondagrupo2.model.PublicationCreateRequest;
import com.example.tprondagrupo2.network.TokenManager;
import javax.inject.Inject;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class DraftManager {
    /** Hilt provee la fábrica; cada vista captura su propia sesión, sin singleton de borrador. */
    public static final class Factory {
        private final Context context;
        private final TokenManager tokenManager;

        @Inject
        public Factory(@ApplicationContext Context context, TokenManager tokenManager) {
            this.context = context;
            this.tokenManager = tokenManager;
        }

        public DraftManager create() {
            return new DraftManager(context, tokenManager.getToken());
        }
    }

    private static final String PREF_NAME = "publication_draft_prefs";
    private static final String KEY_DRAFT = "publication_draft_json";
    private static final String KEY_ADDRESS_SESSION = "address_session";

    private final SharedPreferences prefs;
    private final Gson gson;
    private final String sessionFingerprint;

    public DraftManager(Context context, String sessionToken) {
        this(context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE), sessionToken);
    }

    DraftManager(SharedPreferences prefs, String sessionToken) {
        this.prefs = prefs;
        this.gson = new Gson();
        this.sessionFingerprint = fingerprint(sessionToken);
    }

    public void saveDraft(PublicationCreateRequest request) {
        String json = gson.toJson(request);
        prefs.edit().putString(KEY_DRAFT, json).putString(KEY_ADDRESS_SESSION, sessionFingerprint).apply();
    }

    public PublicationCreateRequest loadDraft() {
        String json = prefs.getString(KEY_DRAFT, null);
        if (json == null) return null;
        try {
            PublicationCreateRequest draft = gson.fromJson(json, PublicationCreateRequest.class);
            // La direccion privada no se restaura en otra sesion ni desde borradores antiguos sin dueño.
            if (draft != null && (sessionFingerprint == null
                    || !sessionFingerprint.equals(prefs.getString(KEY_ADDRESS_SESSION, null)))) {
                draft.setAddress(null);
            }
            return draft;
        } catch (Exception e) {
            return null;
        }
    }

    public void clearDraft() {
        prefs.edit().remove(KEY_DRAFT).remove(KEY_ADDRESS_SESSION).apply();
    }

    private static String fingerprint(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hash) result.append(String.format(java.util.Locale.ROOT, "%02x", b & 0xff));
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
