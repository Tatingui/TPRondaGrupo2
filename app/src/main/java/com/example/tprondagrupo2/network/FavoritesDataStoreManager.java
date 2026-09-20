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
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.core.Single;

public class FavoritesDataStoreManager {

    public static final Preferences.Key<String> FAVORITES_KEY = PreferencesKeys.stringKey("favorites");
    private static RxDataStore<Preferences> dataStore;
    private static final Map<String, Boolean> memoryCache = new ConcurrentHashMap<>();
    private static boolean isInitialized = false;

    private FavoritesDataStoreManager() {
    }

    public static synchronized RxDataStore<Preferences> getInstance(Context context) {
        if (dataStore == null) {
            dataStore = new RxPreferenceDataStoreBuilder(context.getApplicationContext(), "favorites_datastore").build();
            loadCacheAsync();
        }
        return dataStore;
    }

    private static synchronized void loadCacheAsync() {
        if (isInitialized || dataStore == null) return;
        dataStore.data().firstOrError().subscribe(prefs -> {
            String json = prefs.get(FAVORITES_KEY);
            List<FavoriteDataStoreItem> list = parseList(json);
            synchronized (memoryCache) {
                memoryCache.clear();
                for (FavoriteDataStoreItem item : list) {
                    if (item.getId() != null) {
                        memoryCache.put(item.getId(), item.isHasUpdates());
                    }
                }
                isInitialized = true;
            }
        }, throwable -> {
            // ignore
        });
    }

    public static void addFavorite(Context context, String pubId) {
        if (context == null || pubId == null) return;
        synchronized (memoryCache) {
            memoryCache.put(pubId, false);
        }

        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            synchronized (memoryCache) {
                List<FavoriteDataStoreItem> list = new ArrayList<>();
                for (Map.Entry<String, Boolean> entry : memoryCache.entrySet()) {
                    list.add(new FavoriteDataStoreItem(entry.getKey(), entry.getValue()));
                }
                mutable.set(FAVORITES_KEY, new Gson().toJson(list));
            }
            return Single.just(mutable);
        }).subscribe();
    }

    public static void removeFavorite(Context context, String pubId) {
        if (context == null || pubId == null) return;
        synchronized (memoryCache) {
            memoryCache.remove(pubId);
        }

        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            synchronized (memoryCache) {
                List<FavoriteDataStoreItem> list = new ArrayList<>();
                for (Map.Entry<String, Boolean> entry : memoryCache.entrySet()) {
                    list.add(new FavoriteDataStoreItem(entry.getKey(), entry.getValue()));
                }
                mutable.set(FAVORITES_KEY, new Gson().toJson(list));
            }
            return Single.just(mutable);
        }).subscribe();
    }

    public static void setHasUpdates(Context context, String pubId, boolean hasUpdates) {
        if (context == null || pubId == null) return;
        synchronized (memoryCache) {
            memoryCache.put(pubId, hasUpdates);
        }

        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            synchronized (memoryCache) {
                List<FavoriteDataStoreItem> list = new ArrayList<>();
                for (Map.Entry<String, Boolean> entry : memoryCache.entrySet()) {
                    list.add(new FavoriteDataStoreItem(entry.getKey(), entry.getValue()));
                }
                mutable.set(FAVORITES_KEY, new Gson().toJson(list));
            }
            return Single.just(mutable);
        }).subscribe();
    }

    public static Map<String, Boolean> getHasUpdatesMap(Context context) {
        if (context != null) {
            getInstance(context);
        }
        synchronized (memoryCache) {
            return new HashMap<>(memoryCache);
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
