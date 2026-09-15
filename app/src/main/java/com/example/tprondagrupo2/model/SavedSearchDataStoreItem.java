package com.example.tprondagrupo2.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SavedSearchDataStoreItem implements Serializable {

    private String id;

    @SerializedName("publication_ids")
    private List<String> publicationIds;

    @SerializedName("has_updates")
    private boolean hasUpdates;

    public SavedSearchDataStoreItem() {
        this.publicationIds = new ArrayList<>();
    }

    public SavedSearchDataStoreItem(String id, List<String> publicationIds, boolean hasUpdates) {
        this.id = id;
        this.publicationIds = publicationIds != null ? publicationIds : new ArrayList<>();
        this.hasUpdates = hasUpdates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPublicationIds() {
        if (publicationIds == null) {
            publicationIds = new ArrayList<>();
        }
        return publicationIds;
    }

    public void setPublicationIds(List<String> publicationIds) {
        this.publicationIds = publicationIds != null ? publicationIds : new ArrayList<>();
    }

    public boolean isHasUpdates() {
        return hasUpdates;
    }

    public void setHasUpdates(boolean hasUpdates) {
        this.hasUpdates = hasUpdates;
    }
}
