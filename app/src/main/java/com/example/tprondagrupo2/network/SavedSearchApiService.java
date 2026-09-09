package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.SavedSearch;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SavedSearchApiService {

    @GET("saved-searches")
    Call<List<SavedSearch>> getSavedSearches();

    @POST("saved-searches")
    Call<SavedSearch> saveSearch(@Body SavedSearch search);

    @DELETE("saved-searches/{id}")
    Call<Void> deleteSearch(@Path("id") Long id);
}
