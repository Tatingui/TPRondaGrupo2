package com.example.tprondagrupo2.network;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import com.example.tprondagrupo2.model.FavoriteDataStoreItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;

public class FavoritesDataStoreManager {

    public static final Preferences.Key<String> FAVORITES_KEY = PreferencesKeys.stringKey("favorites");
    private static RxDataStore<Preferences> dataStore;

    private FavoritesDataStoreManager() {
    }

    public static synchronized RxDataStore<Preferences> getInstance(Context context) {
        if (dataStore == null) {
            dataStore = new RxPreferenceDataStoreBuilder(context.getApplicationContext(), "favorites_datastore").build();
        }
        return dataStore;
    }

    public static void addFavorite(Context context, String pubId) {
        if (context == null || pubId == null) return;
        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            String json = mutable.get(FAVORITES_KEY);
            List<FavoriteDataStoreItem> list = parseList(json);

            boolean exists = false;
            for (FavoriteDataStoreItem item : list) {
                if (pubId.equals(item.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                list.add(new FavoriteDataStoreItem(pubId, false));
            }

            mutable.set(FAVORITES_KEY, new Gson().toJson(list));
            return Single.just(mutable);
        }).subscribe();
    }

    public static void removeFavorite(Context context, String pubId) {
        if (context == null || pubId == null) return;
        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            String json = mutable.get(FAVORITES_KEY);
            List<FavoriteDataStoreItem> list = parseList(json);

            list.removeIf(item -> pubId.equals(item.getId()));

            mutable.set(FAVORITES_KEY, new Gson().toJson(list));
            return Single.just(mutable);
        }).subscribe();
    }

    public static void setHasUpdates(Context context, String pubId, boolean hasUpdates) {
        if (context == null || pubId == null) return;
        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            String json = mutable.get(FAVORITES_KEY);
            List<FavoriteDataStoreItem> list = parseList(json);

            boolean found = false;
            for (FavoriteDataStoreItem item : list) {
                if (pubId.equals(item.getId())) {
                    item.setHasUpdates(hasUpdates);
                    found = true;
                    break;
                }
            }
            if (!found) {
                list.add(new FavoriteDataStoreItem(pubId, hasUpdates));
            }

            mutable.set(FAVORITES_KEY, new Gson().toJson(list));
            return Single.just(mutable);
        }).subscribe();
    }

    public static Map<String, Boolean> getHasUpdatesMap(Context context) {
        if (context == null) return new HashMap<>();
        try {
            RxDataStore<Preferences> ds = getInstance(context);
            Preferences prefs = ds.data().blockingFirst();
            String json = prefs.get(FAVORITES_KEY);
            List<FavoriteDataStoreItem> list = parseList(json);
            Map<String, Boolean> map = new HashMap<>();
            for (FavoriteDataStoreItem item : list) {
                if (item.getId() != null) {
                    map.put(item.getId(), item.isHasUpdates());
                }
            }
            return map;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static List<FavoriteDataStoreItem> parseList(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<ArrayList<FavoriteDataStoreItem>>() {}.getType();
            List<FavoriteDataStoreItem> result = new Gson().fromJson(json, listType);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
