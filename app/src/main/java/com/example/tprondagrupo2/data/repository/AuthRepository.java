package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.LoginRequest;
import com.example.tprondagrupo2.model.OtpRequest;
import com.example.tprondagrupo2.model.OtpSendRequest;
import com.example.tprondagrupo2.model.RegisterRequest;
import com.example.tprondagrupo2.network.AuthApiService;
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
 * Recibe AuthApiService y TokenManager por constructor (inyeccion de dependencias).
 * AuthApiService es provisto por Hilt via NetworkModule.
 */
public class AuthRepository {

    public interface AuthCallback {
        void onSuccess(String token);
        void onError(String message);
        void onUnverified(String email);
        void onNetworkError();
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
        void onNetworkError();
    }

    private final AuthApiService authApiService;
    private final TokenManager tokenManager;

    public AuthRepository(AuthApiService authApiService, TokenManager tokenManager) {
        this.authApiService = authApiService;
        this.tokenManager = tokenManager;
    }

    /**
     * Ejecuta el login con email y contrasena.
     * Retrofit usa enqueue para ejecutar la llamada en un hilo secundario
     * y entregar el resultado en el hilo principal.
     */
    public void login(String email, String password, boolean keepSession, AuthCallback callback) {
        authApiService.login(new LoginRequest(email, password))
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
     * Registra un nuevo usuario.
     * Si el registro es exitoso, el servidor responde con un mensaje
     * y el fragment navega al OtpFragment para verificar el email.
     */
    public void register(RegisterRequest request, SimpleCallback callback) {
        authApiService.register(request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(extractMessage(body,
                                    "No se pudo crear la cuenta"));
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
     * Verifica el codigo OTP ingresado por el usuario.
     * Si la verificacion es exitosa, persiste el token JWT recibido.
     */
    public void verifyOtp(String email, String code, AuthCallback callback) {
        authApiService.verifyOtp(new OtpRequest(email, code))
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
                            callback.onSuccess(token);
                        } else {
                            callback.onError(extractMessage(body,
                                    "Codigo invalido"));
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
     */
    public void sendOtp(String email, SimpleCallback callback) {
        authApiService.sendOtp(new OtpSendRequest(email))
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

    /**
     * Reenvia el codigo OTP al email indicado.
     */
    public void resendOtp(String email, SimpleCallback callback) {
        authApiService.resendOtp(new OtpSendRequest(email))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> response) {
                        AuthResponse body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()) {
                            callback.onSuccess();
                        } else {
                            callback.onError(extractMessage(body,
                                    "No se pudo reenviar el codigo"));
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

    /**
     * Cierra la sesion del usuario: limpia el token almacenado.
     */
    public void logout() {
        tokenManager.clearToken();
    }
}