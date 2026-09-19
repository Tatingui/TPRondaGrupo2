package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.OfferCreateRequest;
import com.example.tprondagrupo2.model.OfferRespondRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OfferApiService {

    @POST("offers")
    Call<Offer> createOffer(@Body OfferCreateRequest request);

    @GET("offers/sent")
    Call<List<Offer>> getSentOffers();

    @GET("offers/received")
    Call<List<Offer>> getReceivedOffers();

    @PATCH("offers/{id}/respond")
    Call<Offer> respondOffer(@Path("id") Long id, @Body OfferRespondRequest request);
}
