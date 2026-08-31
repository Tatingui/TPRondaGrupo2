package com.example.tprondagrupo2.network;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "RONDA_API";

    private static final String BASE_URL = "http://localhost:8081/api/";

    private static final long TIMEOUT_SECONDS = 30;

    private static Retrofit retrofit;

    private ApiClient() {
        // Clase de utilidad, no se instancia
    }

    public static synchronized Retrofit getClient() {
        if (retrofit == null) {
            Log.d(TAG, "Creando Retrofit con BASE_URL=" + BASE_URL);

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                    message -> Log.d(TAG, message));
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = TokenManager.getInstance().getToken();

                        if (token != null) {
                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("Authorization", "Bearer " + token);
                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }

                        return chain.proceed(original);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthApiService getAuthService() {
        return getClient().create(AuthApiService.class);
    }

    public static PublicationApiService getPublicationService() {
        return getClient().create(PublicationApiService.class);
    }
}
