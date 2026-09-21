package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.Pregunta;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PublicationDetailApiService {

    @GET("publications/{id}/questions")
    Call<List<Pregunta>> getQuestions(@Path("id") String id);

    @POST("publications/{id}/view")
    Call<Void> recordView(@Path("id") String id);
}
