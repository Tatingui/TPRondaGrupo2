package com.example.tprondagrupo2.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.ApiClient;
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
    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private RecyclerView rvSavedSearches;
    private TextView tvEmptySavedSearches;
    private SavedSearchAdapter savedSearchAdapter;
    private final List<SavedSearch> savedSearches = new ArrayList<>();
    
    private PublicationAdapter adapter;
    private final List<Publicacion> favoritePublications = new ArrayList<>();

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

        setupSavedSearchesRecyclerView();
        setupRecyclerView();
        mostrarMiPerfil();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedSearches();
        fetchFavorites();
    }

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
                p.setFavorite(true); // Asegurar que el estado sea favorito al cargar
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
                        // Al quitar de favoritos en el perfil, removemos el item de la lista
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

    private void mostrarMiPerfil() {
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
}
