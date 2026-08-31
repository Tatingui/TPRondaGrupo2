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
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Publication;
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
    
    private PublicationAdapter adapter;
    private List<Publication> favoritePublications = new ArrayList<>();

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

        setupRecyclerView();
        mostrarMiPerfil();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFavorites();
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

        ApiClient.getPublicationService().getFavorites().enqueue(new Callback<List<Publication>>() {
            @Override
            public void onResponse(Call<List<Publication>> call, Response<List<Publication>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    updateFavoritesList(response.body());
                } else {
                    Log.e(TAG, "Error fetching favorites: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Publication>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failure fetching favorites", t);
            }
        });
    }

    private void updateFavoritesList(List<Publication> favorites) {
        favoritePublications.clear();
        if (favorites != null) {
            for (Publication p : favorites) {
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



    private void abrirDetalle(Publication publication) {
        Bundle args = new Bundle();
        Publicacion p = mapearAPublicacionModel(publication);
        p.setFavorite(publication.isFavorite());
        args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, p);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_profile_to_detalle, args);
    }

    private Publicacion mapearAPublicacionModel(Publication publication) {
        Vendedor vendedor = null;
        if (publication.getSellerId() != null) {
            vendedor = new Vendedor(
                    publication.getSellerId().toString(),
                    publication.getSellerName(),
                    4.5, 15, 10, "2 años",
                    publication.getLocation()
            );
        }

        return new Publicacion(
                publication.getId() != null ? publication.getId().toString() : "0",
                publication.getTitle(),
                publication.getImageUrls(),
                publication.getDescription(),
                publication.getCategoryName(),
                publication.getStatus(),
                publication.getPrice(),
                publication.getCreatedAt(),
                vendedor);
    }

    private void onFavoriteClick(Publication publication, int position) {
        boolean isFavorite = publication.isFavorite();
        String pubId = publication.getId().toString();

        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    publication.setFavorite(!isFavorite);
                    if (!publication.isFavorite()) {
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
