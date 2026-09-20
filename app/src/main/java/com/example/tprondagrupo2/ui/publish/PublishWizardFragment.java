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
import com.example.tprondagrupo2.network.PublicationApiService;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class PublishWizardFragment extends Fragment {

    @Inject
    PublicationApiService publicationApiService;

    @Inject
    DraftManager.Factory draftManagerFactory;

    private int currentStep = 1;
    private DraftManager draftManager;

    private TextView tvStepTitle;
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private Button btnBack, btnNext, btnSelectPhoto;
    private TextView tvImageStatus, tvReviewSummary;

    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etImageUrl, etDeliveryAddress;
    private android.widget.AutoCompleteTextView autoCompleteCategory, autoCompleteStatus;

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
        draftManager = draftManagerFactory.create();

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
        etDeliveryAddress = view.findViewById(R.id.etDeliveryAddress);
        autoCompleteCategory = view.findViewById(R.id.autoCompleteCategory);
        autoCompleteStatus = view.findViewById(R.id.autoCompleteStatus);
        etImageUrl = view.findViewById(R.id.etImageUrl);

        String[] categories = {"Deportes", "Hogar", "Electrónica", "Ropa", "Otros"};
        android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteCategory.setAdapter(catAdapter);
        autoCompleteCategory.setText(categories[0], false);

        String[] statuses = {"Nuevo", "Como nuevo", "Usado"};
        android.widget.ArrayAdapter<String> statusAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses);
        autoCompleteStatus.setAdapter(statusAdapter);
        autoCompleteStatus.setText(statuses[0], false);

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

    private Long getSelectedCategoryId() {
        String sel = autoCompleteCategory.getText() != null ? autoCompleteCategory.getText().toString() : "";
        switch (sel) {
            case "Hogar": return 2L;
            case "Electrónica": return 3L;
            case "Ropa": return 4L;
            case "Otros": return 5L;
            default: return 1L; // Deportes
        }
    }

    private String getSelectedStatus() {
        String sel = autoCompleteStatus.getText() != null ? autoCompleteStatus.getText().toString() : "";
        if ("Como nuevo".equals(sel)) return "LIKE_NEW";
        if ("Usado".equals(sel)) return "USED";
        return "NEW";
    }

    private void loadDraft() {
        PublicationCreateRequest draft = draftManager.loadDraft();
        if (draft != null) {
            if (draft.getTitle() != null) etTitle.setText(draft.getTitle());
            if (draft.getDescription() != null) etDescription.setText(draft.getDescription());
            if (draft.getPrice() != null) etPrice.setText(String.valueOf(draft.getPrice()));
            if (draft.getLocation() != null) etLocation.setText(draft.getLocation());
            if (draft.getAddress() != null) etDeliveryAddress.setText(draft.getAddress());
            if (draft.getCategoryId() != null) {
                int idx = (int) (draft.getCategoryId() - 1);
                String[] categories = {"Deportes", "Hogar", "Electrónica", "Ropa", "Otros"};
                if (idx >= 0 && idx < categories.length) {
                    autoCompleteCategory.setText(categories[idx], false);
                }
            }
            if (draft.getStatus() != null) {
                String s = draft.getStatus();
                if ("LIKE_NEW".equals(s)) autoCompleteStatus.setText("Como nuevo", false);
                else if ("USED".equals(s)) autoCompleteStatus.setText("Usado", false);
                else autoCompleteStatus.setText("Nuevo", false);
            }
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
        double price;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) { price = 0.0; }
        String loc = etLocation.getText() != null ? etLocation.getText().toString() : "";
        long catId = getSelectedCategoryId();
        String status = getSelectedStatus();

        PublicationCreateRequest request = new PublicationCreateRequest(title, desc, price, status, loc, catId, imageUrls);
        request.setAddress(etDeliveryAddress.getText() == null ? "" : etDeliveryAddress.getText().toString().trim());
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
        etDeliveryAddress.addTextChangedListener(watcher);
        autoCompleteCategory.addTextChangedListener(watcher);
        autoCompleteStatus.addTextChangedListener(watcher);
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
        if (!DeliveryAddress.isValid(etDeliveryAddress.getText() == null ? null : etDeliveryAddress.getText().toString())) {
            etDeliveryAddress.setError(getString(R.string.delivery_address_invalid));
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
                "Dirección de entrega (privada): " + etDeliveryAddress.getText() + "\n" +
                "Categoría: " + autoCompleteCategory.getText().toString() + "\n" +
                "Estado: " + autoCompleteStatus.getText().toString() + "\n" +
                "Fotos: " + imageUrls.size() + " adjuntas";
        tvReviewSummary.setText(summary);
    }

    private void submitPublication() {
        if (!validateStep1()) {
            currentStep = 1;
            updateStepUI();
            return;
        }
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        String loc = etLocation.getText().toString().trim();
        long catId = getSelectedCategoryId();
        String status = getSelectedStatus();

        if (imageUrls.isEmpty()) {
            imageUrls.add("https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&q=80&w=800"); // Default fallback
        }

        PublicationCreateRequest request = new PublicationCreateRequest(title, desc, price, status, loc, catId, imageUrls);
        request.setAddress(etDeliveryAddress.getText().toString().trim());
        publicationApiService.createPublication(request).enqueue(new Callback<Publicacion>() {
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
