package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.LoginRequest;
import com.example.tprondagrupo2.model.OtpRequest;
import com.example.tprondagrupo2.model.OtpSendRequest;
import com.example.tprondagrupo2.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/otp/send")
    Call<AuthResponse> sendOtp(@Body OtpSendRequest request);

    @POST("auth/otp/verify")
    Call<AuthResponse> verifyOtp(@Body OtpRequest request);

    @POST("auth/otp/resend")
    Call<AuthResponse> resendOtp(@Body OtpSendRequest request);
}
