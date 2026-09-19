package com.example.tprondagrupo2.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Offer;

import java.util.List;

public class MyOffersAdapter extends RecyclerView.Adapter<MyOffersAdapter.ViewHolder> {

    public interface OnOfferActionListener {
        void onAccept(Offer offer, int position);
        void onReject(Offer offer, int position);
    }

    private final List<Offer> offers;
    private final boolean isReceived;
    private final OnOfferActionListener listener;

    public MyOffersAdapter(List<Offer> offers, boolean isReceived, OnOfferActionListener listener) {
        this.offers = offers;
        this.isReceived = isReceived;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Offer offer = offers.get(position);
        holder.tvTitle.setText(offer.getPublicationTitle() != null ? offer.getPublicationTitle() : "Publicación #" + offer.getPublicationId());
        holder.tvDetails.setText("Ofertado: $" + offer.getOfferedPrice() + " (Original: $" + offer.getPublicationOriginalPrice() + ")");
        
        if (isReceived) {
            holder.tvUser.setText("Comprador: " + (offer.getBuyerName() != null ? offer.getBuyerName() : "Usuario"));
        } else {
            holder.tvUser.setText("Vendedor: " + (offer.getSellerName() != null ? offer.getSellerName() : "Vendedor"));
        }

        holder.tvStatus.setText("Estado: " + offer.getStatus());

        if (offer.getMessage() != null && !offer.getMessage().trim().isEmpty()) {
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setText("Mensaje: " + offer.getMessage());
        } else {
            holder.tvMessage.setVisibility(View.GONE);
        }

        if (offer.getPublicationImage() != null && !offer.getPublicationImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(offer.getPublicationImage())
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        if (isReceived && "PENDING".equals(offer.getStatus())) {
            holder.layoutSellerActions.setVisibility(View.VISIBLE);
            holder.btnAccept.setOnClickListener(v -> listener.onAccept(offer, holder.getAdapterPosition()));
            holder.btnReject.setOnClickListener(v -> listener.onReject(offer, holder.getAdapterPosition()));
        } else {
            holder.layoutSellerActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDetails, tvUser, tvStatus, tvMessage;
        LinearLayout layoutSellerActions;
        Button btnAccept, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivOfferPubImage);
            tvTitle = itemView.findViewById(R.id.tvOfferPubTitle);
            tvDetails = itemView.findViewById(R.id.tvOfferDetails);
            tvUser = itemView.findViewById(R.id.tvOfferUser);
            tvStatus = itemView.findViewById(R.id.tvOfferStatus);
            tvMessage = itemView.findViewById(R.id.tvOfferMessage);
            layoutSellerActions = itemView.findViewById(R.id.layoutSellerActions);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
