package com.example.tprondagrupo2.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.data.repository.UserRepository;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.model.SavedSearchDataStoreItem;
import com.example.tprondagrupo2.model.UserProfile;
import com.example.tprondagrupo2.model.UserProfileUpdateRequest;
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.FavoritesDataStoreManager;
import com.example.tprondagrupo2.network.NetworkObserver;
import com.example.tprondagrupo2.network.PublicationApiService;
import com.example.tprondagrupo2.network.PublicationPageResponse;
import com.example.tprondagrupo2.network.SavedSearchApiService;
import com.example.tprondagrupo2.network.SavedSearchesDataStoreManager;
import com.example.tprondagrupo2.network.TokenManager;
import com.example.tprondagrupo2.network.UserApiService;
import com.example.tprondagrupo2.ui.PublicationAdapter;
import com.example.tprondagrupo2.ui.detalle.DetallePublicacionFragment;
import com.example.tprondagrupo2.ui.detalle.VendedorViewBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    @Inject
    UserApiService userApiService;

    @Inject
    SavedSearchApiService savedSearchApiService;

    @Inject
    PublicationApiService publicationApiService;

    /** Repositorio que centraliza las operaciones de perfil y logout */
    private UserRepository userRepository;

    private TextView tvAvatar;
    private TextView tvNombre;
    private TextView tvNivel;
    private RatingBar rbReputacion;
    private TextView tvReputacion;
    private TextView tvVentas;
    private TextView tvMiembroDesde;
    private TextView tvUbicacion;
    private TextView tvEmail;
    private TextView tvTelefono;
    private Button btnEditProfile;
    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private RecyclerView rvSavedSearches;
    private TextView tvEmptySavedSearches;
    private SavedSearchAdapter savedSearchAdapter;
    private final List<SavedSearch> savedSearches = new ArrayList<>();

    private PublicationAdapter adapter;
    private final List<Publicacion> favoritePublications = new ArrayList<>();

    /** Perfil cargado del backend para usar en edicion */
    private UserProfile currentProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRepository = new UserRepository(userApiService, TokenManager.getInstance());

        tvAvatar = view.findViewById(R.id.tvPerfilAvatar);
        tvNombre = view.findViewById(R.id.tvPerfilNombre);
        tvNivel = view.findViewById(R.id.tvPerfilNivel);
        rbReputacion = view.findViewById(R.id.rbPerfilReputacion);
        tvReputacion = view.findViewById(R.id.tvPerfilReputacion);
        tvVentas = view.findViewById(R.id.tvPerfilVentas);
        tvMiembroDesde = view.findViewById(R.id.tvPerfilMiembroDesde);
        tvUbicacion = view.findViewById(R.id.tvPerfilUbicacion);
        tvEmail = view.findViewById(R.id.tvPerfilEmail);
        tvTelefono = view.findViewById(R.id.tvPerfilTelefono);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvSavedSearches = view.findViewById(R.id.rvSavedSearches);
        tvEmptySavedSearches = view.findViewById(R.id.tvEmptySavedSearches);

        view.findViewById(R.id.btnGoPublish).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_publish)
        );
        view.findViewById(R.id.btnGoMyPublications).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_my_publications)
        );
        view.findViewById(R.id.btnGoMyOffers).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_my_offers)
        );

        com.google.android.material.switchmaterial.SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);
        com.example.tprondagrupo2.data.ThemePreferenceManager themeManager = new com.example.tprondagrupo2.data.ThemePreferenceManager(requireContext());
        switchDarkMode.setChecked(themeManager.isDarkModeEnabled());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeManager.setDarkModeEnabled(isChecked);
            requireActivity().recreate();
        });

        // Cerrar sesion: delega al repositorio y navega al login
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            userRepository.logout();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_profile_to_login);
        });

        // Borrar cuenta: muestra dialogo de confirmacion
        view.findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Borrar cuenta")
                    .setMessage("¿Estás seguro? Se eliminarán todos tus datos, publicaciones, ofertas y calificaciones. Esta acción no se puede deshacer.")
                    .setPositiveButton("Sí, borrar", (dialog, which) -> {
                        userRepository.deleteAccount(new com.example.tprondagrupo2.data.repository.UserRepository.DeleteAccountCallback() {
                            @Override
                            public void onSuccess() {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        android.widget.Toast.makeText(requireContext(), "Cuenta eliminada", android.widget.Toast.LENGTH_SHORT).show();
                                        NavHostFragment.findNavController(ProfileFragment.this)
                                                .navigate(R.id.action_profile_to_login);
                                    });
                                }
                            }

                            @Override
                            public void onError(String message) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() ->
                                            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditDialog());
        }

        setupSavedSearchesRecyclerView();
        setupRecyclerView();
        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedSearches();
        fetchFavorites();
    }

    // ==================== PERFIL REAL DESDE API ====================

    private void loadProfile() {
        if (!NetworkObserver.isCurrentlyConnected(requireContext())) {
            android.widget.Toast.makeText(getContext(), "Sin conexión: no se puede cargar el perfil", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.getMyProfile(new UserRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (!isAdded()) return;
                currentProfile = profile;
                mostrarPerfil(currentProfile);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Log.e(TAG, message);
                mostrarPerfilMock();
            }

            @Override
            public void onNetworkError() {
                if (!isAdded()) return;
                Log.e(TAG, "Error de conexion cargando perfil");
                mostrarPerfilMock();
            }
        });
    }

    private void mostrarPerfil(UserProfile perfil) {
        if (tvNombre != null) tvNombre.setText(perfil.getNombre());

        // Email
        if (tvEmail != null) {
            tvEmail.setText(perfil.getEmail());
            tvEmail.setVisibility(View.VISIBLE);
        }

        // Telefono
        if (tvTelefono != null) {
            if (perfil.getTelefono() != null && !perfil.getTelefono().isEmpty()) {
                tvTelefono.setText(perfil.getTelefono());
                tvTelefono.setVisibility(View.VISIBLE);
            } else {
                tvTelefono.setVisibility(View.GONE);
            }
        }

        // Miembro desde
        if (tvMiembroDesde != null && perfil.getMiembroDesde() != null) {
            tvMiembroDesde.setText(getString(R.string.vendedor_miembro_desde, perfil.getMiembroDesde()));
        }

        // Ubicacion / Zona
        if (tvUbicacion != null) {
            if (perfil.getZona() != null && !perfil.getZona().isEmpty()) {
                tvUbicacion.setVisibility(View.VISIBLE);
                tvUbicacion.setText(getString(R.string.perfil_vendedor_ubicacion, perfil.getZona()));
            } else {
                tvUbicacion.setVisibility(View.GONE);
            }
        }

        // Reputacion: UserProfile ahora implementa ReputacionInfo,
        // se pasa directo a VendedorViewBinder sin crear un Vendedor intermedio
        VendedorViewBinder.bindReputacion(perfil, tvAvatar, rbReputacion, tvReputacion, tvNivel);
        if (tvVentas != null) {
            tvVentas.setText(getString(R.string.vendedor_operaciones,
                    perfil.getCantidadVentas(), perfil.getCantidadCompras()));
        }
    }

    /**
     * Fallback si falla la carga del perfil real.
     */
    private void mostrarPerfilMock() {
        // Sin reputacion inventada: si no se pudo cargar el perfil, se muestra en 0
        Vendedor miPerfil = new Vendedor("me", "Mi Usuario", 0, 0, 0, "Enero 2024", "Mi Ciudad");

        if (tvNombre != null) tvNombre.setText(miPerfil.getNombre());
        VendedorViewBinder.bindReputacion(miPerfil, tvAvatar, rbReputacion, tvReputacion, tvNivel);

        if (tvVentas != null) {
            tvVentas.setText(getString(R.string.vendedor_operaciones,
                    miPerfil.getCantidadVentas(), miPerfil.getCantidadCompras()));
        }
        if (tvMiembroDesde != null) tvMiembroDesde.setText(getString(R.string.vendedor_miembro_desde, miPerfil.getMiembroDesde()));

        if (tvUbicacion != null) {
            if (miPerfil.getUbicacion() != null && !miPerfil.getUbicacion().isEmpty()) {
                tvUbicacion.setVisibility(View.VISIBLE);
                tvUbicacion.setText(getString(R.string.perfil_vendedor_ubicacion, miPerfil.getUbicacion()));
            } else {
                tvUbicacion.setVisibility(View.GONE);
            }
        }
    }

    // ==================== EDICION DEL PERFIL ====================

    private void showEditDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText etEditNombre = dialogView.findViewById(R.id.etEditNombre);
        EditText etEditTelefono = dialogView.findViewById(R.id.etEditTelefono);
        AutoCompleteTextView etEditZona = dialogView.findViewById(R.id.etEditZona);

        // Configurar AutoComplete de zonas
        String[] zonas = getResources().getStringArray(R.array.zonas_argentina);
        ArrayAdapter<String> zonaAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                zonas
        );
        etEditZona.setAdapter(zonaAdapter);

        // Pre-cargar con datos actuales
        if (currentProfile != null) {
            etEditNombre.setText(currentProfile.getNombre());
            etEditTelefono.setText(currentProfile.getTelefono());
            etEditZona.setText(currentProfile.getZona());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Editar perfil")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = etEditNombre.getText().toString().trim();
                    String telefono = etEditTelefono.getText().toString().trim();
                    String zona = etEditZona.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(getContext(), "El nombre no puede estar vac\u00edo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserProfileUpdateRequest update = new UserProfileUpdateRequest(
                            nombre,
                            telefono.isEmpty() ? null : telefono,
                            zona.isEmpty() ? null : zona
                    );
                    saveProfile(update);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveProfile(UserProfileUpdateRequest request) {
        userRepository.updateMyProfile(request, new UserRepository.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (!isAdded()) return;
                currentProfile = profile;
                mostrarPerfil(currentProfile);
                Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNetworkError() {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexi\u00f3n", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==================== BUSQUEDAS GUARDADAS ====================

    private void setupSavedSearchesRecyclerView() {
        if (rvSavedSearches == null) return;
        savedSearchAdapter = new SavedSearchAdapter(savedSearches, new SavedSearchAdapter.OnSavedSearchClickListener() {
            @Override
            public void onSearchClick(SavedSearch savedSearch) {
                if (savedSearch != null && savedSearch.getId() != null && getContext() != null) {
                    SavedSearchDataStoreItem dsItem = SavedSearchesDataStoreManager.getSavedSearchesMap(requireContext())
                            .get(String.valueOf(savedSearch.getId()));
                    List<String> knownIds = dsItem != null ? dsItem.getPublicationIds() : new ArrayList<>();
                    SavedSearchesDataStoreManager.updateSearchUpdates(requireContext(), String.valueOf(savedSearch.getId()), knownIds, false);
                    savedSearch.setHasUpdates(false);
                }
                Bundle args = new Bundle();
                args.putSerializable("saved_search", savedSearch);
                NavHostFragment.findNavController(ProfileFragment.this)
                        .navigate(R.id.action_profile_to_home, args);
            }

            @Override
            public void onDeleteClick(SavedSearch savedSearch, int position) {
                if (getContext() == null || savedSearch == null || savedSearch.getId() == null) return;
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.perfil_mis_busquedas)
                        .setMessage(R.string.eliminar_busqueda_confirm)
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            savedSearchApiService.deleteSearch(savedSearch.getId()).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        SavedSearchesDataStoreManager.removeSearch(requireContext(), String.valueOf(savedSearch.getId()));
                                        if (position >= 0 && position < savedSearches.size()) {
                                            savedSearches.remove(position);
                                            if (savedSearchAdapter != null) {
                                                savedSearchAdapter.notifyItemRemoved(position);
                                                savedSearchAdapter.notifyItemRangeChanged(position, savedSearches.size());
                                            }
                                        } else {
                                            loadSavedSearches();
                                        }
                                        if (savedSearches.isEmpty() && tvEmptySavedSearches != null) {
                                            tvEmptySavedSearches.setVisibility(View.VISIBLE);
                                        }
                                        Toast.makeText(getContext(), "B\u00fasqueda eliminada", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Error al eliminar la b\u00fasqueda", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(getContext(), "Error de conexi\u00f3n", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
        rvSavedSearches.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSavedSearches.setAdapter(savedSearchAdapter);
        rvSavedSearches.setNestedScrollingEnabled(false);
    }

    private void loadSavedSearches() {
        if (getContext() == null) return;
        if (!NetworkObserver.isCurrentlyConnected(requireContext())) {
            android.widget.Toast.makeText(getContext(), "Sin conexión: no se pueden cargar búsquedas guardadas", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        savedSearchApiService.getSavedSearches().enqueue(new Callback<List<SavedSearch>>() {
            @Override
            public void onResponse(Call<List<SavedSearch>> call, Response<List<SavedSearch>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savedSearches.clear();
                    savedSearches.addAll(response.body());
                    if (savedSearchAdapter != null) {
                        savedSearchAdapter.notifyDataSetChanged();
                    }
                    if (tvEmptySavedSearches != null) {
                        tvEmptySavedSearches.setVisibility(savedSearches.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    checkUpdatesForSavedSearches(savedSearches);
                }
            }

            @Override
            public void onFailure(Call<List<SavedSearch>> call, Throwable t) {
                Log.e(TAG, "Error al cargar b\u00fasquedas guardadas", t);
            }
        });
    }

    private void checkUpdatesForSavedSearches(List<SavedSearch> list) {
        if (list == null || getContext() == null) return;
        Map<String, SavedSearchDataStoreItem> dsMap = SavedSearchesDataStoreManager.getSavedSearchesMap(requireContext());

        for (int i = 0; i < list.size(); i++) {
            final SavedSearch search = list.get(i);
            final int pos = i;
            if (search.getId() == null) continue;

            final String searchIdStr = String.valueOf(search.getId());
            final SavedSearchDataStoreItem dsItem = dsMap.get(searchIdStr);

            publicationApiService.getPublications(
                    search.getQuery() != null && !search.getQuery().isEmpty() ? search.getQuery() : null,
                    search.getCategoryId(),
                    search.getMinPrice(),
                    search.getMaxPrice(),
                    search.getCondition(),
                    search.getLocation(),
                    0,
                    50,
                    search.getSort() != null ? search.getSort() : "createdAt,desc"
            ).enqueue(new Callback<PublicationPageResponse>() {
                @Override
                public void onResponse(Call<PublicationPageResponse> call, Response<PublicationPageResponse> response) {
                    if (isAdded() && response.isSuccessful() && response.body() != null) {
                        List<Publicacion> currentItems = response.body().getContent();
                        List<String> currentIds = new ArrayList<>();
                        if (currentItems != null) {
                            for (Publicacion p : currentItems) {
                                if (p.getId() != null) {
                                    currentIds.add(p.getId());
                                }
                            }
                        }

                        List<String> knownIds = dsItem != null ? dsItem.getPublicationIds() : new ArrayList<>();
                        boolean hasNew = false;

                        if (dsItem == null) {
                            SavedSearchesDataStoreManager.saveSearch(requireContext(), searchIdStr, currentIds);
                        } else {
                            for (String id : currentIds) {
                                if (!knownIds.contains(id)) {
                                    hasNew = true;
                                    break;
                                }
                            }
                            if (hasNew || dsItem.isHasUpdates()) {
                                search.setHasUpdates(true);
                                SavedSearchesDataStoreManager.updateSearchUpdates(requireContext(), searchIdStr, currentIds, true);
                                if (savedSearchAdapter != null && pos >= 0 && pos < savedSearches.size()) {
                                    savedSearchAdapter.notifyItemChanged(pos);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<PublicationPageResponse> call, Throwable t) {
                    // Ignorar fallo de busqueda en segundo plano
                }
            });
        }
    }

    // ==================== FAVORITOS ====================

    private void setupRecyclerView() {
        if (rvFavorites == null) return;
        adapter = new PublicationAdapter(favoritePublications, this::abrirDetalle, this::onFavoriteClick);
        rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvFavorites.setAdapter(adapter);
        rvFavorites.setNestedScrollingEnabled(false);
    }

    private void fetchFavorites() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);

        publicationApiService.getFavorites().enqueue(new Callback<List<Publicacion>>() {
            @Override
            public void onResponse(Call<List<Publicacion>> call, Response<List<Publicacion>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    updateFavoritesList(response.body());
                } else {
                    Log.e(TAG, "Error fetching favorites: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Publicacion>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failure fetching favorites", t);
            }
        });
    }

    private void updateFavoritesList(List<Publicacion> favorites) {
        favoritePublications.clear();
        if (favorites != null && getContext() != null) {
            Map<String, Boolean> hasUpdatesMap = FavoritesDataStoreManager.getHasUpdatesMap(requireContext());
            for (Publicacion p : favorites) {
                p.setFavorite(true);
                if (p.getLastSeenPrice() != null && p.getPrice() < p.getLastSeenPrice()) {
                    FavoritesDataStoreManager.setHasUpdates(requireContext(), p.getId(), true);
                    p.setHasUpdates(true);
                } else if (p.getId() != null && hasUpdatesMap.containsKey(p.getId())) {
                    p.setHasUpdates(Boolean.TRUE.equals(hasUpdatesMap.get(p.getId())));
                }
            }
            favoritePublications.addAll(favorites);
        }
        if (adapter != null) adapter.notifyDataSetChanged();

        if (favoritePublications.isEmpty()) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        }
    }

    private void abrirDetalle(Publicacion publicacion) {
        Bundle args = new Bundle();
        args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, publicacion);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_profile_to_detalle, args);
    }

    private void onFavoriteClick(Publicacion publicacion, int position) {
        boolean isFavorite = publicacion.isFavorite();
        String pubId = publicacion.getId();

        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    publicacion.setFavorite(!isFavorite);
                    if (!publicacion.isFavorite()) {
                        FavoritesDataStoreManager.removeFavorite(requireContext(), pubId);
                        favoritePublications.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, favoritePublications.size());
                        if (favoritePublications.isEmpty() && tvEmpty != null) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } else {
                        FavoritesDataStoreManager.addFavorite(requireContext(), pubId);
                        adapter.notifyItemChanged(position);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al actualizar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexi\u00f3n", Toast.LENGTH_SHORT).show();
            }
        };

        if (isFavorite) {
            publicationApiService.unmarkAsFavorite(pubId).enqueue(callback);
        } else {
            publicationApiService.markAsFavorite(pubId).enqueue(callback);
        }
    }
}
