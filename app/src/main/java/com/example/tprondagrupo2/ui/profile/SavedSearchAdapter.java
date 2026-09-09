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
        holder.tvTitle.setText(item.getDisplayTitle());
        holder.tvSummary.setText(item.getSummaryFilters());

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

    @Override
    public int getItemCount() {
        return savedSearches != null ? savedSearches.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSummary;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSavedSearchTitle);
            tvSummary = itemView.findViewById(R.id.tvSavedSearchSummary);
            btnDelete = itemView.findViewById(R.id.btnDeleteSavedSearch);
        }
    }
}
