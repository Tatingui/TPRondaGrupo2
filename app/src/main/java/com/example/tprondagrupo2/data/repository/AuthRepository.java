package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.LoginRequest;
import com.example.tprondagrupo2.model.OtpSendRequest;
import com.example.tprondagrupo2.network.ApiClient;
import com.example.tprondagrupo2.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para operaciones de autenticacion.
 *
 * Patron Repository: actua como intermediario entre la capa de datos (Retrofit/API)
 * y la capa de presentacion (Fragments). El Fragment no conoce los detalles
 * de como se hace la llamada HTTP ni como se persiste el token.
 *
 * Responsabilidades:
 * - Ejecutar las llamadas a la API de auth via Retrofit
 * - Persistir el token en TokenManager cuando el login es exitoso
 * - Traducir las respuestas de la API a resultados tipados para la UI
 *
 * Hoy obtiene el ApiService via ApiClient (singleton estatico).
 * Post-migracion a Hilt, recibiria AuthApiService y TokenManager via @Inject.
 */
public class AuthRepository {

    /**
     * Callback generico para operaciones de auth.
     * Simplifica el manejo en el Fragment: en vez de un Callback<AuthResponse>
     * con toda la logica de parseo, recibe resultados ya procesados.
     */
    public interface AuthCallback {
        /** Login exitoso. token es el JWT recibido del servidor. */
        void onSuccess(String token);

        /** Login fallido. message contiene el motivo del servidor o un fallback. */
        void onError(String message);

        /** El usuario no esta verificado, debe pasar por OTP. */
        void onUnverified(String email);

        /** Error de red, timeout, o servidor inalcanzable. */
        void onNetworkError();
    }

    /** Callback simplificado para operaciones que solo necesitan exito/error. */
    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
        void onNetworkError();
    }

    private final TokenManager tokenManager;

    public AuthRepository(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    /**
     * Ejecuta el login con email y contrasena.
     *
     * Flujo:
     * 1. Envia LoginRequest al endpoint POST /api/auth/login via Retrofit
     * 2. Si la respuesta es exitosa y trae token, lo guarda en TokenManager
     * 3. Si el mensaje contiene "no verificado", notifica que falta OTP
     * 4. Cualquier otro error se propaga como mensaje al callback
     *
     * Retrofit usa un Callback asincrono (enqueue) para no bloquear el main thread.
     * El interceptor de OkHttp en ApiClient agrega el header Authorization
     * automaticamente en las siguientes requests.
     */
    public void login(String email, String password, boolean keepSession, AuthCallback callback) {
        ApiClient.getAuthService().login(new LoginRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        AuthResponse body = response.body();

                        if (response.isSuccessful() && body != null && body.isSuccess()) {
                            String token = body.getToken();
                            if (token != null) {
                                tokenManager.saveToken(token);
                            }
                            tokenManager.setKeepSession(keepSession);
                            callback.onSuccess(token);
                        } else {
                            String msg = extractMessage(body, "");
                            if (msg.contains("no verificado")) {
                                callback.onUnverified(email);
                            } else {
                                callback.onError(extractMessage(body,
                                        "No se pudo iniciar sesion"));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call,
                                          @NonNull Throwable t) {
                        callback.onNetworkError();
                    }
                });
    }

    /**
     * Solicita el envio de un codigo OTP al email indicado.
     * Endpoint: POST /api/auth/otp/send
     */
    public void sendOtp(String email, SimpleCallback callback) {
        ApiClient.getAuthService().sendOtp(new OtpSendRequest(email))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(extractMessage(body,
                                    "No se pudo enviar el codigo"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call,
                                          @NonNull Throwable t) {
                        callback.onNetworkError();
                    }
                });
    }

    @NonNull
    private String extractMessage(@Nullable AuthResponse body, String fallback) {
        if (body != null && body.getMessage() != null && !body.getMessage().isEmpty()) {
            return body.getMessage();
        }
        return fallback;
    }
}
