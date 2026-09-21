package com.example.tprondagrupo2.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.tprondagrupo2.BuildConfig;
import com.example.tprondagrupo2.di.NetworkModule;

import org.junit.Before;
import org.junit.Test;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Test que valida la provisión de dependencias de red mediante Hilt (NetworkModule).
 */
public class ApiClientTest {

    private static final String BASE_URL_ESPERADA = BuildConfig.BASE_URL;

    private NetworkModule networkModule;
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;

    @Before
    public void setUp() {
        // Inicializa el módulo de red e inyecta la instancia de OkHttpClient y Retrofit para los tests
        networkModule = new NetworkModule();
        okHttpClient = networkModule.provideOkHttpClient(new SessionInterceptor(() -> null, () -> {}));
        retrofit = networkModule.provideRetrofit(okHttpClient);
    }

    @Test
    public void testProvideOkHttpClientNoDevuelveNull() {
        // Valida que el método provideOkHttpClient() de Hilt construya e instancie OkHttpClient correctamente
        assertNotNull(okHttpClient);
    }

    @Test
    public void testProvideRetrofitNoDevuelveNull() {
        // Valida que el método provideRetrofit() de Hilt cree la instancia de Retrofit sin fallar ni retornar null
        assertNotNull(retrofit);
    }

    @Test
    public void testProvideAuthServiceNoDevuelveNull() {
        // Valida que Hilt pueda generar e inyectar la implementación del servicio de autenticación (AuthApiService)
        AuthApiService authApiService = networkModule.provideAuthService(retrofit);
        assertNotNull(authApiService);
    }

    @Test
    public void testProvidePublicationServiceNoDevuelveNull() {
        // Valida que Hilt pueda generar e inyectar la implementación del servicio de publicaciones (PublicationApiService)
        PublicationApiService publicationApiService = networkModule.providePublicationService(retrofit);
        assertNotNull(publicationApiService);
    }

    @Test
    public void testProvideSavedSearchServiceNoDevuelveNull() {
        // Valida que Hilt pueda generar e inyectar la implementación del servicio de búsquedas guardadas (SavedSearchApiService)
        SavedSearchApiService savedSearchApiService = networkModule.provideSavedSearchService(retrofit);
        assertNotNull(savedSearchApiService);
    }

    @Test
    public void testProvideUserServiceNoDevuelveNull() {
        // Valida que Hilt pueda generar e inyectar la implementación del servicio de usuarios y perfiles (UserApiService)
        UserApiService userApiService = networkModule.provideUserService(retrofit);
        assertNotNull(userApiService);
    }

    @Test
    public void testBaseUrlEsLaEsperada() {
        // Valida que Retrofit use la URL configurada para esta compilación.
        assertEquals(BASE_URL_ESPERADA, retrofit.baseUrl().toString());
    }

    @Test
    public void testBaseUrlTerminaConBarra() {
        // Valida el requisito de Retrofit de que la base URL termine en /
        String baseUrl = retrofit.baseUrl().toString();
        assertEquals('/', baseUrl.charAt(baseUrl.length() - 1));
    }
}
