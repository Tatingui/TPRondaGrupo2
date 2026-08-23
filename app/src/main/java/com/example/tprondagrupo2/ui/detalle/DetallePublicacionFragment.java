package com.example.tprondagrupo2.ui.detalle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Vendedor;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DetallePublicacionFragment extends Fragment {

    public static final String ARG_PUBLICACION = "publicacion";

    private static final Locale LOCALE_AR = new Locale("es", "AR");

    private ViewPager2 vpGaleria;
    private TextView tvIndicadorFotos;
    private TextView tvTitulo;
    private TextView tvPrecio;
    private TextView tvCategoria;
    private TextView tvEstado;
    private TextView tvFechaPublicacion;
    private TextView tvDescripcion;

    // Sección del vendedor
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

        tvVendedorAvatar = view.findViewById(R.id.tvVendedorAvatar);
        tvVendedorNombre = view.findViewById(R.id.tvVendedorNombre);
        tvVendedorNivel = view.findViewById(R.id.tvVendedorNivel);
        rbVendedorReputacion = view.findViewById(R.id.rbVendedorReputacion);
        tvVendedorReputacion = view.findViewById(R.id.tvVendedorReputacion);
        tvVendedorVentas = view.findViewById(R.id.tvVendedorVentas);
        tvVendedorMiembroDesde = view.findViewById(R.id.tvVendedorMiembroDesde);
        btnVerPerfilVendedor = view.findViewById(R.id.btnVerPerfilVendedor);

        mostrarPublicacion(obtenerPublicacion());
    }

    private Publicacion obtenerPublicacion() {
        // Todavia no hay backend de publicaciones: si no llega nada por argumentos
        // usamos datos de demo para que la pantalla se pueda ver con contenido
        if (getArguments() != null) {
            Object extra = getArguments().getSerializable(ARG_PUBLICACION);
            if (extra instanceof Publicacion) {
                return (Publicacion) extra;
            }
        }
        return crearPublicacionDemo();
    }

    private void mostrarPublicacion(@NonNull Publicacion publicacion) {
        configurarGaleria(publicacion);

        tvTitulo.setText(publicacion.getTitulo());
        tvPrecio.setText(formatearPrecio(publicacion.getPrecio()));
        tvCategoria.setText(publicacion.getCategoria());
        tvEstado.setText(publicacion.getEstado());
        tvFechaPublicacion.setText(
                getString(R.string.detalle_publicado_el, publicacion.getFechaPublicacion()));
        tvDescripcion.setText(publicacion.getDescripcion());

        mostrarVendedor(obtenerVendedor(publicacion));
    }

    private void mostrarVendedor(@NonNull Vendedor vendedor) {
        tvVendedorNombre.setText(vendedor.getNombre());
        VendedorViewBinder.bindReputacion(vendedor, tvVendedorAvatar, rbVendedorReputacion,
                tvVendedorReputacion, tvVendedorNivel);

        tvVendedorVentas.setText(getString(R.string.vendedor_ventas, vendedor.getCantidadVentas()));
        tvVendedorMiembroDesde.setText(
                getString(R.string.vendedor_miembro_desde, vendedor.getMiembroDesde()));

        btnVerPerfilVendedor.setOnClickListener(v -> abrirPerfil(vendedor));
    }

    private void abrirPerfil(@NonNull Vendedor vendedor) {
        Bundle args = new Bundle();
        args.putSerializable(PerfilVendedorFragment.ARG_VENDEDOR, vendedor);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_detalle_to_perfil_vendedor, args);
    }

    /**
     * Si la publicación todavía no trae vendedor (mocks previos sin backend),
     * usamos datos de demo para que la sección se vea con contenido.
     */
    private Vendedor obtenerVendedor(@NonNull Publicacion publicacion) {
        if (publicacion.getVendedor() != null) {
            return publicacion.getVendedor();
        }
        return crearVendedorDemo();
    }

    private Vendedor crearVendedorDemo() {
        return new Vendedor("1", "Juan Pérez", 4.5, 342, 128, "Marzo 2023", "Palermo");
    }

    private void configurarGaleria(@NonNull Publicacion publicacion) {
        List<String> fotos = publicacion.getFotos();

        GaleriaFotosAdapter adapter = new GaleriaFotosAdapter(fotos, position -> {
            FotoFullscreenDialog dialog = FotoFullscreenDialog.newInstance(fotos, position);
            dialog.show(getParentFragmentManager(), "foto_fullscreen");
        });
        vpGaleria.setAdapter(adapter);

        int total = publicacion.getCantidadFotos();
        actualizarIndicador(0, total);

        vpGaleria.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                actualizarIndicador(position, total);
            }
        });
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

    private Publicacion crearPublicacionDemo() {
        return new Publicacion(
                "1",
                "Bicicleta rodado 29 en excelente estado",
                Arrays.asList("Foto 1", "Foto 2", "Foto 3"),
                "Bicicleta de montaña rodado 29, cuadro de aluminio, 21 velocidades. "
                        + "Muy poco uso, siempre guardada en lugar techado. Frenos a disco "
                        + "delanteros y traseros recién calibrados.",
                "Deportes",
                "Usado",
                185000,
                "20/08/2026",
                crearVendedorDemo());
    }
}
