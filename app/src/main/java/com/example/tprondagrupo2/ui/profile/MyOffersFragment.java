package com.example.tprondagrupo2.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.OfferRespondRequest;
import com.example.tprondagrupo2.network.OfferApiService;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MyOffersFragment extends Fragment {

    @Inject
    OfferApiService offerApiService;

    private TabLayout tabLayoutOffers;
    private RecyclerView rvOffers;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private MyOffersAdapter adapter;
    private final List<Offer> currentOffers = new ArrayList<>();
    private boolean isReceivedTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_offers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayoutOffers = view.findViewById(R.id.tabLayoutOffers);
        rvOffers = view.findViewById(R.id.rvOffers);
        progressBar = view.findViewById(R.id.progressBarOffers);
        tvEmpty = view.findViewById(R.id.tvEmptyOffers);

        setupRecyclerView();
        setupTabs();
        fetchOffers();
    }

    private void setupRecyclerView() {
        adapter = new MyOffersAdapter(currentOffers, isReceivedTab, new MyOffersAdapter.OnOfferActionListener() {
            @Override
            public void onAccept(Offer offer, int position) {
                respondOffer(offer.getId(), "ACCEPTED", position);
            }

            @Override
            public void onReject(Offer offer, int position) {
                respondOffer(offer.getId(), "REJECTED", position);
            }
        });
        rvOffers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOffers.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayoutOffers.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isReceivedTab = tab.getPosition() == 0;
                setupRecyclerView();
                fetchOffers();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchOffers() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        Call<List<Offer>> call = isReceivedTab ? offerApiService.getReceivedOffers() : offerApiService.getSentOffers();
        call.enqueue(new Callback<List<Offer>>() {
            @Override
            public void onResponse(@NonNull Call<List<Offer>> call, @NonNull Response<List<Offer>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentOffers.clear();
                    currentOffers.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (currentOffers.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar ofertas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Offer>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void respondOffer(Long offerId, String status, int position) {
        OfferRespondRequest request = new OfferRespondRequest(status, null);
        offerApiService.respondOffer(offerId, request).enqueue(new Callback<Offer>() {
            @Override
            public void onResponse(@NonNull Call<Offer> call, @NonNull Response<Offer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentOffers.set(position, response.body());
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Oferta " + status.toLowerCase(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al responder oferta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Offer> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
