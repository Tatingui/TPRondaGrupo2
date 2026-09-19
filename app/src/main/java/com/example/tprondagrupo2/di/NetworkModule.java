package com.example.tprondagrupo2.di;

import androidx.annotation.Nullable;

import com.example.tprondagrupo2.network.AuthApiService;
import com.example.tprondagrupo2.network.PublicationApiService;
import com.example.tprondagrupo2.network.SavedSearchApiService;
import com.example.tprondagrupo2.network.SessionManager;
import com.example.tprondagrupo2.network.TokenManager;
import com.example.tprondagrupo2.network.UserApiService;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    // Conexión al backend de la PC mediante adb reverse tcp:8081 tcp:8081.
    // Configurar el túnel después de iniciar el emulador (start-backend.bat lo hace).
    private static final String BASE_URL = "http://localhost:8081/api/";
    private static final long TIMEOUT_SECONDS = 30;
    private static final Logger LOGGER = Logger.getLogger(NetworkModule.class.getName());

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(LOGGER::fine);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = getStoredToken();

                    Request.Builder builder = original.newBuilder();
                    if (token != null && !token.trim().isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }

                    Response response = chain.proceed(builder.build());

                    if (response.code() == 401) {
                        try {
                            TokenManager.getInstance().clearToken();
                        } catch (IllegalStateException ignored) { }
                        SessionManager.getInstance().notifySessionExpired();
                    }

                    return response;
                })
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        LOGGER.fine("Creando Retrofit con BASE_URL=" + BASE_URL);
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public AuthApiService provideAuthService(Retrofit retrofit) {
        return retrofit.create(AuthApiService.class);
    }

    @Provides
    @Singleton
    public PublicationApiService providePublicationService(Retrofit retrofit) {
        return retrofit.create(PublicationApiService.class);
    }

    @Provides
    @Singleton
    public SavedSearchApiService provideSavedSearchService(Retrofit retrofit) {
        return retrofit.create(SavedSearchApiService.class);
    }

    @Provides
    @Singleton
    public UserApiService provideUserService(Retrofit retrofit) {
        return retrofit.create(UserApiService.class);
    }

    @Nullable
    private static String getStoredToken() {
        try {
            return TokenManager.getInstance().getToken();
        } catch (IllegalStateException e) {
            LOGGER.fine("TokenManager no inicializado; se omite Authorization");
            return null;
        }
    }
}
