package com.example.tprondagrupo2.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tprondagrupo2.data.repository.PublicationRepository;
import com.example.tprondagrupo2.data.repository.RepoCallback;
import com.example.tprondagrupo2.data.repository.SavedSearchRepository;
import com.example.tprondagrupo2.db.dao.PublicacionDao;
import com.example.tprondagrupo2.db.entity.PublicacionEntity;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.network.PublicationPageResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final PublicationRepository publicationRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final PublicacionDao publicacionDao;

    private final MutableLiveData<List<Publicacion>> publications = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Set<String>> favoriteIds = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> offlineBannerVisible = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<SavedSearch> savedSearchSuccess = new MutableLiveData<>();

    @Inject
    public HomeViewModel(PublicationRepository publicationRepository,
                         SavedSearchRepository savedSearchRepository,
                         PublicacionDao publicacionDao) {
        this.publicationRepository = publicationRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.publicacionDao = publicacionDao;
    }

    public LiveData<List<Publicacion>> getPublications() { return publications; }
    public LiveData<Set<String>> getFavoriteIds() { return favoriteIds; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getIsLastPage() { return isLastPage; }
    public LiveData<Boolean> getOfflineBannerVisible() { return offlineBannerVisible; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<SavedSearch> getSavedSearchSuccess() { return savedSearchSuccess; }

    public void fetchFavoriteIds(Runnable onComplete) {
        publicationRepository.getFavorites(new PublicationRepository.FavoritesCallback() {
            @Override
            public void onSuccess(List<Publicacion> result) {
                Set<String> favs = new HashSet<>();
                if (result != null) {
                    for (Publicacion p : result) {
                        if (p.getId() != null) favs.add(p.getId());
                    }
                }
                favoriteIds.setValue(favs);
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onError(String msg) {
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onNetworkError() {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    public void loadPublications(String search, Long categoryId, Double minPrice, Double maxPrice,
                                String status, String location, int page, int size, String sort,
                                boolean isConnected) {
        if (!isConnected) {
            offlineBannerVisible.setValue(true);
            loadFromCache(search, categoryId, status, location, minPrice, maxPrice, sort);
            return;
        }

        offlineBannerVisible.setValue(false);
        loading.setValue(true);

        publicationRepository.getPublications(search, categoryId, minPrice, maxPrice, status, location, page, size, sort,
                new PublicationRepository.PublicationPageCallback() {
                    @Override
                    public void onSuccess(PublicationPageResponse pageResponse) {
                        loading.setValue(false);
                        if (pageResponse != null && pageResponse.getContent() != null) {
                            List<Publicacion> newItems = pageResponse.getContent();
                            Set<String> favs = favoriteIds.getValue() != null ? favoriteIds.getValue() : new HashSet<>();
                            for (Publicacion p : newItems) {
                                p.setFavorite(p.getId() != null && favs.contains(p.getId()));
                            }

                            List<Publicacion> currentList = publications.getValue() != null ? publications.getValue() : new ArrayList<>();
                            if (page == 0) {
                                currentList = new ArrayList<>(newItems);
                                saveToCache(newItems);
                            } else {
                                currentList.addAll(newItems);
                            }
                            publications.setValue(currentList);
                            isLastPage.setValue(pageResponse.isLast());
                        } else {
                            if (page == 0) publications.setValue(new ArrayList<>());
                            isLastPage.setValue(true);
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        loading.setValue(false);
                        loadFromCache(search, categoryId, status, location, minPrice, maxPrice, sort);
                    }

                    @Override
                    public void onNetworkError() {
                        loading.setValue(false);
                        loadFromCache(search, categoryId, status, location, minPrice, maxPrice, sort);
                    }
                });
    }

    private void saveToCache(List<Publicacion> items) {
        if (items == null) return;
        List<PublicacionEntity> entities = items.stream()
                .map(PublicacionEntity::fromModel)
                .collect(Collectors.toList());
        publicacionDao.insertAll(entities);
    }

    private void loadFromCache(String search, Long categoryId, String status, String location,
                               Double minPrice, Double maxPrice, String sort) {
        List<PublicacionEntity> entities;
        if (search != null && !search.isEmpty()) {
            entities = publicacionDao.searchByTitle(search);
        } else {
            entities = publicacionDao.getAll();
        }

        List<Publicacion> cachedItems = entities.stream()
                .map(PublicacionEntity::toModel)
                .collect(Collectors.toList());

        List<Publicacion> filtered = new ArrayList<>();
        for (Publicacion p : cachedItems) {
            boolean matches = true;
            if (categoryId != null && (p.getCategoryName() == null || !p.getCategoryName().equals(String.valueOf(categoryId)))) matches = false;
            if (matches && status != null && !status.equals(p.getStatus())) matches = false;
            if (matches && minPrice != null && p.getPrice() < minPrice) matches = false;
            if (matches && maxPrice != null && p.getPrice() > maxPrice) matches = false;
            if (matches && location != null && !location.equals(p.getLocation())) matches = false;

            if (matches) filtered.add(p);
        }

        if (sort != null) {
            if (sort.equals("price,asc")) {
                filtered.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
            } else if (sort.equals("price,desc")) {
                filtered.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            } else if (sort.equals("createdAt,desc")) {
                filtered.sort((p1, p2) -> {
                    Long id1 = p1.getIdLong();
                    Long id2 = p2.getIdLong();
                    if (id1 == null && id2 == null) return 0;
                    if (id1 == null) return 1;
                    if (id2 == null) return -1;
                    return Long.compare(id2, id1);
                });
            }
        }

        publications.setValue(filtered);
        isLastPage.setValue(true);
        offlineBannerVisible.setValue(true);
    }

    public void toggleFavorite(Publicacion publicacion) {
        if (publicacion == null || publicacion.getId() == null) return;
        boolean currentlyFavorite = publicacion.isFavorite();
        String pubId = publicacion.getId();

        publicationRepository.toggleFavorite(pubId, currentlyFavorite, new PublicationRepository.ToggleFavoriteCallback() {
            @Override
            public void onSuccess() {
                publicacion.setFavorite(!currentlyFavorite);
                Set<String> favs = favoriteIds.getValue() != null ? new HashSet<>(favoriteIds.getValue()) : new HashSet<>();
                if (publicacion.isFavorite()) {
                    favs.add(pubId);
                } else {
                    favs.remove(pubId);
                }
                favoriteIds.setValue(favs);

                List<Publicacion> currentList = publications.getValue();
                if (currentList != null) {
                    publications.setValue(new ArrayList<>(currentList));
                }
            }

            @Override
            public void onError(String msg) {
                toastMessage.setValue("Error al actualizar favorito");
            }

            @Override
            public void onNetworkError() {
                toastMessage.setValue("Error de conexión");
            }
        });
    }

    public void saveSearch(SavedSearch search) {
        savedSearchRepository.saveSearch(search, new RepoCallback<SavedSearch>() {
            @Override
            public void onSuccess(SavedSearch result) {
                savedSearchSuccess.setValue(result);
            }

            @Override
            public void onError(String msg) {
                toastMessage.setValue("Error al guardar la búsqueda");
            }

            @Override
            public void onNetworkError() {
                toastMessage.setValue("Error de conexión al guardar");
            }
        });
    }
}
