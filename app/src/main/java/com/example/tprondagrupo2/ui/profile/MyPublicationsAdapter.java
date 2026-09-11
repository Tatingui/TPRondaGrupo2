package com.example.tprondagrupo2.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;

import java.util.List;

public class MyPublicationsAdapter extends RecyclerView.Adapter<MyPublicationsAdapter.ViewHolder> {

    public interface OnPublicationActionListener {
        void onPause(Publicacion pub, int position);
        void onActivate(Publicacion pub, int position);
        void onSell(Publicacion pub, int position);
    }

    private final List<Publicacion> publications;
    private final OnPublicationActionListener listener;

    public MyPublicationsAdapter(List<Publicacion> publications, OnPublicationActionListener listener) {
        this.publications = publications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_publication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Publicacion pub = publications.get(position);
        holder.tvTitle.setText(pub.getTitle());
        holder.tvPrice.setText("$" + pub.getPrice());
        
        String state = pub.getState() != null ? pub.getState() : "ACTIVE";
        holder.tvState.setText("Estado: " + state);

        if (pub.getFirstImageUrl() != null && !pub.getFirstImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(pub.getFirstImageUrl())
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.ic_launcher_foreground);
        }

        if ("PAUSED".equals(state)) {
            holder.btnPause.setText("Reactivar");
            holder.btnPause.setOnClickListener(v -> listener.onActivate(pub, holder.getAdapterPosition()));
        } else {
            holder.btnPause.setText("Pausar");
            holder.btnPause.setOnClickListener(v -> listener.onPause(pub, holder.getAdapterPosition()));
        }

        holder.btnSell.setOnClickListener(v -> listener.onSell(pub, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return publications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvPrice, tvState;
        Button btnPause, btnSell;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivMyPubThumbnail);
            tvTitle = itemView.findViewById(R.id.tvMyPubTitle);
            tvPrice = itemView.findViewById(R.id.tvMyPubPrice);
            tvState = itemView.findViewById(R.id.tvMyPubState);
            btnPause = itemView.findViewById(R.id.btnMyPubPause);
            btnSell = itemView.findViewById(R.id.btnMyPubSell);
        }
    }
}
