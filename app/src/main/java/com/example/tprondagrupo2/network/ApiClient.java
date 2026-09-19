package com.example.tprondagrupo2.network;

import com.example.tprondagrupo2.di.NetworkModule;

import retrofit2.Retrofit;

public class ApiClient {

    private static Retrofit retrofit;

    private ApiClient() {
        // Clase de utilidad, no se instancia
    }

    public static synchronized Retrofit getClient() {
        if (retrofit == null) {
            NetworkModule module = new NetworkModule();
            retrofit = module.provideRetrofit(module.provideOkHttpClient());
        }
        return retrofit;
    }

    public static AuthApiService getAuthService() {
        return getClient().create(AuthApiService.class);
    }

    public static PublicationApiService getPublicationService() {
        return getClient().create(PublicationApiService.class);
    }

    public static SavedSearchApiService getSavedSearchService() {
        return getClient().create(SavedSearchApiService.class);
    }

    public static UserApiService getUserService() {
        return getClient().create(UserApiService.class);
    }
}
