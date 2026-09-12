package com.example.tprondagrupo2.network;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());

    // Conexion al backend de la PC mediante adb reverse tcp:8081 tcp:8081.
    // Configurar el tunel despues de iniciar el emulador (start-backend.bat lo hace).
    private static final String BASE_URL = "http://localhost:8081/api/";

    private static final long TIMEOUT_SECONDS = 30;

    private static Retrofit retrofit;

    private ApiClient() {
        // Clase de utilidad, no se instancia
    }

    public static synchronized Retrofit getClient() {
        if (retrofit == null) {
            LOGGER.fine("Creando Retrofit con BASE_URL=" + BASE_URL);

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                    LOGGER::fine);
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
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

                        okhttp3.Response response = chain.proceed(builder.build());

                        // Si el backend devuelve 401, el token venció o es inválido.
                        // Limpiamos el token y notificamos a la UI para redirigir al login.
                        if (response.code() == 401) {
                            try {
                                TokenManager.getInstance().clearToken();
                            } catch (IllegalStateException ignored) { }
                            SessionManager.getInstance().notifySessionExpired();
                        }

                        return response;
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

    public static SavedSearchApiService getSavedSearchService() {
        return getClient().create(SavedSearchApiService.class);
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
