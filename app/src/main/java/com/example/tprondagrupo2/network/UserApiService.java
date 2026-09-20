package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.PerfilPublico;
import com.example.tprondagrupo2.model.UserProfile;
import com.example.tprondagrupo2.model.UserProfileUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

import okhttp3.MultipartBody;

public interface UserApiService {

    @GET("usuarios/me")
    Call<UserProfile> getMyProfile();

    @PUT("usuarios/me")
    Call<UserProfile> updateMyProfile(@Body UserProfileUpdateRequest request);

    @GET("usuarios/{id}/publico")
    Call<PerfilPublico> getPublicProfile(@Path("id") String id);

    @DELETE("usuarios/me")
    Call<Void> deleteMyAccount();

    @Multipart
    @POST("usuarios/me/foto")
    Call<UserProfile> uploadProfilePhoto(@Part MultipartBody.Part foto);
}
