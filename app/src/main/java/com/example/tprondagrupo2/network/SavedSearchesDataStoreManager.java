package com.example.tprondagrupo2.network;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import com.example.tprondagrupo2.model.SavedSearchDataStoreItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.core.Single;

public class SavedSearchesDataStoreManager {

    public static final Preferences.Key<String> SAVED_SEARCHES_KEY = PreferencesKeys.stringKey("savedSearches");
    private static RxDataStore<Preferences> dataStore;
    private static final Map<String, SavedSearchDataStoreItem> memoryCache = new ConcurrentHashMap<>();
    private static boolean isInitialized = false;

    private SavedSearchesDataStoreManager() {
    }

    public static synchronized RxDataStore<Preferences> getInstance(Context context) {
        if (dataStore == null) {
            dataStore = new RxPreferenceDataStoreBuilder(context.getApplicationContext(), "saved_searches_datastore").build();
            loadCacheAsync();
        }
        return dataStore;
    }

    private static synchronized void loadCacheAsync() {
        if (isInitialized || dataStore == null) return;
        dataStore.data().firstOrError().subscribe(prefs -> {
            String json = prefs.get(SAVED_SEARCHES_KEY);
            List<SavedSearchDataStoreItem> list = parseList(json);
            synchronized (memoryCache) {
                memoryCache.clear();
                for (SavedSearchDataStoreItem item : list) {
                    if (item.getId() != null) {
                        memoryCache.put(item.getId(), item);
                    }
                }
                isInitialized = true;
            }
        }, throwable -> {
            // ignore
        });
    }

    public static void saveSearch(Context context, String searchId, List<String> pubIds) {
        if (context == null || searchId == null) return;
        synchronized (memoryCache) {
            SavedSearchDataStoreItem item = memoryCache.get(searchId);
            if (item != null) {
                item.setPublicationIds(pubIds);
                item.setHasUpdates(false);
            } else {
                memoryCache.put(searchId, new SavedSearchDataStoreItem(searchId, pubIds, false));
            }
        }

        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            synchronized (memoryCache) {
                List<SavedSearchDataStoreItem> list = new ArrayList<>(memoryCache.values());
                mutable.set(SAVED_SEARCHES_KEY, new Gson().toJson(list));
            }
            return Single.just(mutable);
        }).subscribe();
    }

    public static void removeSearch(Context context, String searchId) {
        if (context == null || searchId == null) return;
        synchronized (memoryCache) {
            memoryCache.remove(searchId);
        }

        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            synchronized (memoryCache) {
                List<SavedSearchDataStoreItem> list = new ArrayList<>(memoryCache.values());
                mutable.set(SAVED_SEARCHES_KEY, new Gson().toJson(list));
            }
            return Single.just(mutable);
        }).subscribe();
    }

    public static void updateSearchUpdates(Context context, String searchId, List<String> newPubIds, boolean hasUpdates) {
        if (context == null || searchId == null) return;
        synchronized (memoryCache) {
            SavedSearchDataStoreItem item = memoryCache.get(searchId);
            if (item != null) {
                item.setPublicationIds(newPubIds);
                item.setHasUpdates(hasUpdates);
            } else {
                memoryCache.put(searchId, new SavedSearchDataStoreItem(searchId, newPubIds, hasUpdates));
            }
        }

        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            synchronized (memoryCache) {
                List<SavedSearchDataStoreItem> list = new ArrayList<>(memoryCache.values());
                mutable.set(SAVED_SEARCHES_KEY, new Gson().toJson(list));
            }
            return Single.just(mutable);
        }).subscribe();
    }

    public static Map<String, SavedSearchDataStoreItem> getSavedSearchesMap(Context context) {
        if (context != null) {
            getInstance(context);
        }
        synchronized (memoryCache) {
            return new HashMap<>(memoryCache);
        }
    }

    private static List<SavedSearchDataStoreItem> parseList(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<ArrayList<SavedSearchDataStoreItem>>() {}.getType();
            List<SavedSearchDataStoreItem> result = new Gson().fromJson(json, listType);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
