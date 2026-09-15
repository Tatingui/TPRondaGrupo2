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

import io.reactivex.rxjava3.core.Single;

public class SavedSearchesDataStoreManager {

    public static final Preferences.Key<String> SAVED_SEARCHES_KEY = PreferencesKeys.stringKey("savedSearches");
    private static RxDataStore<Preferences> dataStore;

    private SavedSearchesDataStoreManager() {
    }

    public static synchronized RxDataStore<Preferences> getInstance(Context context) {
        if (dataStore == null) {
            dataStore = new RxPreferenceDataStoreBuilder(context.getApplicationContext(), "saved_searches_datastore").build();
        }
        return dataStore;
    }

    public static void saveSearch(Context context, String searchId, List<String> pubIds) {
        if (context == null || searchId == null) return;
        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            String json = mutable.get(SAVED_SEARCHES_KEY);
            List<SavedSearchDataStoreItem> list = parseList(json);

            boolean found = false;
            for (SavedSearchDataStoreItem item : list) {
                if (searchId.equals(item.getId())) {
                    item.setPublicationIds(pubIds);
                    item.setHasUpdates(false);
                    found = true;
                    break;
                }
            }
            if (!found) {
                list.add(new SavedSearchDataStoreItem(searchId, pubIds, false));
            }

            mutable.set(SAVED_SEARCHES_KEY, new Gson().toJson(list));
            return Single.just(mutable);
        }).subscribe();
    }

    public static void removeSearch(Context context, String searchId) {
        if (context == null || searchId == null) return;
        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            String json = mutable.get(SAVED_SEARCHES_KEY);
            List<SavedSearchDataStoreItem> list = parseList(json);

            list.removeIf(item -> searchId.equals(item.getId()));

            mutable.set(SAVED_SEARCHES_KEY, new Gson().toJson(list));
            return Single.just(mutable);
        }).subscribe();
    }

    public static void updateSearchUpdates(Context context, String searchId, List<String> newPubIds, boolean hasUpdates) {
        if (context == null || searchId == null) return;
        RxDataStore<Preferences> ds = getInstance(context);
        ds.updateDataAsync(prefsIn -> {
            MutablePreferences mutable = prefsIn.toMutablePreferences();
            String json = mutable.get(SAVED_SEARCHES_KEY);
            List<SavedSearchDataStoreItem> list = parseList(json);

            boolean found = false;
            for (SavedSearchDataStoreItem item : list) {
                if (searchId.equals(item.getId())) {
                    item.setPublicationIds(newPubIds);
                    item.setHasUpdates(hasUpdates);
                    found = true;
                    break;
                }
            }
            if (!found) {
                list.add(new SavedSearchDataStoreItem(searchId, newPubIds, hasUpdates));
            }

            mutable.set(SAVED_SEARCHES_KEY, new Gson().toJson(list));
            return Single.just(mutable);
        }).subscribe();
    }

    public static Map<String, SavedSearchDataStoreItem> getSavedSearchesMap(Context context) {
        if (context == null) return new HashMap<>();
        try {
            RxDataStore<Preferences> ds = getInstance(context);
            Preferences prefs = ds.data().blockingFirst();
            String json = prefs.get(SAVED_SEARCHES_KEY);
            List<SavedSearchDataStoreItem> list = parseList(json);
            Map<String, SavedSearchDataStoreItem> map = new HashMap<>();
            for (SavedSearchDataStoreItem item : list) {
                if (item.getId() != null) {
                    map.put(item.getId(), item);
                }
            }
            return map;
        } catch (Exception e) {
            return new HashMap<>();
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
