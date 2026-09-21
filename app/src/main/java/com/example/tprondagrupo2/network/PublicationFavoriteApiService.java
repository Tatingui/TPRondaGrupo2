package com.example.tprondagrupo2.network;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PublicationFavoriteApiService {

    @POST("publications/{id}/favorite")
    Call<Void> markAsFavorite(@Path("id") String id);

    @DELETE("publications/{id}/favorite")
    Call<Void> unmarkAsFavorite(@Path("id") String id);
}
