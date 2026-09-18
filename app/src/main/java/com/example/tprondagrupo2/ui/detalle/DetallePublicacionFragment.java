package com.example.tprondagrupo2.ui.detalle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.db.AppDatabase;
import com.example.tprondagrupo2.db.entity.PublicacionEntity;
import com.example.tprondagrupo2.model.AuthResponse;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.ApiClient;
import com.example.tprondagrupo2.network.FavoritesDataStoreManager;
import com.example.tprondagrupo2.network.NetworkObserver;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePublicacionFragment extends Fragment {

    public static final String ARG_PUBLICACION = "publicacion";

    private static final Locale LOCALE_AR = new Locale("es", "AR");
    private static final Gson GSON = new Gson();

    private ViewPager2 vpGaleria;
    private TextView tvIndicadorFotos;
    private TextView tvTitulo;
    private TextView tvPrecio;
    private TextView tvCategoria;
    private TextView tvEstado;
    private TextView tvFechaPublicacion;
    private TextView tvDescripcion;
    private ImageButton btnFavorite;
    private Publicacion currentPublicacion;
    private NetworkObserver networkObserver;

    // Sección del vendedor
    private View seccionVendedor;
    private TextView tvVendedorAvatar;
    private TextView tvVendedorNombre;
    private TextView tvVendedorNivel;
    private RatingBar rbVendedorReputacion;
    private TextView tvVendedorReputacion;
    private TextView tvVendedorVentas;
    private TextView tvVendedorMiembroDesde;
    private Button btnVerPerfilVendedor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_publicacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vpGaleria = view.findViewById(R.id.vpGaleria);
        tvIndicadorFotos = view.findViewById(R.id.tvIndicadorFotos);
        tvTitulo = view.findViewById(R.id.tvTitulo);
        tvPrecio = view.findViewById(R.id.tvPrecio);
        tvCategoria = view.findViewById(R.id.tvCategoria);
        tvEstado = view.findViewById(R.id.tvEstado);
        tvFechaPublicacion = view.findViewById(R.id.tvFechaPublicacion);
        tvDescripcion = view.findViewById(R.id.tvDescripcion);
        btnFavorite = view.findViewById(R.id.btnFavoriteDetail);

        seccionVendedor = view.findViewById(R.id.seccionVendedor);
        tvVendedorAvatar = view.findViewById(R.id.tvVendedorAvatar);
        tvVendedorNombre = view.findViewById(R.id.tvVendedorNombre);
        tvVendedorNivel = view.findViewById(R.id.tvVendedorNivel);
        rbVendedorReputacion = view.findViewById(R.id.rbVendedorReputacion);
        tvVendedorReputacion = view.findViewById(R.id.tvVendedorReputacion);
        tvVendedorVentas = view.findViewById(R.id.tvVendedorVentas);
        tvVendedorMiembroDesde = view.findViewById(R.id.tvVendedorMiembroDesde);
        btnVerPerfilVendedor = view.findViewById(R.id.btnVerPerfilVendedor);

        currentPublicacion = obtenerPublicacion();
        if (currentPublicacion == null) {
            Toast.makeText(getContext(), "No se encontró la publicación", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        btnFavorite.setOnClickListener(v -> toggleFavorite(currentPublicacion));

        vpGaleria.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                actualizarIndicador(position, currentPublicacion.getCantidadFotos());
            }
        });

        networkObserver = new NetworkObserver(requireContext());
        networkObserver.getIsConnected().observe(getViewLifecycleOwner(), connected -> {
            btnFavorite.setAlpha(connected ? 1.0f : 0.5f);
        });

        // Primero mostramos lo que llegó por el Bundle (o la caché) y después
        // lo actualizamos con los datos completos del backend.
        mostrarPublicacion(currentPublicacion);
        saveToCache(currentPublicacion);
        registrarVista(currentPublicacion);
        cargarDetalle(currentPublicacion.getId());
    }

    private void cargarDetalle(String id) {
        if (id == null) return;

        ApiClient.getPublicationService().getPublication(id).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(@NonNull Call<Publicacion> call, @NonNull Response<Publicacion> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    currentPublicacion = response.body();
                    mostrarPublicacion(currentPublicacion);
                    saveToCache(currentPublicacion);
                } else if (response.code() == 404) {
                    Toast.makeText(getContext(), "La publicación ya no existe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Publicacion> call, @NonNull Throwable t) {
                // Sin conexión: nos quedamos con los datos que ya se muestran
                if (isAdded()) {
                    Toast.makeText(getContext(),
                            "Sin conexión: la información podría no estar actualizada",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveToCache(Publicacion p) {
        PublicacionEntity entity = PublicacionEntity.fromModel(p);
        AppDatabase.getInstance(requireContext()).publicacionDao().insert(entity);
    }

    private Publicacion obtenerPublicacion() {
        if (getArguments() != null) {
            Object extra = getArguments().getSerializable(ARG_PUBLICACION);
            if (extra instanceof Publicacion) {
                return (Publicacion) extra;
            }
        }
        return null;
    }

    private void mostrarPublicacion(@NonNull Publicacion publicacion) {
        configurarGaleria(publicacion);

        tvTitulo.setText(publicacion.getTitle());
        tvPrecio.setText(formatearPrecio(publicacion.getPrice()));
        tvCategoria.setText(publicacion.getCategoryName());
        tvEstado.setText(traducirEstado(publicacion.getStatus()));
        tvFechaPublicacion.setText(
                getString(R.string.detalle_publicado_el, formatearFecha(publicacion.getCreatedAt())));
        tvDescripcion.setText(publicacion.getDescription());

        actualizarIconoFavorito(publicacion.isFavorite());
        mostrarVendedor(publicacion.getVendedor());
    }

    /** El backend manda NEW / LIKE_NEW / USED. */
    private String traducirEstado(String status) {
        if (status == null) return "";
        switch (status) {
            case "NEW":
                return "Nuevo";
            case "LIKE_NEW":
                return "Como nuevo";
            case "USED":
                return "Usado";
            default:
                return status;
        }
    }

    /** El backend manda la fecha como "2026-09-18T10:30:00"; la mostramos como "18/09/2026". */
    private String formatearFecha(String fecha) {
        if (fecha == null || fecha.length() < 10 || fecha.charAt(4) != '-') {
            return fecha != null ? fecha : "";
        }
        String anio = fecha.substring(0, 4);
        String mes = fecha.substring(5, 7);
        String dia = fecha.substring(8, 10);
        return dia + "/" + mes + "/" + anio;
    }

    private void registrarVista(@NonNull Publicacion publicacion) {
        if (publicacion.getId() == null) return;
        String pubId = publicacion.getId();

        ApiClient.getPublicationService().recordView(pubId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // Vista registrada en backend
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Ignorar fallo no bloqueante
            }
        });

        if (getContext() != null) {
            FavoritesDataStoreManager.setHasUpdates(requireContext(), pubId, false);
        }
        publicacion.setHasUpdates(false);
        publicacion.setLastSeenPrice(publicacion.getPrice());
    }

    private void toggleFavorite(@NonNull Publicacion publicacion) {
        if (!networkObserver.isCurrentlyConnected()) {
            Toast.makeText(getContext(), "Se necesita conexión para esta acción", Toast.LENGTH_SHORT).show();
            return;
        }

        final boolean wasFavorite = publicacion.isFavorite();
        final String pubId = publicacion.getId();

        btnFavorite.setEnabled(false);

        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded()) {
                    btnFavorite.setEnabled(true);
                    if (response.isSuccessful()) {
                        publicacion.setFavorite(!wasFavorite);
                        actualizarIconoFavorito(publicacion.isFavorite());
                        if (publicacion.isFavorite()) {
                            FavoritesDataStoreManager.addFavorite(requireContext(), pubId);
                        } else {
                            FavoritesDataStoreManager.removeFavorite(requireContext(), pubId);
                        }
                        String mensaje = publicacion.isFavorite() ? "Agregado a favoritos" : "Eliminado de favoritos";
                        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                    } else {
                        String mensaje = "Error al actualizar favorito";
                        if (response.errorBody() != null) {
                            try {
                                String errorJson = response.errorBody().string();
                                AuthResponse error = GSON.fromJson(errorJson, AuthResponse.class);
                                if (response.code() == 401 || response.code() == 403) {
                                    mensaje = "Sesión vencida o inválida. Iniciá sesión de nuevo.";
                                } else if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
                                    mensaje = error.getMessage();
                                }
                            } catch (Exception ignored) {
                                if (response.code() == 401 || response.code() == 403) {
                                    mensaje = "Sesión vencida o inválida. Iniciá sesión de nuevo.";
                                }
                            }
                        } else if (response.code() == 401 || response.code() == 403) {
                            mensaje = "Sesión vencida o inválida. Iniciá sesión de nuevo.";
                        }
                        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) {
                    btnFavorite.setEnabled(true);
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (wasFavorite) {
            ApiClient.getPublicationService().unmarkAsFavorite(pubId).enqueue(callback);
        } else {
            ApiClient.getPublicationService().markAsFavorite(pubId).enqueue(callback);
        }
    }

    private void actualizarIconoFavorito(boolean isFavorite) {
        btnFavorite.setImageResource(isFavorite 
                ? android.R.drawable.btn_star_big_on 
                : android.R.drawable.btn_star_big_off);
    }

    private void mostrarVendedor(@Nullable Vendedor vendedor) {
        if (vendedor == null) {
            seccionVendedor.setVisibility(View.GONE);
            return;
        }
        seccionVendedor.setVisibility(View.VISIBLE);

        tvVendedorNombre.setText(vendedor.getNombre());
        VendedorViewBinder.bindReputacion(vendedor, tvVendedorAvatar, rbVendedorReputacion,
                tvVendedorReputacion, tvVendedorNivel);

        tvVendedorVentas.setText(getString(R.string.vendedor_ventas, vendedor.getCantidadVentas()));

        // "Miembro desde" llega recién con el detalle del backend
        if (vendedor.getMiembroDesde() != null) {
            tvVendedorMiembroDesde.setVisibility(View.VISIBLE);
            tvVendedorMiembroDesde.setText(
                    getString(R.string.vendedor_miembro_desde, vendedor.getMiembroDesde()));
        } else {
            tvVendedorMiembroDesde.setVisibility(View.GONE);
        }

        btnVerPerfilVendedor.setOnClickListener(v -> abrirPerfil(vendedor));
    }

    private void abrirPerfil(@NonNull Vendedor vendedor) {
        Bundle args = new Bundle();
        args.putSerializable(PerfilVendedorFragment.ARG_VENDEDOR, vendedor);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_detalle_to_perfil_vendedor, args);
    }

    private void configurarGaleria(@NonNull Publicacion publicacion) {
        List<String> fotos = publicacion.getImageUrls();

        GaleriaFotosAdapter adapter = new GaleriaFotosAdapter(fotos, position -> {
            FotoFullscreenDialog dialog = FotoFullscreenDialog.newInstance(fotos, position);
            dialog.show(getParentFragmentManager(), "foto_fullscreen");
        });
        vpGaleria.setAdapter(adapter);

        actualizarIndicador(0, publicacion.getCantidadFotos());
    }

    private void actualizarIndicador(int position, int total) {
        if (total <= 0) {
            tvIndicadorFotos.setVisibility(View.GONE);
            return;
        }
        tvIndicadorFotos.setVisibility(View.VISIBLE);
        tvIndicadorFotos.setText(getString(R.string.detalle_indicador_fotos, position + 1, total));
    }

    private String formatearPrecio(double precio) {
        return NumberFormat.getCurrencyInstance(LOCALE_AR).format(precio);
    }
}
