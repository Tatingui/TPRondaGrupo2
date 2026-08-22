package com.example.tprondagrupo2.ui.detalle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;

import java.util.ArrayList;
import java.util.List;

public class GaleriaFotosAdapter extends RecyclerView.Adapter<GaleriaFotosAdapter.FotoViewHolder> {

    private final List<String> fotos;

    public GaleriaFotosAdapter(List<String> fotos) {
        this.fotos = fotos != null ? fotos : new ArrayList<>();
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_foto_galeria, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        // Todavia no hay libreria de imagenes ni backend real, mostramos el nombre
        // de la foto sobre el placeholder para que la galeria se vea con contenido
        String foto = fotos.get(position);
        holder.tvFotoPlaceholder.setText(foto);
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }

    static class FotoViewHolder extends RecyclerView.ViewHolder {

        final TextView tvFotoPlaceholder;

        FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFotoPlaceholder = itemView.findViewById(R.id.tvFotoPlaceholder);
        }
    }
}
