package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.Oferta;
import com.example.tprondagrupo2.model.OfertaRequest;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.PublicationCreateRequest;
import com.example.tprondagrupo2.model.TextoRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @GET("publications/{id}")
    Call<Publicacion> getPublication(@Path("id") String id);

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

    @POST("publications/{id}/view")
    Call<Void> recordView(@Path("id") String id);

    @DELETE("publications/{id}")
    Call<Void> deletePublication(@Path("id") Long id);

    // Preguntas y ofertas desde el detalle

    @GET("publications/{id}/questions")
    Call<List<Pregunta>> getQuestions(@Path("id") String id);

    @POST("publications/{id}/questions")
    Call<Pregunta> askQuestion(@Path("id") String id, @Body TextoRequest request);

    @PUT("publications/questions/{questionId}/answer")
    Call<Pregunta> answerQuestion(@Path("questionId") Long questionId, @Body TextoRequest request);

    @POST("publications/{id}/offers")
    Call<Oferta> makeOffer(@Path("id") String id, @Body OfertaRequest request);
}
