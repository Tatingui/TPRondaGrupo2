package com.example.tprondagrupo2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publication;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PublicationAdapter extends RecyclerView.Adapter<PublicationAdapter.ViewHolder> {

    private List<Publication> publications;
    private List<Publication> publicationsFull; // Copia para el filtrado
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Publication publication);
    }

    public PublicationAdapter(List<Publication> publications, OnItemClickListener listener) {
        this.publications = publications;
        this.publicationsFull = new ArrayList<>(publications);
        this.listener = listener;
    }

    // Método para que el compañero implemente el filtrado
    public void updateList(List<Publication> newList) {
        this.publications = newList;
        notifyDataSetChanged();
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
        holder.tvCondition.setText(pub.getCondition());
        holder.tvLocation.setText(pub.getLocation());
        
        // TODO: Cargar imagen con Glide o Picasso cuando tengan fotos reales
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(pub));
    }

    @Override
    public int getItemCount() {
        return publications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvTitle, tvPrice, tvCondition, tvLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}
