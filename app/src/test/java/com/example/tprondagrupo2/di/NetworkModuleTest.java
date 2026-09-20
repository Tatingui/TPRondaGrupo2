package com.example.tprondagrupo2.di;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.tprondagrupo2.BuildConfig;
import com.example.tprondagrupo2.network.AuthApiService;
import com.example.tprondagrupo2.network.PublicationApiService;
import com.example.tprondagrupo2.network.SavedSearchApiService;
import com.example.tprondagrupo2.network.UserApiService;

import org.junit.Before;
import org.junit.Test;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class NetworkModuleTest {

    private static final String BASE_URL_ESPERADA = BuildConfig.BASE_URL;

    private NetworkModule networkModule;
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;

    @Before
    public void setUp() {
        networkModule = new NetworkModule();
        okHttpClient = networkModule.provideOkHttpClient();
        retrofit = networkModule.provideRetrofit(okHttpClient);
    }

    @Test
    public void testProvideOkHttpClientNoDevuelveNull() {
        assertNotNull(okHttpClient);
    }

    @Test
    public void testProvideRetrofitNoDevuelveNull() {
        assertNotNull(retrofit);
    }

    @Test
    public void testBaseUrlEsLaEsperada() {
        assertEquals(BASE_URL_ESPERADA, retrofit.baseUrl().toString());
    }

    @Test
    public void testBaseUrlTerminaConBarra() {
        String baseUrl = retrofit.baseUrl().toString();
        assertEquals('/', baseUrl.charAt(baseUrl.length() - 1));
    }

    @Test
    public void testProvideAuthServiceNoDevuelveNull() {
        AuthApiService authApiService = networkModule.provideAuthService(retrofit);
        assertNotNull(authApiService);
    }

    @Test
    public void testProvidePublicationServiceNoDevuelveNull() {
        PublicationApiService publicationApiService = networkModule.providePublicationService(retrofit);
        assertNotNull(publicationApiService);
    }

    @Test
    public void testProvideSavedSearchServiceNoDevuelveNull() {
        SavedSearchApiService savedSearchApiService = networkModule.provideSavedSearchService(retrofit);
        assertNotNull(savedSearchApiService);
    }

    @Test
    public void testProvideUserServiceNoDevuelveNull() {
        UserApiService userApiService = networkModule.provideUserService(retrofit);
        assertNotNull(userApiService);
    }
}
