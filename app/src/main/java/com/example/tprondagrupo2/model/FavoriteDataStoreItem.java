package com.example.tprondagrupo2.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FavoriteDataStoreItem implements Serializable {

    private String id;

    @SerializedName("has_updates")
    private boolean hasUpdates;

    public FavoriteDataStoreItem() {
    }

    public FavoriteDataStoreItem(String id, boolean hasUpdates) {
        this.id = id;
        this.hasUpdates = hasUpdates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isHasUpdates() {
        return hasUpdates;
    }

    public void setHasUpdates(boolean hasUpdates) {
        this.hasUpdates = hasUpdates;
    }
}
