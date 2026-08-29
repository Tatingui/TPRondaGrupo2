package com.example.tprondagrupo2.ui.detalle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tprondagrupo2.R;

import java.util.ArrayList;
import java.util.List;

public class GaleriaFotosAdapter extends RecyclerView.Adapter<GaleriaFotosAdapter.FotoViewHolder> {

    public interface OnFotoClickListener {
        void onFotoClick(int position);
    }

    private final List<String> fotos;
    private final OnFotoClickListener listener;

    public GaleriaFotosAdapter(List<String> fotos, OnFotoClickListener listener) {
        this.fotos = fotos != null ? fotos : new ArrayList<>();
        this.listener = listener;
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
        String url = fotos.get(position);
        
        Glide.with(holder.itemView.getContext())
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.ivFoto);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFotoClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }

    static class FotoViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivFoto;

        FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
        }
    }
}
