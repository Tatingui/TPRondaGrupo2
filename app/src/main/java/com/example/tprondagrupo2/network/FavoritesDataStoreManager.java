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

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class FavoritesDataStoreManager {

    public static final Preferences.Key<String> FAVORITES_KEY = PreferencesKeys.stringKey("favorites");
    private final RxDataStore<Preferences> dataStore;
    private final Map<String, Boolean> memoryCache = new ConcurrentHashMap<>();
    private boolean isInitialized = false;

    @Inject
    public FavoritesDataStoreManager(@ApplicationContext Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(
                context.getApplicationContext(), "favorites_datastore").build();
        loadCacheAsync();
    }

    private synchronized void loadCacheAsync() {
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

    public void addFavorite(String pubId) {
        if (pubId == null) return;
        synchronized (memoryCache) {
            memoryCache.put(pubId, false);
        }

        dataStore.updateDataAsync(prefsIn -> {
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

    public void removeFavorite(String pubId) {
        if (pubId == null) return;
        synchronized (memoryCache) {
            memoryCache.remove(pubId);
        }

        dataStore.updateDataAsync(prefsIn -> {
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

    public void setHasUpdates(String pubId, boolean hasUpdates) {
        if (pubId == null) return;
        synchronized (memoryCache) {
            memoryCache.put(pubId, hasUpdates);
        }

        dataStore.updateDataAsync(prefsIn -> {
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

    public Map<String, Boolean> getHasUpdatesMap() {
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
