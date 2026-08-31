package com.example.tprondagrupo2.ui;

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
import com.example.tprondagrupo2.model.Publication;

import java.util.List;
import java.util.Locale;

public class PublicationAdapter extends RecyclerView.Adapter<PublicationAdapter.ViewHolder> {

    private static final String TAG = "PublicationAdapter";
    private List<Publication> publications;
    private OnItemClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnItemClickListener {
        void onItemClick(Publication publication);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Publication publication, int position);
    }

    public PublicationAdapter(List<Publication> publications, OnItemClickListener listener, OnFavoriteClickListener favoriteListener) {
        this.publications = publications;
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }

    public void updateList(List<Publication> newList) {
        this.publications.clear();
        this.publications.addAll(newList);
        notifyDataSetChanged();
    }

    public void addItems(List<Publication> newItems) {
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
        Publication pub = publications.get(position);
        holder.tvTitle.setText(pub.getTitle());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$ %.2f", pub.getPrice()));
        holder.tvCondition.setText(pub.getStatus());
        holder.tvLocation.setText("Zona: " + pub.getLocation());

        String imageUrl = pub.getFirstImageUrl();
        Log.d(TAG, "Cargando imagen para: " + pub.getTitle() + " URL: " + imageUrl);

        // Carga de imagen con Glide
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(holder.ivProduct);

        holder.btnFavorite.setImageResource(pub.isFavorite()
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);

        holder.btnFavorite.setOnClickListener(v -> {
            if (favoriteListener != null) {
                favoriteListener.onFavoriteClick(pub, position);
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onItemClick(pub));
    }

    @Override
    public int getItemCount() {
        return publications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvTitle, tvPrice, tvCondition, tvLocation;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
