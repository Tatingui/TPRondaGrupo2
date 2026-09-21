package com.example.tprondagrupo2.ui.publish;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tprondagrupo2.R;

import java.util.List;

public class PhotoThumbnailAdapter extends RecyclerView.Adapter<PhotoThumbnailAdapter.ViewHolder> {

    public interface OnRemoveClickListener {
        void onRemove(int position);
    }

    private final List<Uri> photos;
    private final OnRemoveClickListener removeListener;

    public PhotoThumbnailAdapter(List<Uri> photos, OnRemoveClickListener removeListener) {
        this.photos = photos;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_thumbnail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = photos.get(position);
        Glide.with(holder.ivThumbnail.getContext())
                .load(uri)
                .centerCrop()
                .into(holder.ivThumbnail);

        holder.ivRemove.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (removeListener != null && pos != RecyclerView.NO_POSITION) {
                removeListener.onRemove(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        ImageView ivRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivRemove = itemView.findViewById(R.id.ivRemove);
        }
    }
}
