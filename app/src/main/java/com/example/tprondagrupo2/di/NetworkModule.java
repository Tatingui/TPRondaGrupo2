package com.example.tprondagrupo2.di;

import com.example.tprondagrupo2.BuildConfig;
import com.example.tprondagrupo2.network.AuthApiService;
import com.example.tprondagrupo2.network.HistorialApiService;
import com.example.tprondagrupo2.network.OfferApiService;
import com.example.tprondagrupo2.network.PublicationDetailApiService;
import com.example.tprondagrupo2.network.PublicationFavoriteApiService;
import com.example.tprondagrupo2.network.PublicationReadApiService;
import com.example.tprondagrupo2.network.PublicationWriteApiService;
import com.example.tprondagrupo2.network.SavedSearchApiService;
import com.example.tprondagrupo2.network.SessionInterceptor;
import com.example.tprondagrupo2.network.UserApiService;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    // URL del backend: se lee de local.properties via BuildConfig.
    // Cada dev configura la suya en local.properties (archivo ignorado por git).
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static final long TIMEOUT_SECONDS = 30;
    private static final Logger LOGGER = Logger.getLogger(NetworkModule.class.getName());

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(SessionInterceptor sessionInterceptor) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(LOGGER::fine);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(sessionInterceptor)
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
    public PublicationReadApiService providePublicationReadService(Retrofit retrofit) {
        return retrofit.create(PublicationReadApiService.class);
    }

    @Provides
    @Singleton
    public PublicationWriteApiService providePublicationWriteService(Retrofit retrofit) {
        return retrofit.create(PublicationWriteApiService.class);
    }

    @Provides
    @Singleton
    public PublicationFavoriteApiService providePublicationFavoriteService(Retrofit retrofit) {
        return retrofit.create(PublicationFavoriteApiService.class);
    }

    @Provides
    @Singleton
    public PublicationDetailApiService providePublicationDetailService(Retrofit retrofit) {
        return retrofit.create(PublicationDetailApiService.class);
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

    @Provides
    @Singleton
    public OfferApiService provideOfferService(Retrofit retrofit) {
        return retrofit.create(OfferApiService.class);
    }

    @Provides
    @Singleton
    public HistorialApiService provideHistorialService(Retrofit retrofit) {
        return retrofit.create(HistorialApiService.class);
    }

}
