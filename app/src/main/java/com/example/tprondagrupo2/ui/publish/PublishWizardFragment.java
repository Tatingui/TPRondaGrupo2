package com.example.tprondagrupo2.ui.publish;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.data.DraftManager;
import com.example.tprondagrupo2.model.PublicationCreateRequest;
import com.example.tprondagrupo2.util.PublicationConstants;
import com.google.android.material.textfield.TextInputEditText;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PublishWizardFragment extends Fragment {

    @Inject
    DraftManager.Factory draftManagerFactory;

    private PublishViewModel viewModel;
    private int currentStep = 1;
    private DraftManager draftManager;

    private TextView tvStepTitle;
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private Button btnBack, btnNext, btnSelectPhoto;
    private TextView tvImageStatus, tvReviewSummary;

    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etImageUrl, etDeliveryAddress;
    private android.widget.AutoCompleteTextView autoCompleteCategory, autoCompleteStatus;

    private List<Uri> imageUris = new ArrayList<>();
    private RecyclerView rvPhotoThumbnails;
    private PhotoThumbnailAdapter photoAdapter;

    private final Handler draftHandler = new Handler(Looper.getMainLooper());
    private Runnable draftRunnable;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    launchCrop(uri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri croppedUri = UCrop.getOutput(result.getData());
                    if (croppedUri != null) {
                        imageUris.add(croppedUri);
                        updatePhotoThumbnails();
                        saveCurrentDraft();
                    }
                }
            }
    );

    private void launchCrop(Uri sourceUri) {
        File destFile = new File(requireContext().getCacheDir(),
                "pub_crop_" + System.currentTimeMillis() + ".jpg");
        Uri destUri = Uri.fromFile(destFile);

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(85);
        options.setToolbarTitle("Recortar foto");

        Intent cropIntent = UCrop.of(sourceUri, destUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(800, 800)
                .withOptions(options)
                .getIntent(requireContext());

        cropLauncher.launch(cropIntent);
    }

    private void updatePhotoThumbnails() {
        if (rvPhotoThumbnails != null) {
            if (imageUris.isEmpty()) {
                rvPhotoThumbnails.setVisibility(View.GONE);
                tvImageStatus.setText("Seleccioná al menos una foto");
            } else {
                rvPhotoThumbnails.setVisibility(View.VISIBLE);
                tvImageStatus.setText(imageUris.size() + " foto(s) seleccionada(s)");
                if (photoAdapter != null) {
                    photoAdapter.notifyDataSetChanged();
                }
            }
        }
    }

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
        viewModel = new ViewModelProvider(this).get(PublishViewModel.class);

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

        String[] categories = PublicationConstants.CATEGORIES;
        android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteCategory.setAdapter(catAdapter);
        autoCompleteCategory.setText(categories[0], false);

        String[] statuses = {"Nuevo", "Como nuevo", "Usado"};
        android.widget.ArrayAdapter<String> statusAdapter = new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses);
        autoCompleteStatus.setAdapter(statusAdapter);
        autoCompleteStatus.setText(statuses[0], false);

        loadDraft();
        setupTextWatchers();
        setupViewModelObservers();

        btnNext.setOnClickListener(v -> {
            if (currentStep == 1) {
                if (validateStep1()) {
                    currentStep = 2;
                    updateStepUI();
                }
            } else if (currentStep == 2) {
                if (imageUris.isEmpty() && etImageUrl.getText() != null && !etImageUrl.getText().toString().isEmpty()) {
                    imageUris.add(Uri.parse(etImageUrl.getText().toString().trim()));
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

        // Setup photo thumbnails RecyclerView
        rvPhotoThumbnails = view.findViewById(R.id.rvPhotoThumbnails);
        photoAdapter = new PhotoThumbnailAdapter(imageUris, position -> {
            imageUris.remove(position);
            photoAdapter.notifyItemRemoved(position);
            photoAdapter.notifyItemRangeChanged(position, imageUris.size());
            updatePhotoThumbnails();
            saveCurrentDraft();
        });
        rvPhotoThumbnails.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPhotoThumbnails.setAdapter(photoAdapter);

        btnSelectPhoto.setOnClickListener(v -> galleryLauncher.launch("image/*"));
    }

    private void setupViewModelObservers() {
        viewModel.getPublishedResult().observe(getViewLifecycleOwner(), pub -> {
            if (pub != null) {
                draftManager.clearDraft();
                Toast.makeText(getContext(), "¡Publicado con éxito!", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(PublishWizardFragment.this).popBackStack();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnNext.setEnabled(!Boolean.TRUE.equals(isLoading));
        });
    }

    private Long getSelectedCategoryId() {
        String sel = autoCompleteCategory.getText() != null ? autoCompleteCategory.getText().toString() : "";
        for (int i = 0; i < PublicationConstants.CATEGORIES.length; i++) {
            if (PublicationConstants.CATEGORIES[i].equals(sel)) {
                return (long) (i + 1);
            }
        }
        return 1L;
    }

    private String getSelectedStatus() {
        String sel = autoCompleteStatus.getText() != null ? autoCompleteStatus.getText().toString() : "";
        return PublicationConstants.toBackendStatus(sel);
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
                String catName = PublicationConstants.getCategoryName(draft.getCategoryId());
                autoCompleteCategory.setText(catName, false);
            }
            if (draft.getStatus() != null) {
                String uiStatus = PublicationConstants.translateStatus(draft.getStatus());
                autoCompleteStatus.setText(uiStatus, false);
            }
            if (draft.getImageUrls() != null) {
                imageUris.clear();
                for (String url : draft.getImageUrls()) {
                    imageUris.add(Uri.parse(url));
                }
                updatePhotoThumbnails();
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

        List<String> urls = new ArrayList<>();
        for (Uri u : imageUris) {
            urls.add(u.toString());
        }
        PublicationCreateRequest request = new PublicationCreateRequest(title, desc, price, status, loc, catId, urls);
        request.setAddress(etDeliveryAddress.getText() == null ? "" : etDeliveryAddress.getText().toString().trim());
        draftManager.saveDraft(request);
    }

    private void saveCurrentDraftDebounced() {
        if (draftRunnable != null) {
            draftHandler.removeCallbacks(draftRunnable);
        }
        draftRunnable = this::saveCurrentDraft;
        draftHandler.postDelayed(draftRunnable, 500); // 500ms debounce
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveCurrentDraftDebounced();
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
                "Fotos: " + imageUris.size() + " adjuntas";
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

        List<String> finalUrls = new ArrayList<>();
        for (Uri u : imageUris) {
            finalUrls.add(u.toString());
        }
        if (finalUrls.isEmpty()) {
            finalUrls.add(getString(R.string.publish_default_image_url));
        }

        PublicationCreateRequest request = new PublicationCreateRequest(title, desc, price, status, loc, catId, finalUrls);
        request.setAddress(etDeliveryAddress.getText().toString().trim());

        viewModel.createPublication(request);
    }
}
