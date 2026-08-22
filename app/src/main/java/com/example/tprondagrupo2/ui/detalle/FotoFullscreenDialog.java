package com.example.tprondagrupo2.ui.detalle;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tprondagrupo2.R;

import java.util.ArrayList;
import java.util.List;

public class FotoFullscreenDialog extends DialogFragment {

    private static final String ARG_FOTOS = "fotos";
    private static final String ARG_POSICION = "posicion";

    public static FotoFullscreenDialog newInstance(List<String> fotos, int posicionInicial) {
        FotoFullscreenDialog dialog = new FotoFullscreenDialog();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_FOTOS, new ArrayList<>(fotos));
        args.putInt(ARG_POSICION, posicionInicial);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public int getTheme() {
        return android.R.style.Theme_Black_NoTitleBar_Fullscreen;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_foto_fullscreen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<String> fotos = getArguments() != null
                ? getArguments().getStringArrayList(ARG_FOTOS)
                : new ArrayList<>();
        int posicionInicial = getArguments() != null
                ? getArguments().getInt(ARG_POSICION, 0)
                : 0;

        ViewPager2 vpFullscreen = view.findViewById(R.id.vpFullscreen);
        TextView tvIndicador = view.findViewById(R.id.tvIndicadorFullscreen);
        ImageView btnCerrar = view.findViewById(R.id.btnCerrar);

        vpFullscreen.setAdapter(new FullscreenAdapter(fotos));

        int total = fotos != null ? fotos.size() : 0;
        actualizarIndicador(tvIndicador, posicionInicial, total);

        vpFullscreen.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                actualizarIndicador(tvIndicador, position, total);
            }
        });

        vpFullscreen.setCurrentItem(posicionInicial, false);

        btnCerrar.setOnClickListener(v -> dismiss());
    }

    private void actualizarIndicador(TextView tv, int position, int total) {
        if (total <= 1) {
            tv.setVisibility(View.GONE);
            return;
        }
        tv.setVisibility(View.VISIBLE);
        tv.setText(getString(R.string.detalle_indicador_fotos, position + 1, total));
    }

    static class FullscreenAdapter extends RecyclerView.Adapter<FullscreenAdapter.VH> {

        private final List<String> fotos;

        FullscreenAdapter(List<String> fotos) {
            this.fotos = fotos != null ? fotos : new ArrayList<>();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_foto_fullscreen, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.tvFotoNombre.setText(fotos.get(position));
        }

        @Override
        public int getItemCount() {
            return fotos.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvFotoNombre;

            VH(@NonNull View itemView) {
                super(itemView);
                tvFotoNombre = itemView.findViewById(R.id.tvFotoNombre);
            }
        }
    }
}
