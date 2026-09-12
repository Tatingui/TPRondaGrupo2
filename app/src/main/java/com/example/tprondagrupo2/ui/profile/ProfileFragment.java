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
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.model.UserProfile;
import com.example.tprondagrupo2.model.UserProfileUpdateRequest;
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.ApiClient;
import com.example.tprondagrupo2.network.TokenManager;
import com.example.tprondagrupo2.ui.PublicationAdapter;
import com.example.tprondagrupo2.ui.detalle.DetallePublicacionFragment;
import com.example.tprondagrupo2.ui.detalle.VendedorViewBinder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

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

        // Cerrar sesion: limpia el token y vuelve al login
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            TokenManager.getInstance().clearToken();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_profile_to_login);
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
        ApiClient.getUserService().getMyProfile().enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call,
                                   @NonNull Response<UserProfile> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    mostrarPerfil(currentProfile);
                } else {
                    Log.e(TAG, "Error cargando perfil: " + response.code());
                    // Fallback: mostrar datos minimos
                    mostrarPerfilMock();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Error de conexion cargando perfil", t);
                mostrarPerfilMock();
            }
        });
    }

    private void mostrarPerfil(UserProfile perfil) {
        if (tvNombre != null) tvNombre.setText(perfil.getNombre());

        // Avatar: primera letra del nombre
        if (tvAvatar != null && perfil.getNombre() != null && !perfil.getNombre().isEmpty()) {
            tvAvatar.setText(String.valueOf(perfil.getNombre().charAt(0)).toUpperCase());
        }

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

        // Reputacion: sigue siendo mock (responsabilidad del compañero)
        // Creamos un Vendedor solo para bindear la parte visual de reputacion
        Vendedor mockReputacion = new Vendedor(
                String.valueOf(perfil.getId()),
                perfil.getNombre(),
                5.0, 10, 5,
                perfil.getMiembroDesde() != null ? perfil.getMiembroDesde() : "",
                perfil.getZona() != null ? perfil.getZona() : ""
        );
        VendedorViewBinder.bindReputacion(mockReputacion, tvAvatar, rbReputacion, tvReputacion, tvNivel);
        if (tvVentas != null) tvVentas.setText(getString(R.string.vendedor_ventas, mockReputacion.getCantidadVentas()));
    }

    /**
     * Fallback si falla la carga del perfil real.
     */
    private void mostrarPerfilMock() {
        Vendedor miPerfil = new Vendedor("me", "Mi Usuario", 5.0, 10, 5, "Enero 2024", "Mi Ciudad");

        if (tvNombre != null) tvNombre.setText(miPerfil.getNombre());
        VendedorViewBinder.bindReputacion(miPerfil, tvAvatar, rbReputacion, tvReputacion, tvNivel);

        if (tvVentas != null) tvVentas.setText(getString(R.string.vendedor_ventas, miPerfil.getCantidadVentas()));
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
                        Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
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
        ApiClient.getUserService().updateMyProfile(request).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(@NonNull Call<UserProfile> call,
                                   @NonNull Response<UserProfile> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    mostrarPerfil(currentProfile);
                    Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==================== BUSQUEDAS GUARDADAS ====================

    private void setupSavedSearchesRecyclerView() {
        if (rvSavedSearches == null) return;
        savedSearchAdapter = new SavedSearchAdapter(savedSearches, new SavedSearchAdapter.OnSavedSearchClickListener() {
            @Override
            public void onSearchClick(SavedSearch savedSearch) {
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
                            ApiClient.getSavedSearchService().deleteSearch(savedSearch.getId()).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
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
                                        Toast.makeText(getContext(), "Búsqueda eliminada", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Error al eliminar la búsqueda", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
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
        ApiClient.getSavedSearchService().getSavedSearches().enqueue(new Callback<List<SavedSearch>>() {
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
                }
            }

            @Override
            public void onFailure(Call<List<SavedSearch>> call, Throwable t) {
                Log.e(TAG, "Error al cargar búsquedas guardadas", t);
            }
        });
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

        ApiClient.getPublicationService().getFavorites().enqueue(new Callback<List<Publicacion>>() {
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
        if (favorites != null) {
            for (Publicacion p : favorites) {
                p.setFavorite(true);
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
                        favoritePublications.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, favoritePublications.size());
                        if (favoritePublications.isEmpty() && tvEmpty != null) {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } else {
                        adapter.notifyItemChanged(position);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al actualizar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        };

        if (isFavorite) {
            ApiClient.getPublicationService().unmarkAsFavorite(pubId).enqueue(callback);
        } else {
            ApiClient.getPublicationService().markAsFavorite(pubId).enqueue(callback);
        }
    }
}
