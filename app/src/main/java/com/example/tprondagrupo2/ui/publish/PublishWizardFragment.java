package com.example.tprondagrupo2.ui.publish;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.data.DraftManager;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.PublicationCreateRequest;
import com.example.tprondagrupo2.network.ApiClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishWizardFragment extends Fragment {

    private int currentStep = 1;
    private DraftManager draftManager;

    private TextView tvStepTitle;
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private Button btnBack, btnNext, btnSelectPhoto;
    private TextView tvImageStatus, tvReviewSummary;

    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etCategoryId, etStatus, etImageUrl;

    private List<String> imageUrls = new ArrayList<>();

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUrls.add(uri.toString());
                    tvImageStatus.setText(imageUrls.size() + " fotos seleccionadas");
                    saveCurrentDraft();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_publish_wizard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        draftManager = new DraftManager(requireContext());

        tvStepTitle = view.findViewById(R.id.tvStepTitle);
        layoutStep1 = view.findViewById(R.id.layoutStep1);
        layoutStep2 = view.findViewById(R.id.layoutStep2);
        layoutStep3 = view.findViewById(R.id.layoutStep3);
        btnBack = view.findViewById(R.id.btnBack);
        btnNext = view.findViewById(R.id.btnNext);
        btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto);
        tvImageStatus = view.findViewById(R.id.tvImageStatus);
        tvReviewSummary = view.findViewById(R.id.tvReviewSummary);

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etPrice = view.findViewById(R.id.etPrice);
        etLocation = view.findViewById(R.id.etLocation);
        etCategoryId = view.findViewById(R.id.etCategoryId);
        etStatus = view.findViewById(R.id.etStatus);
        etImageUrl = view.findViewById(R.id.etImageUrl);

        loadDraft();
        setupTextWatchers();

        btnNext.setOnClickListener(v -> {
            if (currentStep == 1) {
                if (validateStep1()) {
                    currentStep = 2;
                    updateStepUI();
                }
            } else if (currentStep == 2) {
                if (imageUrls.isEmpty() && etImageUrl.getText() != null && !etImageUrl.getText().toString().isEmpty()) {
                    imageUrls.add(etImageUrl.getText().toString().trim());
                }
                currentStep = 3;
                updateReviewSummary();
                updateStepUI();
            } else if (currentStep == 3) {
                submitPublication();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateStepUI();
            }
        });

        btnSelectPhoto.setOnClickListener(v -> galleryLauncher.launch("image/*"));
    }

    private void loadDraft() {
        PublicationCreateRequest draft = draftManager.loadDraft();
        if (draft != null) {
            if (draft.getTitle() != null) etTitle.setText(draft.getTitle());
            if (draft.getDescription() != null) etDescription.setText(draft.getDescription());
            if (draft.getPrice() != null) etPrice.setText(String.valueOf(draft.getPrice()));
            if (draft.getLocation() != null) etLocation.setText(draft.getLocation());
            if (draft.getCategoryId() != null) etCategoryId.setText(String.valueOf(draft.getCategoryId()));
            if (draft.getStatus() != null) etStatus.setText(draft.getStatus());
            if (draft.getImageUrls() != null) {
                imageUrls = new ArrayList<>(draft.getImageUrls());
                tvImageStatus.setText(imageUrls.size() + " fotos seleccionadas");
            }
            Toast.makeText(getContext(), "Borrador restaurado", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentDraft() {
        String title = etTitle.getText() != null ? etTitle.getText().toString() : "";
        String desc = etDescription.getText() != null ? etDescription.getText().toString() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString() : "0";
        double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);
        String loc = etLocation.getText() != null ? etLocation.getText().toString() : "";
        String catStr = etCategoryId.getText() != null ? etCategoryId.getText().toString() : "1";
        long catId = catStr.isEmpty() ? 1L : Long.parseLong(catStr);
        String status = etStatus.getText() != null && !etStatus.getText().toString().isEmpty() ? etStatus.getText().toString().toUpperCase() : "NEW";

        PublicationCreateRequest request = new PublicationCreateRequest(title, desc, price, status, loc, catId, imageUrls);
        draftManager.saveDraft(request);
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveCurrentDraft();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTitle.addTextChangedListener(watcher);
        etDescription.addTextChangedListener(watcher);
        etPrice.addTextChangedListener(watcher);
        etLocation.addTextChangedListener(watcher);
        etCategoryId.addTextChangedListener(watcher);
        etStatus.addTextChangedListener(watcher);
    }

    private boolean validateStep1() {
        if (etTitle.getText() == null || etTitle.getText().toString().trim().isEmpty()) {
            etTitle.setError("Ingrese un título");
            return false;
        }
        if (etDescription.getText() == null || etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Ingrese una descripción");
            return false;
        }
        if (etPrice.getText() == null || etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Ingrese un precio");
            return false;
        }
        if (etLocation.getText() == null || etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Ingrese una zona");
            return false;
        }
        return true;
    }

    private void updateStepUI() {
        if (currentStep == 1) {
            tvStepTitle.setText("Paso 1: Información Básica");
            layoutStep1.setVisibility(View.VISIBLE);
            layoutStep2.setVisibility(View.GONE);
            layoutStep3.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);
            btnNext.setText("Siguiente");
        } else if (currentStep == 2) {
            tvStepTitle.setText("Paso 2: Fotos del Artículo");
            layoutStep1.setVisibility(View.GONE);
            layoutStep2.setVisibility(View.VISIBLE);
            layoutStep3.setVisibility(View.GONE);
            btnBack.setVisibility(View.VISIBLE);
            btnNext.setText("Siguiente");
        } else if (currentStep == 3) {
            tvStepTitle.setText("Paso 3: Revisión y Publicación");
            layoutStep1.setVisibility(View.GONE);
            layoutStep2.setVisibility(View.GONE);
            layoutStep3.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.VISIBLE);
            btnNext.setText("Publicar Ahora");
        }
    }

    private void updateReviewSummary() {
        String summary = "Título: " + (etTitle.getText() != null ? etTitle.getText().toString() : "") + "\n" +
                "Precio: $" + (etPrice.getText() != null ? etPrice.getText().toString() : "") + "\n" +
                "Zona: " + (etLocation.getText() != null ? etLocation.getText().toString() : "") + "\n" +
                "Estado: " + (etStatus.getText() != null ? etStatus.getText().toString() : "") + "\n" +
                "Fotos: " + imageUrls.size() + " adjuntas";
        tvReviewSummary.setText(summary);
    }

    private void submitPublication() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        String loc = etLocation.getText().toString().trim();
        long catId = Long.parseLong(etCategoryId.getText().toString().trim());
        String status = etStatus.getText().toString().trim().toUpperCase();

        if (imageUrls.isEmpty()) {
            imageUrls.add("https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&q=80&w=800"); // Default fallback
        }

        PublicationCreateRequest request = new PublicationCreateRequest(title, desc, price, status, loc, catId, imageUrls);

        ApiClient.getPublicationService().createPublication(request).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                if (response.isSuccessful()) {
                    draftManager.clearDraft();
                    Toast.makeText(getContext(), "¡Publicado con éxito!", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(PublishWizardFragment.this).popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error al publicar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Publicacion> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
