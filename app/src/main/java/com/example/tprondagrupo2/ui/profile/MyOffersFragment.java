package com.example.tprondagrupo2.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.OfferRespondRequest;
import com.example.tprondagrupo2.model.OperacionHistorial;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.network.HistorialApiService;
import com.example.tprondagrupo2.network.OfferApiService;
import com.example.tprondagrupo2.network.PublicationApiService;
import com.example.tprondagrupo2.ui.detalle.DetallePublicacionFragment;
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

    @Inject
    HistorialApiService historialApiService;

    @Inject
    PublicationApiService publicationApiService;

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
                acceptOffer(offer, position);
            }

            @Override
            public void onReject(Offer offer, int position) {
                rejectOffer(offer.getId(), position);
            }

            @Override
            public void onCounterOffer(Offer offer, int position) {
                showCounterOfferDialog(offer, position);
            }

            @Override
            public void onBuyerAcceptCounter(Offer offer, int position) {
                acceptOffer(offer, position);
            }

            @Override
            public void onBuyerRejectCounter(Offer offer, int position) {
                rejectOffer(offer.getId(), position);
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

    /**
     * Acepta la oferta usando el endpoint correcto (PUT /transactions/offers/{id}/accept).
     * Esto crea la transacción en el backend, marca la publicación como SOLD,
     * y luego navega al detalle de la publicación para que el vendedor vea
     * la dirección y el botón "Cómo llegar".
     */
    private void acceptOffer(Offer offer, int position) {
        historialApiService.acceptOffer(offer.getId()).enqueue(new Callback<OperacionHistorial>() {
            @Override
            public void onResponse(@NonNull Call<OperacionHistorial> call, @NonNull Response<OperacionHistorial> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Oferta aceptada", Toast.LENGTH_SHORT).show();
                    navigateToPublicationDetail(offer.getPublicationId());
                } else {
                    Toast.makeText(getContext(), "Error al aceptar oferta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OperacionHistorial> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Rechaza la oferta usando el endpoint original (PATCH /offers/{id}/respond).
     * Para rechazar solo se necesita cambiar el estado, no crear transacción.
     */
    private void rejectOffer(Long offerId, int position) {
        OfferRespondRequest request = new OfferRespondRequest("REJECTED", null);
        offerApiService.respondOffer(offerId, request).enqueue(new Callback<Offer>() {
            @Override
            public void onResponse(@NonNull Call<Offer> call, @NonNull Response<Offer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentOffers.set(position, response.body());
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Oferta rechazada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al rechazar oferta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Offer> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Muestra un diálogo para que el vendedor haga una contra-oferta.
     */
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
                sendCounterOffer(offer.getId(), nuevoMonto, position);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void sendCounterOffer(Long offerId, double newPrice, int position) {
        OfferRespondRequest request = new OfferRespondRequest("COUNTER_OFFER", newPrice);
        offerApiService.respondOffer(offerId, request).enqueue(new Callback<Offer>() {
            @Override
            public void onResponse(@NonNull Call<Offer> call, @NonNull Response<Offer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentOffers.set(position, response.body());
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Contra-oferta enviada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al enviar contra-oferta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Offer> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Obtiene la publicación por ID y navega al DetallePublicacionFragment
     * para que el vendedor vea la dirección y pueda usar "Cómo llegar".
     */
    private void navigateToPublicationDetail(Long publicationId) {
        if (publicationId == null) return;
        publicationApiService.getPublication(String.valueOf(publicationId)).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(@NonNull Call<Publicacion> call, @NonNull Response<Publicacion> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    Bundle args = new Bundle();
                    args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, response.body());
                    NavHostFragment.findNavController(MyOffersFragment.this)
                            .navigate(R.id.action_myOffers_to_detalle, args);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Publicacion> call, @NonNull Throwable t) {
                // Si falla la navegación al detalle, al menos la oferta ya fue aceptada
            }
        });
    }
}
