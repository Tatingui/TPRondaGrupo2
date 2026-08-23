package com.example.tprondagrupo2.ui.detalle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Vendedor;

/**
 * Perfil público del vendedor: nombre, foto (avatar con inicial), reputación
 * y datos generales. No lista publicaciones porque todavía no hay backend que
 * vincule publicaciones a usuarios.
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

        mostrarVendedor(obtenerVendedor());
    }

    private Vendedor obtenerVendedor() {
        if (getArguments() != null) {
            Object extra = getArguments().getSerializable(ARG_VENDEDOR);
            if (extra instanceof Vendedor) {
                return (Vendedor) extra;
            }
        }
        return crearVendedorDemo();
    }

    private void mostrarVendedor(@NonNull Vendedor vendedor) {
        tvNombre.setText(vendedor.getNombre());
        VendedorViewBinder.bindReputacion(vendedor, tvAvatar, rbReputacion, tvReputacion, tvNivel);

        tvVentas.setText(getString(R.string.vendedor_ventas, vendedor.getCantidadVentas()));
        tvMiembroDesde.setText(getString(R.string.vendedor_miembro_desde, vendedor.getMiembroDesde()));

        if (vendedor.getUbicacion() != null && !vendedor.getUbicacion().isEmpty()) {
            tvUbicacion.setVisibility(View.VISIBLE);
            tvUbicacion.setText(getString(R.string.perfil_vendedor_ubicacion, vendedor.getUbicacion()));
        } else {
            tvUbicacion.setVisibility(View.GONE);
        }
    }

    private Vendedor crearVendedorDemo() {
        return new Vendedor("1", "Juan Pérez", 4.5, 342, 128, "Marzo 2023", "Palermo");
    }
}
