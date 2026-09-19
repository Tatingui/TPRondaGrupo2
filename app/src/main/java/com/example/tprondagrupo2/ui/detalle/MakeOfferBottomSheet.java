package com.example.tprondagrupo2.ui.detalle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Offer;
import com.example.tprondagrupo2.model.OfferCreateRequest;
import com.example.tprondagrupo2.network.OfferApiService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MakeOfferBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PUBLICATION_ID = "publication_id";

    private Long publicationId;

    @Inject
    OfferApiService offerApiService;

    private TextInputEditText etOfferedPrice, etOfferMessage;
    private Button btnSubmitOffer;

    public static MakeOfferBottomSheet newInstance(Long publicationId) {
        MakeOfferBottomSheet fragment = new MakeOfferBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_PUBLICATION_ID, publicationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            publicationId = getArguments().getLong(ARG_PUBLICATION_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_make_offer_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etOfferedPrice = view.findViewById(R.id.etOfferedPrice);
        etOfferMessage = view.findViewById(R.id.etOfferMessage);
        btnSubmitOffer = view.findViewById(R.id.btnSubmitOffer);

        btnSubmitOffer.setOnClickListener(v -> submitOffer());
    }

    private void submitOffer() {
        String priceStr = etOfferedPrice.getText() != null ? etOfferedPrice.getText().toString().trim() : "";
        String message = etOfferMessage.getText() != null ? etOfferMessage.getText().toString().trim() : "";

        if (priceStr.isEmpty()) {
            etOfferedPrice.setError("Ingrese un precio");
            return;
        }

        double price = Double.parseDouble(priceStr);
        OfferCreateRequest request = new OfferCreateRequest(publicationId, price, message);

        offerApiService.createOffer(request).enqueue(new Callback<Offer>() {
            @Override
            public void onResponse(@NonNull Call<Offer> call, @NonNull Response<Offer> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Oferta enviada con éxito!", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Error al enviar oferta: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Offer> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
