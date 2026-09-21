package com.example.tprondagrupo2.ui.profile;

import android.content.Context;
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
import com.example.tprondagrupo2.util.FormatUtils;

import java.util.List;

public class MyOffersAdapter extends RecyclerView.Adapter<MyOffersAdapter.ViewHolder> {

    public interface OnOfferActionListener {
        void onOpenPublication(Offer offer);
        void onAccept(Offer offer, int position);
        void onReject(Offer offer, int position);
        void onCounterOffer(Offer offer, int position);
        void onBuyerAcceptCounter(Offer offer, int position);
        void onBuyerRejectCounter(Offer offer, int position);
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
        Context context = holder.itemView.getContext();

        holder.btnOpenPublication.setOnClickListener(v -> listener.onOpenPublication(offer));
        holder.tvTitle.setText(offer.getPublicationTitle() != null
                ? offer.getPublicationTitle()
                : context.getString(R.string.offer_publication_number, offer.getPublicationId()));

        String offeredStr = FormatUtils.formatPrice(offer.getOfferedPrice());
        String originalStr = FormatUtils.formatPrice(offer.getPublicationOriginalPrice());
        holder.tvDetails.setText(context.getString(R.string.offer_details_format, offeredStr, originalStr));

        if (isReceived) {
            String buyerName = offer.getBuyerName() != null ? offer.getBuyerName() : context.getString(R.string.offer_default_user);
            holder.tvUser.setText(context.getString(R.string.offer_buyer_format, buyerName));
        } else {
            String sellerName = offer.getSellerName() != null ? offer.getSellerName() : context.getString(R.string.offer_default_seller);
            holder.tvUser.setText(context.getString(R.string.offer_seller_format, sellerName));
        }

        String statusText;
        switch (offer.getStatus() != null ? offer.getStatus() : "") {
            case "PENDING":
                statusText = context.getString(R.string.offer_status_pending);
                break;
            case "ACCEPTED":
                statusText = context.getString(R.string.offer_status_accepted);
                break;
            case "REJECTED":
                statusText = context.getString(R.string.offer_status_rejected);
                break;
            case "COUNTER_OFFER":
                statusText = context.getString(R.string.offer_status_counter);
                break;
            case "EXPIRED":
                statusText = context.getString(R.string.offer_status_expired);
                break;
            default:
                statusText = offer.getStatus();
                break;
        }
        holder.tvStatus.setText(context.getString(R.string.offer_status_format, statusText));

        if (offer.getMessage() != null && !offer.getMessage().trim().isEmpty()) {
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setText(context.getString(R.string.offer_message_format, offer.getMessage()));
        } else {
            holder.tvMessage.setVisibility(View.GONE);
        }

        if (offer.getPublicationImage() != null && !offer.getPublicationImage().isEmpty()) {
            Glide.with(context)
                    .load(offer.getPublicationImage())
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Seller actions: show for received PENDING offers
        if (isReceived && "PENDING".equals(offer.getStatus())) {
            holder.layoutSellerActions.setVisibility(View.VISIBLE);
            holder.btnAccept.setOnClickListener(v -> listener.onAccept(offer, holder.getAdapterPosition()));
            holder.btnReject.setOnClickListener(v -> listener.onReject(offer, holder.getAdapterPosition()));
            holder.btnCounterOffer.setOnClickListener(v -> listener.onCounterOffer(offer, holder.getAdapterPosition()));
        } else {
            holder.layoutSellerActions.setVisibility(View.GONE);
        }

        // Buyer info: show counter-offer details for sent offers
        if (!isReceived && "COUNTER_OFFER".equals(offer.getStatus())) {
            holder.layoutBuyerActions.setVisibility(View.VISIBLE);
            holder.btnBuyerAccept.setVisibility(View.GONE);
            holder.btnBuyerReject.setVisibility(View.GONE);
        } else {
            holder.layoutBuyerActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDetails, tvUser, tvStatus, tvMessage;
        LinearLayout layoutSellerActions, layoutBuyerActions;
        Button btnAccept, btnReject, btnCounterOffer;
        Button btnOpenPublication;
        Button btnBuyerAccept, btnBuyerReject;

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
            btnCounterOffer = itemView.findViewById(R.id.btnCounterOffer);
            btnOpenPublication = itemView.findViewById(R.id.btnOpenOfferPublication);
            layoutBuyerActions = itemView.findViewById(R.id.layoutBuyerActions);
            btnBuyerAccept = itemView.findViewById(R.id.btnBuyerAccept);
            btnBuyerReject = itemView.findViewById(R.id.btnBuyerReject);
        }
    }
}
