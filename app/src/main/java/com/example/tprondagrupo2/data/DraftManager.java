package com.example.tprondagrupo2.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.example.tprondagrupo2.model.PublicationCreateRequest;

public class DraftManager {
    private static final String PREF_NAME = "publication_draft_prefs";
    private static final String KEY_DRAFT = "publication_draft_json";

    private final SharedPreferences prefs;
    private final Gson gson;

    public DraftManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveDraft(PublicationCreateRequest request) {
        String json = gson.toJson(request);
        prefs.edit().putString(KEY_DRAFT, json).apply();
    }

    public PublicationCreateRequest loadDraft() {
        String json = prefs.getString(KEY_DRAFT, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, PublicationCreateRequest.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void clearDraft() {
        prefs.edit().remove(KEY_DRAFT).apply();
    }
}
