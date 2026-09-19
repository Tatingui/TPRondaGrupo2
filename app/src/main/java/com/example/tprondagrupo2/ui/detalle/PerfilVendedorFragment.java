package com.example.tprondagrupo2.ui.detalle;

import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.PerfilPublico;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.ApiClient;
import com.example.tprondagrupo2.ui.PublicationAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Perfil público de un usuario: nombre, avatar con inicial, reputación,
 * antigüedad en la plataforma y sus publicaciones activas.
 * Recibe el vendedor por Bundle y después completa los datos con la API.
 */
public class PerfilVendedorFragment extends Fragment {

    public static final String ARG_VENDEDOR = "vendedor";

    private TextView tvAvatar;
    private TextView tvNombre;
    private TextView tvNivel;
    private RatingBar rbReputacion;
    private TextView tvReputacion;
    private TextView tvVentas;
    private TextView tvMiembroDesde;
    private TextView tvUbicacion;

    // Publicaciones activas
    private ProgressBar pbPublicaciones;
    private TextView tvSinPublicaciones;
    private PublicationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_vendedor, container, false);
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
        pbPublicaciones = view.findViewById(R.id.pbPerfilPublicaciones);
        tvSinPublicaciones = view.findViewById(R.id.tvPerfilSinPublicaciones);

        // Reutilizamos el mismo adapter del Home. Tocar una publicación abre su detalle.
        RecyclerView rvPublicaciones = view.findViewById(R.id.rvPerfilPublicaciones);
        rvPublicaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PublicationAdapter(new ArrayList<>(), this::abrirDetalle, null);
        rvPublicaciones.setAdapter(adapter);

        Vendedor vendedor = obtenerVendedor();
        if (vendedor == null) {
            Toast.makeText(getContext(), "No se encontró el vendedor", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        // Primero mostramos lo que llegó por el Bundle y después lo completamos con la API
        mostrarVendedor(vendedor);
        cargarPerfilPublico(vendedor.getId());
    }

    private Vendedor obtenerVendedor() {
        if (getArguments() != null) {
            Object extra = getArguments().getSerializable(ARG_VENDEDOR);
            if (extra instanceof Vendedor) {
                return (Vendedor) extra;
            }
        }
        return null;
    }

    private void cargarPerfilPublico(String id) {
        if (id == null) return;

        pbPublicaciones.setVisibility(View.VISIBLE);
        ApiClient.getUserService().getPublicProfile(id).enqueue(new Callback<PerfilPublico>() {
            @Override
            public void onResponse(@NonNull Call<PerfilPublico> call, @NonNull Response<PerfilPublico> response) {
                if (!isAdded()) return;
                pbPublicaciones.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    PerfilPublico perfil = response.body();
                    mostrarVendedor(perfil);
                    mostrarPublicaciones(perfil);
                } else {
                    Toast.makeText(getContext(), "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PerfilPublico> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                // Sin conexión: nos quedamos con los datos que llegaron por el Bundle
                pbPublicaciones.setVisibility(View.GONE);
                tvSinPublicaciones.setVisibility(View.VISIBLE);
                tvSinPublicaciones.setText(R.string.perfil_sin_conexion);
            }
        });
    }

    private void mostrarVendedor(@NonNull Vendedor vendedor) {
        tvNombre.setText(vendedor.getNombre());
        VendedorViewBinder.bindReputacion(vendedor, tvAvatar, rbReputacion, tvReputacion, tvNivel);

        tvVentas.setText(getString(R.string.vendedor_operaciones,
                vendedor.getCantidadVentas(), vendedor.getCantidadCompras()));
        if (vendedor.getMiembroDesde() != null) {
            tvMiembroDesde.setVisibility(View.VISIBLE);
            tvMiembroDesde.setText(getString(R.string.vendedor_miembro_desde, vendedor.getMiembroDesde()));
        } else {
            tvMiembroDesde.setVisibility(View.GONE);
        }

        if (vendedor.getUbicacion() != null && !vendedor.getUbicacion().isEmpty()) {
            tvUbicacion.setVisibility(View.VISIBLE);
            tvUbicacion.setText(getString(R.string.perfil_vendedor_ubicacion, vendedor.getUbicacion()));
        } else {
            tvUbicacion.setVisibility(View.GONE);
        }
    }

    private void mostrarPublicaciones(@NonNull PerfilPublico perfil) {
        adapter.updateList(perfil.getPublicacionesActivas());

        if (perfil.getPublicacionesActivas().isEmpty()) {
            tvSinPublicaciones.setVisibility(View.VISIBLE);
            tvSinPublicaciones.setText(R.string.perfil_sin_publicaciones);
        } else {
            tvSinPublicaciones.setVisibility(View.GONE);
        }
    }

    private void abrirDetalle(Publicacion publicacion) {
        Bundle args = new Bundle();
        args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, publicacion);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_perfil_vendedor_to_detalle, args);
    }
}
