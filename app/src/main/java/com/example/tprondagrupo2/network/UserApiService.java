package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.PerfilPublico;
import com.example.tprondagrupo2.model.UserProfile;
import com.example.tprondagrupo2.model.UserProfileUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApiService {

    @GET("usuarios/me")
    Call<UserProfile> getMyProfile();

    @PUT("usuarios/me")
    Call<UserProfile> updateMyProfile(@Body UserProfileUpdateRequest request);

    @GET("usuarios/{id}/publico")
    Call<PerfilPublico> getPublicProfile(@Path("id") String id);
}
