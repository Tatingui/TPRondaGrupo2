package com.example.tprondagrupo2.data.repository;

import androidx.annotation.NonNull;

import com.example.tprondagrupo2.model.PerfilPublico;
import com.example.tprondagrupo2.model.UserProfile;
import com.example.tprondagrupo2.model.UserProfileUpdateRequest;
import com.example.tprondagrupo2.network.TokenManager;
import com.example.tprondagrupo2.network.UserApiService;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para operaciones del perfil de usuario.
 *
 * Patron Repository: actua como intermediario entre la capa de datos (Retrofit/API)
 * y la capa de presentacion (Fragments). Los Fragments no conocen los detalles
 * de como se hacen las llamadas HTTP.
 *
 * Recibe UserApiService y TokenManager por constructor, provisto por Hilt
 * via NetworkModule.
 */
public class UserRepository {

    public interface ProfileCallback {
        void onSuccess(UserProfile profile);
        void onError(String message);
        void onNetworkError();
    }

    public interface PublicProfileCallback {
        void onSuccess(PerfilPublico perfil);
        void onError(String message);
        void onNetworkError();
    }

    private final UserApiService userApiService;
    private final TokenManager tokenManager;

    @Inject
    public UserRepository(UserApiService userApiService, TokenManager tokenManager) {
        this.userApiService = userApiService;
        this.tokenManager = tokenManager;
    }

    /**
     * Obtiene el perfil del usuario logueado (GET /usuarios/me).
     */
    public void getMyProfile(ProfileCallback callback) {
        userApiService.getMyProfile().enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call,
                                   @NonNull Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error cargando perfil");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    /**
     * Actualiza el perfil del usuario logueado (PUT /usuarios/me).
     */
    public void updateMyProfile(UserProfileUpdateRequest request, ProfileCallback callback) {
        userApiService.updateMyProfile(request).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call,
                                   @NonNull Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al actualizar el perfil");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    /**
     * Obtiene el perfil publico de otro usuario (GET /usuarios/{id}/publico).
     */
    public void getPublicProfile(String userId, PublicProfileCallback callback) {
        if (userId == null) return;

        userApiService.getPublicProfile(userId).enqueue(new Callback<PerfilPublico>() {
            @Override
            public void onResponse(@NonNull Call<PerfilPublico> call,
                                   @NonNull Response<PerfilPublico> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("No se pudo cargar el perfil");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PerfilPublico> call, @NonNull Throwable t) {
                callback.onNetworkError();
            }
        });
    }

    /**
     * Cierra la sesion del usuario: limpia el token almacenado.
     */
    public void logout() {
        tokenManager.clearToken();
    }

    public interface DeleteAccountCallback {
        void onSuccess();
        void onError(String message);
    }

    public void deleteAccount(DeleteAccountCallback callback) {
        userApiService.deleteMyAccount().enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    tokenManager.clearToken();
                    callback.onSuccess();
                } else {
                    callback.onError("Error al eliminar la cuenta");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                callback.onError("Error de red: " + t.getMessage());
            }
        });
    }
}