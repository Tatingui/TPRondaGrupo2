package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.Publicacion;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PublicationReadApiService {

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

    @GET("publications/{id}")
    Call<Publicacion> getPublication(@Path("id") String id);

    @GET("publications/my")
    Call<List<Publicacion>> getMyPublications();

    @GET("publications/favorites")
    Call<List<Publicacion>> getFavorites();
}
