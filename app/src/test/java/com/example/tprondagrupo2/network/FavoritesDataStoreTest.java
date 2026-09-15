package com.example.tprondagrupo2.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.tprondagrupo2.model.FavoriteDataStoreItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesDataStoreTest {

    @Test
    public void testFavoriteDataStoreItemSerialization() {
        FavoriteDataStoreItem item = new FavoriteDataStoreItem("10", false);
        Gson gson = new Gson();
        String json = gson.toJson(item);

        assertTrue(json.contains("\"id\":\"10\""));
        assertTrue(json.contains("\"has_updates\":false"));

        FavoriteDataStoreItem deserialized = gson.fromJson(json, FavoriteDataStoreItem.class);
        assertEquals("10", deserialized.getId());
        assertFalse(deserialized.isHasUpdates());
    }

    @Test
    public void testFavoritesListSerialization() {
        List<FavoriteDataStoreItem> list = new ArrayList<>();
        list.add(new FavoriteDataStoreItem("1", false));
        list.add(new FavoriteDataStoreItem("2", true));

        Gson gson = new Gson();
        String json = gson.toJson(list);

        Type listType = new TypeToken<ArrayList<FavoriteDataStoreItem>>() {}.getType();
        List<FavoriteDataStoreItem> result = gson.fromJson(json, listType);

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
        assertFalse(result.get(0).isHasUpdates());
        assertEquals("2", result.get(1).getId());
        assertTrue(result.get(1).isHasUpdates());
    }
}
