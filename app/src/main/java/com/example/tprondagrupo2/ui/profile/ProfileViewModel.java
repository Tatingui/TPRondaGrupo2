package com.example.tprondagrupo2.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.data.repository.RepoCallback;
import com.example.tprondagrupo2.data.repository.SavedSearchRepository;
import com.example.tprondagrupo2.data.repository.UserRepository;
import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.model.UserProfile;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final SavedSearchRepository savedSearchRepository;

    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<List<SavedSearch>> savedSearches = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accountDeleted = new MutableLiveData<>();

    @Inject
    public ProfileViewModel(UserRepository userRepository, SavedSearchRepository savedSearchRepository) {
        this.userRepository = userRepository;
        this.savedSearchRepository = savedSearchRepository;
    }

    public LiveData<UserProfile> getUserProfile() { return userProfile; }
    public LiveData<List<SavedSearch>> getSavedSearches() { return savedSearches; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
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
