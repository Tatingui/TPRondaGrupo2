package com.example.tprondagrupo2.ui.profile;

import android.content.Context;
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
import com.example.tprondagrupo2.util.FormatUtils;
import com.example.tprondagrupo2.util.PublicationConstants;

import java.util.List;

public class MyPublicationsAdapter extends RecyclerView.Adapter<MyPublicationsAdapter.ViewHolder> {

    public interface OnPublicationActionListener {
        void onPause(Publicacion pub, int position);
        void onActivate(Publicacion pub, int position);
        void onDelete(Publicacion pub, int position);
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
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(pub.getTitle());
        holder.tvPrice.setText(FormatUtils.formatPrice(pub.getPrice()));

        String state = pub.getState() != null ? pub.getState() : "ACTIVE";
        holder.tvState.setText(context.getString(R.string.my_publication_state_format, PublicationConstants.translateStatus(state)));

        if (pub.getFirstImageUrl() != null && !pub.getFirstImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(pub.getFirstImageUrl())
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.ic_launcher_foreground);
        }

        if ("SOLD".equals(state)) {
            holder.btnPause.setVisibility(View.GONE);
        } else {
            holder.btnPause.setVisibility(View.VISIBLE);
            if ("PAUSED".equals(state)) {
                holder.btnPause.setText(context.getString(R.string.my_publication_reactivate));
                holder.btnPause.setOnClickListener(v -> {
                    int pos = holder.getBindingAdapterPosition();
                    if (listener != null && pos != RecyclerView.NO_POSITION) {
                        listener.onActivate(pub, pos);
                    }
                });
            } else {
                holder.btnPause.setText(context.getString(R.string.my_publication_pause));
                holder.btnPause.setOnClickListener(v -> {
                    int pos = holder.getBindingAdapterPosition();
                    if (listener != null && pos != RecyclerView.NO_POSITION) {
                        listener.onPause(pub, pos);
                    }
                });
            }
        }

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onDelete(pub, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return publications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvPrice, tvState;
        Button btnPause, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivMyPubThumbnail);
            tvTitle = itemView.findViewById(R.id.tvMyPubTitle);
            tvPrice = itemView.findViewById(R.id.tvMyPubPrice);
            tvState = itemView.findViewById(R.id.tvMyPubState);
            btnPause = itemView.findViewById(R.id.btnMyPubPause);
            btnDelete = itemView.findViewById(R.id.btnMyPubDelete);
        }
    }
}
