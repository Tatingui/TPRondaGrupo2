package com.example.tprondagrupo2.ui.detalle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Vendedor;

/**
 * Perfil público del vendedor: nombre, foto (avatar con inicial), reputación
 * y datos generales. Recibe el vendedor por Bundle desde el detalle.
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

        Vendedor vendedor = obtenerVendedor();
        if (vendedor == null) {
            Toast.makeText(getContext(), "No se encontró el vendedor", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        mostrarVendedor(vendedor);
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
}
