package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.PublicationCreateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PublicationApiService {

    @GET("publications")
    Call<PublicationPageResponse> getPublications(
            @Query("search") String search,
            @Query("categoryId") Long categoryId,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("status") String status,
            @Query("location") String location,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @POST("publications")
    Call<Publicacion> createPublication(@Body PublicationCreateRequest request);

    @GET("publications/my")
    Call<List<Publicacion>> getMyPublications();

    @PATCH("publications/{id}/status")
    Call<Publicacion> updatePublicationStatus(@Path("id") Long id, @Query("state") String state);

    @POST("publications/{id}/favorite")
    Call<Void> markAsFavorite(@Path("id") String id);

    @DELETE("publications/{id}/favorite")
    Call<Void> unmarkAsFavorite(@Path("id") String id);

    @GET("publications/favorites")
    Call<List<Publicacion>> getFavorites();
}
