package com.example.tprondagrupo2.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
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

public class PublicationAdapter extends RecyclerView.Adapter<PublicationAdapter.ViewHolder> {

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
        if (newList != null) {
            this.publications.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public void addItems(List<Publicacion> newItems) {
        if (newItems == null || newItems.isEmpty()) return;
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
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(pub.getTitle());
        holder.tvPrice.setText(FormatUtils.formatPrice(pub.getPrice()));
        holder.tvCondition.setText(PublicationConstants.translateStatus(pub.getStatus()));

        String location = pub.getLocation() != null ? pub.getLocation() : context.getString(R.string.publication_no_location);
        holder.tvLocation.setText(context.getString(R.string.detalle_zona, location));

        String imageUrl = pub.getFirstImageUrl();

        // Carga de imagen con Glide
        Glide.with(context)
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
            int pos = holder.getBindingAdapterPosition();
            if (favoriteListener != null && pos != RecyclerView.NO_POSITION) {
                favoriteListener.onFavoriteClick(pub, pos);
            }
        });

        // Click en la imagen: abrir en pantalla completa
        holder.ivProduct.setOnClickListener(v -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                showFullscreenImage(v, imageUrl);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(pub);
            }
        });
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
