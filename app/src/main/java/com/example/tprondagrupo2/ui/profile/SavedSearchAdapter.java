package com.example.tprondagrupo2.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.SavedSearch;

import java.util.ArrayList;
import java.util.List;

public class SavedSearchAdapter extends RecyclerView.Adapter<SavedSearchAdapter.ViewHolder> {

    public interface OnSavedSearchClickListener {
        void onSearchClick(SavedSearch savedSearch);
        void onDeleteClick(SavedSearch savedSearch, int position);
    }

    private final List<SavedSearch> savedSearches;
    private final OnSavedSearchClickListener listener;

    public SavedSearchAdapter(List<SavedSearch> savedSearches, OnSavedSearchClickListener listener) {
        this.savedSearches = savedSearches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedSearch item = savedSearches.get(position);
        holder.tvTitle.setText(formatDisplayTitle(item));
        holder.tvSummary.setText(formatSummaryFilters(item));

        if (holder.tvBadge != null) {
            holder.tvBadge.setVisibility(item.isHasUpdates() ? View.VISIBLE : View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSearchClick(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(item, pos);
            }
        });
    }

    public static String formatDisplayTitle(SavedSearch item) {
        if (item == null) return "";
        String query = item.getQuery();
        String categoryName = item.getCategoryName();
        if (query != null && !query.trim().isEmpty()) {
            return query.trim();
        }
        if (categoryName != null && !categoryName.equalsIgnoreCase("Categoría") && !categoryName.equalsIgnoreCase("Todas")) {
            return categoryName;
        }
        return "Búsqueda guardada";
    }

    public static String formatSummaryFilters(SavedSearch item) {
        if (item == null) return "";
        List<String> filters = new ArrayList<>();
        String query = item.getQuery();
        String categoryName = item.getCategoryName();
        String conditionName = item.getConditionName();
        String locationName = item.getLocationName();
        Double minPrice = item.getMinPrice();
        Double maxPrice = item.getMaxPrice();
        String sortName = item.getSortName();

        if (query != null && !query.trim().isEmpty() && categoryName != null && !categoryName.equalsIgnoreCase("Categoría") && !categoryName.equalsIgnoreCase("Todas")) {
            filters.add("Categoría: " + categoryName);
        }
        if (conditionName != null && !conditionName.equalsIgnoreCase("Estado") && !conditionName.equalsIgnoreCase("Cualquiera")) {
            filters.add("Estado: " + conditionName);
        }
        if (locationName != null && !locationName.equalsIgnoreCase("Zona") && !locationName.equalsIgnoreCase("Todas")) {
            filters.add("Zona: " + locationName);
        }
        if (minPrice != null || maxPrice != null) {
            if (minPrice != null && maxPrice != null) {
                filters.add("$" + minPrice.longValue() + " - $" + maxPrice.longValue());
            } else if (minPrice != null) {
                filters.add("Desde $" + minPrice.longValue());
            } else {
                filters.add("Hasta $" + maxPrice.longValue());
            }
        }
        if (sortName != null && !sortName.equalsIgnoreCase("Ordenar por") && !sortName.equalsIgnoreCase("Recientes")) {
            filters.add("Orden: " + sortName);
        }

        if (filters.isEmpty()) {
            return "Sin filtros adicionales";
        }

        return String.join(" • ", filters);
    }

    @Override
    public int getItemCount() {
        return savedSearches != null ? savedSearches.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSummary;
        TextView tvBadge;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSavedSearchTitle);
            tvSummary = itemView.findViewById(R.id.tvSavedSearchSummary);
            tvBadge = itemView.findViewById(R.id.tvSavedSearchBadge);
            btnDelete = itemView.findViewById(R.id.btnDeleteSavedSearch);
        }
    }
}
