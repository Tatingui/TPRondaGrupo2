package com.example.tprondagrupo2.network;

import retrofit2.Call;
import retrofit2.http.GET;
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
}
