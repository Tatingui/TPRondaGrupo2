package com.example.tprondagrupo2.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.ui.detalle.DetallePublicacionFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyOffersFragment extends Fragment {

    private MyOffersViewModel viewModel;

    private TabLayout tabLayoutOffers;
    private RecyclerView rvOffers;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View btnRetry;
    private AlertDialog activeDialog;

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

        viewModel = new ViewModelProvider(this).get(MyOffersViewModel.class);

        tabLayoutOffers = view.findViewById(R.id.tabLayoutOffers);
        rvOffers = view.findViewById(R.id.rvOffers);
        progressBar = view.findViewById(R.id.progressBarOffers);
        tvEmpty = view.findViewById(R.id.tvEmptyOffers);
        btnRetry = view.findViewById(R.id.btnRetryOffers);

        isReceivedTab = tabLayoutOffers.getSelectedTabPosition() == 0;

        btnRetry.setOnClickListener(v -> viewModel.fetchOffers(isReceivedTab));

        setupRecyclerView();
        setupTabs();
        setupViewModelObservers();

        viewModel.fetchOffers(isReceivedTab);
    }

    private void setupViewModelObservers() {
        viewModel.getOffers().observe(getViewLifecycleOwner(), offers -> {
            currentOffers.clear();
            if (offers != null) {
                currentOffers.addAll(offers);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getEmptyMessage().observe(getViewLifecycleOwner(), msg -> {
            if (tvEmpty != null) {
                if (msg != null && !msg.isEmpty()) {
                    tvEmpty.setText(msg);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && isAdded()) {
                if (tvEmpty != null) {
                    tvEmpty.setText(error);
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                if (btnRetry != null) {
                    btnRetry.setVisibility(View.VISIBLE);
                }
            } else if (btnRetry != null) {
                btnRetry.setVisibility(View.GONE);
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && isAdded()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigateToDetail().observe(getViewLifecycleOwner(), publicacion -> {
            if (publicacion != null && isAdded()) {
                Bundle args = new Bundle();
                args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, publicacion);
                viewModel.onNavigatedToDetail();
                NavHostFragment.findNavController(MyOffersFragment.this)
                        .navigate(R.id.action_myOffers_to_detalle, args);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new MyOffersAdapter(currentOffers, isReceivedTab, new MyOffersAdapter.OnOfferActionListener() {
            @Override
            public void onOpenPublication(Offer offer) {
                if (offer != null) {
                    viewModel.fetchPublicationForDetail(offer.getPublicationId());
                }
            }

            @Override
            public void onAccept(Offer offer, int position) {
                viewModel.acceptOffer(offer, isReceivedTab);
            }

            @Override
            public void onReject(Offer offer, int position) {
                if (offer != null) {
                    viewModel.rejectOffer(offer.getId(), isReceivedTab);
                }
            }

            @Override
            public void onCounterOffer(Offer offer, int position) {
                showCounterOfferDialog(offer, position);
            }

            @Override
            public void onBuyerAcceptCounter(Offer offer, int position) {
                viewModel.acceptOffer(offer, isReceivedTab);
            }

            @Override
            public void onBuyerRejectCounter(Offer offer, int position) {
                if (offer != null) {
                    viewModel.rejectOffer(offer.getId(), isReceivedTab);
                }
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
                viewModel.fetchOffers(isReceivedTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showCounterOfferDialog(Offer offer, int position) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ofertar, null);
        TextView tvPrecio = dialogView.findViewById(R.id.tvPrecioPublicado);
        EditText etMonto = dialogView.findViewById(R.id.etMontoOferta);
        EditText etMensaje = dialogView.findViewById(R.id.etMensajeOferta);
        TextView tvPorcentaje = dialogView.findViewById(R.id.tvPorcentajeOferta);

        double ofertaComprador = offer.getOfferedPrice();
        double precioOriginal = offer.getPublicationOriginalPrice() != null ? offer.getPublicationOriginalPrice() : Double.MAX_VALUE;

        tvPrecio.setText("Oferta del comprador: $" + String.format("%.2f", ofertaComprador)
                + "\nPrecio original: $" + String.format("%.2f", precioOriginal));
        etMensaje.setVisibility(View.GONE);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Contra-oferta")
                .setView(dialogView)
                .setPositiveButton("Enviar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPositive.setEnabled(false);

            etMonto.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(android.text.Editable s) {
                    try {
                        double monto = Double.parseDouble(s.toString().trim());
                        tvPorcentaje.setVisibility(View.VISIBLE);
                        if (monto < ofertaComprador) {
                            tvPorcentaje.setText("No podés contraofertar a un precio menor al del comprador");
                            tvPorcentaje.setTextColor(0xFFD32F2F);
                            btnPositive.setEnabled(false);
                        } else if (monto > precioOriginal) {
                            tvPorcentaje.setText("No podés contraofertar a un precio mayor al publicado");
                            tvPorcentaje.setTextColor(0xFFD32F2F);
                            btnPositive.setEnabled(false);
                        } else {
                            int porcentaje = (int) Math.round((monto / precioOriginal) * 100);
                            tvPorcentaje.setText(porcentaje + "% del precio publicado");
                            tvPorcentaje.setTextColor(0xFF888888);
                            btnPositive.setEnabled(true);
                        }
                    } catch (NumberFormatException e) {
                        tvPorcentaje.setVisibility(View.GONE);
                        btnPositive.setEnabled(false);
                    }
                }
            });

            btnPositive.setOnClickListener(v -> {
                double nuevoMonto;
                try {
                    nuevoMonto = Double.parseDouble(etMonto.getText().toString().trim());
                } catch (NumberFormatException e) {
                    nuevoMonto = 0;
                }
                if (nuevoMonto < ofertaComprador || nuevoMonto > precioOriginal) {
                    return;
                }
                viewModel.sendCounterOffer(offer.getId(), nuevoMonto, isReceivedTab);
                dialog.dismiss();
            });
        });

        activeDialog = dialog;
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        if (activeDialog != null) activeDialog.dismiss();
        if (tabLayoutOffers != null) tabLayoutOffers.clearOnTabSelectedListeners();
        if (rvOffers != null) rvOffers.setAdapter(null);
        activeDialog = null;
        tabLayoutOffers = null;
        rvOffers = null;
        progressBar = null;
        tvEmpty = null;
        btnRetry = null;
        adapter = null;
        currentOffers.clear();
        super.onDestroyView();
    }
}
