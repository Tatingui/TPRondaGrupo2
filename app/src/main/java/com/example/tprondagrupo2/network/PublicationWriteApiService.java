package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.OfertaRequest;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.Pregunta;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.PublicationCreateRequest;
import com.example.tprondagrupo2.model.TextoRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PublicationWriteApiService {

    @POST("publications")
    Call<Publicacion> createPublication(@Body PublicationCreateRequest request);

    @PATCH("publications/{id}/status")
    Call<Publicacion> updatePublicationStatus(@Path("id") Long id, @Query("state") String state);

    @DELETE("publications/{id}")
    Call<Void> deletePublication(@Path("id") Long id);

    @POST("publications/{id}/questions")
    Call<Pregunta> askQuestion(@Path("id") String id, @Body TextoRequest request);

    @PUT("publications/questions/{questionId}/answer")
    Call<Pregunta> answerQuestion(@Path("questionId") Long questionId, @Body TextoRequest request);

    @POST("publications/{id}/offers")
    Call<Offer> makeOffer(@Path("id") String id, @Body OfertaRequest request);
}
