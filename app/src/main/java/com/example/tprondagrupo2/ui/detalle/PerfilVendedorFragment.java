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
import com.example.tprondagrupo2.data.repository.UserRepository;
import com.example.tprondagrupo2.model.PerfilPublico;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.ReputacionInfo;
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.TokenManager;
import com.example.tprondagrupo2.network.UserApiService;
import com.example.tprondagrupo2.ui.PublicationAdapter;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Perfil publico de un usuario: nombre, avatar con inicial, reputacion,
 * antiguedad en la plataforma y sus publicaciones activas.
 * Recibe el vendedor por Bundle y despues completa los datos con la API.
 */
@AndroidEntryPoint
public class PerfilVendedorFragment extends Fragment {

    public static final String ARG_VENDEDOR = "vendedor";

    @Inject
    UserApiService userApiService;

    /** Repositorio que centraliza las operaciones de perfil */
    private UserRepository userRepository;

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

        userRepository = new UserRepository(userApiService, TokenManager.getInstance());

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

        // Reutilizamos el mismo adapter del Home. Tocar una publicacion abre su detalle.
        RecyclerView rvPublicaciones = view.findViewById(R.id.rvPerfilPublicaciones);
        rvPublicaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PublicationAdapter(new ArrayList<>(), this::abrirDetalle, null);
        rvPublicaciones.setAdapter(adapter);

        Vendedor vendedor = obtenerVendedor();
        if (vendedor == null) {
            Toast.makeText(getContext(), "No se encontro el vendedor", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        // Primero mostramos lo que llego por el Bundle y despues lo completamos con la API
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
        userRepository.getPublicProfile(id, new UserRepository.PublicProfileCallback() {
            @Override
            public void onSuccess(PerfilPublico perfil) {
                if (!isAdded()) return;
                pbPublicaciones.setVisibility(View.GONE);
                mostrarVendedor(perfil);
                mostrarPublicaciones(perfil);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                pbPublicaciones.setVisibility(View.GONE);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNetworkError() {
                if (!isAdded()) return;
                // Sin conexion: nos quedamos con los datos que llegaron por el Bundle
                pbPublicaciones.setVisibility(View.GONE);
                tvSinPublicaciones.setVisibility(View.VISIBLE);
                tvSinPublicaciones.setText(R.string.perfil_sin_conexion);
            }
        });
    }

    private void mostrarVendedor(@NonNull ReputacionInfo info) {
        tvNombre.setText(info.getNombre());
        VendedorViewBinder.bindReputacion(info, tvAvatar, rbReputacion, tvReputacion, tvNivel);

        tvVentas.setText(getString(R.string.vendedor_operaciones,
                info.getCantidadVentas(), info.getCantidadCompras()));
        if (info.getMiembroDesde() != null) {
            tvMiembroDesde.setVisibility(View.VISIBLE);
            tvMiembroDesde.setText(getString(R.string.vendedor_miembro_desde, info.getMiembroDesde()));
        } else {
            tvMiembroDesde.setVisibility(View.GONE);
        }

        // Ubicacion: solo disponible en Vendedor, no en la interfaz general
        if (info instanceof Vendedor) {
            Vendedor vendedor = (Vendedor) info;
            if (vendedor.getUbicacion() != null && !vendedor.getUbicacion().isEmpty()) {
                tvUbicacion.setVisibility(View.VISIBLE);
                tvUbicacion.setText(getString(R.string.perfil_vendedor_ubicacion, vendedor.getUbicacion()));
            } else {
                tvUbicacion.setVisibility(View.GONE);
            }
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
