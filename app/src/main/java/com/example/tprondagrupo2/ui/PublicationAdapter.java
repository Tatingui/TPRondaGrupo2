package com.example.tprondagrupo2.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import com.bumptech.glide.Glide;
import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;

import java.util.List;
import java.util.Locale;

public class PublicationAdapter extends RecyclerView.Adapter<PublicationAdapter.ViewHolder> {

    private static final String TAG = "PublicationAdapter";
    private List<Publicacion> publications;
    private OnItemClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnItemClickListener {
        void onItemClick(Publicacion publication);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Publicacion publication, int position);
    }

    public PublicationAdapter(List<Publicacion> publications, OnItemClickListener listener, OnFavoriteClickListener favoriteListener) {
        this.publications = publications;
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }

    public void updateList(List<Publicacion> newList) {
        this.publications.clear();
        this.publications.addAll(newList);
        notifyDataSetChanged();
    }

    public void addItems(List<Publicacion> newItems) {
        int startPos = this.publications.size();
        this.publications.addAll(newItems);
        notifyItemRangeInserted(startPos, newItems.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_publication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Publicacion pub = publications.get(position);
        holder.tvTitle.setText(pub.getTitle());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$ %.2f", pub.getPrice()));
        holder.tvCondition.setText(traducirEstado(pub.getStatus()));
        holder.tvLocation.setText("Zona: " + (pub.getLocation() != null ? pub.getLocation() : "Sin ubicación"));

        String imageUrl = pub.getFirstImageUrl();
        Log.d(TAG, "Cargando imagen para: " + pub.getTitle() + " URL: " + imageUrl);

        // Carga de imagen con Glide
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.ivProduct);

        holder.btnFavorite.setImageResource(pub.isFavorite()
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);

        if (holder.tvPriceUpdateBadge != null) {
            holder.tvPriceUpdateBadge.setVisibility(pub.isHasUpdates() ? View.VISIBLE : View.GONE);
        }

        holder.btnFavorite.setOnClickListener(v -> {
            if (favoriteListener != null) {
                favoriteListener.onFavoriteClick(pub, position);
            }
        });


        // Click en la imagen: abrir en pantalla completa
        holder.ivProduct.setOnClickListener(v -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                showFullscreenImage(v, imageUrl);
            }
        });
        holder.itemView.setOnClickListener(v -> listener.onItemClick(pub));
    }

    private String traducirEstado(String status) {
        return com.example.tprondagrupo2.util.PublicationConstants.translateStatus(status);
    }

    private void showFullscreenImage(View anchorView, String imageUrl) {
        Dialog dialog = new Dialog(anchorView.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fullscreen_image);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivFullscreen = dialog.findViewById(R.id.ivFullscreen);
        dialog.findViewById(R.id.btnCloseFullscreen).setOnClickListener(v -> dialog.dismiss());
        ivFullscreen.setOnClickListener(v -> dialog.dismiss());

        Glide.with(anchorView.getContext())
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(ivFullscreen);

        dialog.show();
    }

    public int getItemCount() {
        return publications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvTitle, tvPrice, tvCondition, tvLocation, tvPriceUpdateBadge;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPriceUpdateBadge = itemView.findViewById(R.id.tvPriceUpdateBadge);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
