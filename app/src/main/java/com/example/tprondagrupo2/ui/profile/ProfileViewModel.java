package com.example.tprondagrupo2.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.data.repository.PublicationRepository;
import com.example.tprondagrupo2.data.repository.RepoCallback;
import com.example.tprondagrupo2.data.repository.SavedSearchRepository;
import com.example.tprondagrupo2.data.repository.UserRepository;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.model.UserProfile;
import com.example.tprondagrupo2.model.UserProfileUpdateRequest;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import okhttp3.MultipartBody;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final PublicationRepository publicationRepository;

    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<List<SavedSearch>> savedSearches = new MutableLiveData<>();
    private final MutableLiveData<List<Publicacion>> favoritePublications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accountDeleted = new MutableLiveData<>();

    @Inject
    public ProfileViewModel(UserRepository userRepository,
                            SavedSearchRepository savedSearchRepository,
                            PublicationRepository publicationRepository) {
        this.userRepository = userRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.publicationRepository = publicationRepository;
    }

    public LiveData<UserProfile> getUserProfile() { return userProfile; }
    public LiveData<List<SavedSearch>> getSavedSearches() { return savedSearches; }
    public LiveData<List<Publicacion>> getFavoritePublications() { return favoritePublications; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Boolean> getAccountDeleted() { return accountDeleted; }

    public void loadProfile() {
        loading.setValue(true);
        userRepository.getMyProfile(new UserRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                loading.setValue(false);
                userProfile.setValue(profile);
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                error.setValue("Error de red al cargar perfil");
            }
        });
    }

    public void updateProfile(UserProfileUpdateRequest request) {
        loading.setValue(true);
        userRepository.updateMyProfile(request, new UserRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                loading.setValue(false);
                userProfile.setValue(profile);
                toastMessage.setValue("Perfil actualizado correctamente");
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                toastMessage.setValue("Error al actualizar perfil: " + msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void uploadAvatar(MultipartBody.Part photo) {
        loading.setValue(true);
        userRepository.uploadAvatar(photo, new UserRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                loading.setValue(false);
                userProfile.setValue(profile);
                toastMessage.setValue("Foto de perfil actualizada");
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                toastMessage.setValue("Error al subir foto de perfil");
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                toastMessage.setValue("Error de conexión al subir foto");
            }
        });
    }

    public void loadSavedSearches() {
        savedSearchRepository.getSavedSearches(new RepoCallback<List<SavedSearch>>() {
            @Override
            public void onSuccess(List<SavedSearch> list) {
                savedSearches.setValue(list);
            }

            @Override
            public void onError(String msg) {
                error.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                error.setValue("Error de red al cargar búsquedas");
            }
        });
    }

    public void deleteSavedSearch(Long id) {
        if (id == null) return;
        savedSearchRepository.deleteSearch(id, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadSavedSearches();
                toastMessage.setValue("Búsqueda eliminada");
            }

            @Override
            public void onError(String msg) {
                toastMessage.setValue("Error al eliminar la búsqueda");
            }

            @Override
            public void onNetworkError() {
                toastMessage.setValue("Error de red al eliminar la búsqueda");
            }
        });
    }

    public void loadFavorites() {
        publicationRepository.getFavorites(new PublicationRepository.FavoritesCallback() {
            @Override
            public void onSuccess(List<Publicacion> list) {
                favoritePublications.setValue(list);
            }

            @Override
            public void onError(String msg) {
                toastMessage.setValue("Error al cargar favoritos");
            }

            @Override
            public void onNetworkError() {
                toastMessage.setValue("Error de red al cargar favoritos");
            }
        });
    }

    public void deleteAccount() {
        loading.setValue(true);
        userRepository.deleteAccount(new UserRepository.DeleteAccountCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                accountDeleted.setValue(true);
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }

            @Override
            public void onNetworkError() {
                loading.setValue(false);
                error.setValue("Error de red");
            }
        });
    }
}
