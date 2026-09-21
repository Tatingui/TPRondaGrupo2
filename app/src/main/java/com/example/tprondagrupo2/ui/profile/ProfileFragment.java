package com.example.tprondagrupo2.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.tprondagrupo2.BuildConfig;
import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.data.ThemePreferenceManager;
import com.example.tprondagrupo2.data.repository.UserRepository;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.model.SavedSearchDataStoreItem;
import com.example.tprondagrupo2.model.UserProfile;
import com.example.tprondagrupo2.model.UserProfileUpdateRequest;
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.FavoritesDataStoreManager;
import com.example.tprondagrupo2.network.NetworkObserver;
import com.example.tprondagrupo2.network.SavedSearchesDataStoreManager;
import com.example.tprondagrupo2.ui.PublicationAdapter;
import com.example.tprondagrupo2.ui.detalle.DetallePublicacionFragment;
import com.example.tprondagrupo2.ui.detalle.VendedorViewBinder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    @Inject
    FavoritesDataStoreManager favoritesDataStoreManager;

    @Inject
    SavedSearchesDataStoreManager savedSearchesDataStoreManager;

    @Inject
    UserRepository userRepository;

    private ProfileViewModel viewModel;

    private TextView tvAvatar;
    private ImageView ivAvatar;
    private TextView tvNombre;
    private TextView tvNivel;
    private RatingBar rbReputacion;
    private TextView tvReputacion;
    private TextView tvVentas;
    private TextView tvMiembroDesde;
    private TextView tvUbicacion;
    private TextView tvEmail;
    private TextView tvTelefono;
    private Button btnEditProfile;
    private FloatingActionButton fabChangePhoto;
    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private RecyclerView rvSavedSearches;
    private TextView tvEmptySavedSearches;
    private SavedSearchAdapter savedSearchAdapter;
    private final List<SavedSearch> savedSearches = new ArrayList<>();

    private PublicationAdapter adapter;
    private final List<Publicacion> favoritePublications = new ArrayList<>();

    private UserProfile currentProfile;

    // ==================== PHOTO PICKER + UCROP ====================

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    launchCrop(uri);
                }
            });

    private final ActivityResultLauncher<android.content.Intent> cropLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri croppedUri = UCrop.getOutput(result.getData());
                    if (croppedUri != null) {
                        uploadProfilePhoto(croppedUri);
                    }
                }
            });

    private void launchCrop(Uri sourceUri) {
        File destFile = new File(requireContext().getCacheDir(), "profile_crop_" + System.currentTimeMillis() + ".jpg");
        Uri destUri = Uri.fromFile(destFile);

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(85);
        options.setToolbarTitle("Recortar foto");
        options.setCircleDimmedLayer(true);

        android.content.Intent cropIntent = UCrop.of(sourceUri, destUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(512, 512)
                .withOptions(options)
                .getIntent(requireContext());

        cropLauncher.launch(cropIntent);
    }

    private void uploadProfilePhoto(Uri imageUri) {
        try {
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(getContext(), "No se pudo leer la imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            inputStream.close();

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("image/jpeg"),
                    buffer.toByteArray()
            );
            MultipartBody.Part part = MultipartBody.Part.createFormData("foto", "profile.jpg", requestBody);

            Toast.makeText(getContext(), "Subiendo foto...", Toast.LENGTH_SHORT).show();
            viewModel.uploadAvatar(part);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== LIFECYCLE ====================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        tvAvatar = view.findViewById(R.id.tvPerfilAvatar);
        ivAvatar = view.findViewById(R.id.ivPerfilAvatar);
        tvNombre = view.findViewById(R.id.tvPerfilNombre);
        tvNivel = view.findViewById(R.id.tvPerfilNivel);
        rbReputacion = view.findViewById(R.id.rbPerfilReputacion);
        tvReputacion = view.findViewById(R.id.tvPerfilReputacion);
        tvVentas = view.findViewById(R.id.tvPerfilVentas);
        tvMiembroDesde = view.findViewById(R.id.tvPerfilMiembroDesde);
        tvUbicacion = view.findViewById(R.id.tvPerfilUbicacion);
        tvEmail = view.findViewById(R.id.tvPerfilEmail);
        tvTelefono = view.findViewById(R.id.tvPerfilTelefono);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        fabChangePhoto = view.findViewById(R.id.fabChangePhoto);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvSavedSearches = view.findViewById(R.id.rvSavedSearches);
        tvEmptySavedSearches = view.findViewById(R.id.tvEmptySavedSearches);

        view.findViewById(R.id.btnGoPublish).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_publish)
        );
        view.findViewById(R.id.btnGoMyPublications).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_my_publications)
        );
        view.findViewById(R.id.btnGoMyOffers).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profile_to_my_offers)
        );

        if (fabChangePhoto != null) {
            fabChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        com.google.android.material.switchmaterial.SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);
        ThemePreferenceManager themeManager = new ThemePreferenceManager(requireContext());
        switchDarkMode.setChecked(themeManager.isDarkModeEnabled());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeManager.setDarkModeEnabled(isChecked);
            requireActivity().recreate();
        });

        // Cerrar sesion
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            userRepository.logout();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_profile_to_login);
        });

        // Borrar cuenta
        view.findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Borrar cuenta")
                    .setMessage("¿Estás seguro? Se eliminarán todos tus datos, publicaciones, ofertas y calificaciones. Esta acción no se puede deshacer.")
                    .setPositiveButton("Sí, borrar", (dialog, which) -> {
                        viewModel.deleteAccount();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditDialog());
        }

        setupSavedSearchesRecyclerView();
        setupRecyclerView();
        setupViewModelObservers();

        viewModel.loadProfile();
    }

    private void setupViewModelObservers() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                currentProfile = profile;
                mostrarPerfil(currentProfile);
            } else {
                mostrarPerfilMock();
            }
        });

        viewModel.getSavedSearches().observe(getViewLifecycleOwner(), searches -> {
            savedSearches.clear();
            if (searches != null) {
                savedSearches.addAll(searches);
            }
            if (savedSearchAdapter != null) {
                savedSearchAdapter.notifyDataSetChanged();
            }
            if (tvEmptySavedSearches != null) {
                tvEmptySavedSearches.setVisibility(savedSearches.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getFavoritePublications().observe(getViewLifecycleOwner(), favorites -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            updateFavoritesList(favorites);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && isAdded()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getAccountDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (Boolean.TRUE.equals(deleted) && isAdded()) {
                Toast.makeText(requireContext(), "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(ProfileFragment.this)
                        .navigate(R.id.action_profile_to_login);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadSavedSearches();
        viewModel.loadFavorites();
    }

    private void mostrarPerfil(UserProfile perfil) {
        if (tvNombre != null) tvNombre.setText(perfil.getNombre());

        if (perfil.getProfileImageUrl() != null && !perfil.getProfileImageUrl().isEmpty()) {
            String imageUrl = perfil.getProfileImageUrl();
            if (imageUrl.startsWith("/")) {
                String baseUrl = BuildConfig.BASE_URL;
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }
                imageUrl = baseUrl + imageUrl;
            }

            if (ivAvatar != null && isAdded()) {
                ivAvatar.setVisibility(View.VISIBLE);
                tvAvatar.setVisibility(View.GONE);
                Glide.with(this)
                        .load(imageUrl)
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.bg_avatar_vendedor)
                        .error(R.drawable.bg_avatar_vendedor)
                        .into(ivAvatar);
            }
        } else {
            if (ivAvatar != null) ivAvatar.setVisibility(View.GONE);
            if (tvAvatar != null) tvAvatar.setVisibility(View.VISIBLE);
        }

        if (tvEmail != null) {
            tvEmail.setText(perfil.getEmail());
            tvEmail.setVisibility(View.VISIBLE);
        }

        if (tvTelefono != null) {
            if (perfil.getTelefono() != null && !perfil.getTelefono().isEmpty()) {
                tvTelefono.setText(perfil.getTelefono());
                tvTelefono.setVisibility(View.VISIBLE);
            } else {
                tvTelefono.setVisibility(View.GONE);
            }
        }

        if (tvMiembroDesde != null && perfil.getMiembroDesde() != null) {
            tvMiembroDesde.setText(getString(R.string.vendedor_miembro_desde, perfil.getMiembroDesde()));
        }

        if (tvUbicacion != null) {
            if (perfil.getZona() != null && !perfil.getZona().isEmpty()) {
                tvUbicacion.setVisibility(View.VISIBLE);
                tvUbicacion.setText(getString(R.string.perfil_vendedor_ubicacion, perfil.getZona()));
            } else {
                tvUbicacion.setVisibility(View.GONE);
            }
        }

        VendedorViewBinder.bindReputacion(perfil, tvAvatar, rbReputacion, tvReputacion, tvNivel);
        if (tvVentas != null) {
            tvVentas.setText(getString(R.string.vendedor_operaciones,
                    perfil.getCantidadVentas(), perfil.getCantidadCompras()));
        }
    }

    private void mostrarPerfilMock() {
        Vendedor miPerfil = new Vendedor(0L, "Mi Usuario", 0, 0, 0, "Enero 2024", "Mi Ciudad");

        if (tvNombre != null) tvNombre.setText(miPerfil.getNombre());
        VendedorViewBinder.bindReputacion(miPerfil, tvAvatar, rbReputacion, tvReputacion, tvNivel);

        if (tvVentas != null) {
            tvVentas.setText(getString(R.string.vendedor_operaciones,
                    miPerfil.getCantidadVentas(), miPerfil.getCantidadCompras()));
        }
        if (tvMiembroDesde != null) tvMiembroDesde.setText(getString(R.string.vendedor_miembro_desde, miPerfil.getMiembroDesde()));

        if (tvUbicacion != null) {
            if (miPerfil.getUbicacion() != null && !miPerfil.getUbicacion().isEmpty()) {
                tvUbicacion.setVisibility(View.VISIBLE);
                tvUbicacion.setText(getString(R.string.perfil_vendedor_ubicacion, miPerfil.getUbicacion()));
            } else {
                tvUbicacion.setVisibility(View.GONE);
            }
        }
    }

    // ==================== EDICION DEL PERFIL ====================

    private void showEditDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText etEditNombre = dialogView.findViewById(R.id.etEditNombre);
        EditText etEditTelefono = dialogView.findViewById(R.id.etEditTelefono);
        AutoCompleteTextView etEditZona = dialogView.findViewById(R.id.etEditZona);

        String[] zonas = getResources().getStringArray(R.array.zonas_argentina);
        ArrayAdapter<String> zonaAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                zonas
        );
        etEditZona.setAdapter(zonaAdapter);

        if (currentProfile != null) {
            etEditNombre.setText(currentProfile.getNombre());
            etEditTelefono.setText(currentProfile.getTelefono());
            etEditZona.setText(currentProfile.getZona());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Editar perfil")
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = etEditNombre.getText().toString().trim();
            String telefono = etEditTelefono.getText().toString().trim();
            String zona = etEditZona.getText().toString().trim();

            if (nombre.isEmpty()) {
                etEditNombre.setError("El nombre no puede estar vacío");
                etEditNombre.requestFocus();
                return;
            }

            UserProfileUpdateRequest update = new UserProfileUpdateRequest(
                    nombre,
                    telefono.isEmpty() ? null : telefono,
                    zona.isEmpty() ? null : zona
            );
            viewModel.updateProfile(update);
            dialog.dismiss();
        });
    }

    // ==================== BUSQUEDAS GUARDADAS ====================

    private void setupSavedSearchesRecyclerView() {
        if (rvSavedSearches == null) return;
        savedSearchAdapter = new SavedSearchAdapter(savedSearches, new SavedSearchAdapter.OnSavedSearchClickListener() {
            @Override
            public void onSearchClick(SavedSearch savedSearch) {
                if (savedSearch != null && savedSearch.getId() != null && getContext() != null) {
                    SavedSearchDataStoreItem dsItem = savedSearchesDataStoreManager.getSavedSearchesMap()
                            .get(String.valueOf(savedSearch.getId()));
                    List<String> knownIds = dsItem != null ? dsItem.getPublicationIds() : new ArrayList<>();
                    savedSearchesDataStoreManager.updateSearchUpdates(String.valueOf(savedSearch.getId()), knownIds, false);
                    savedSearch.setHasUpdates(false);
                }
                Bundle args = new Bundle();
                args.putSerializable("saved_search", savedSearch);
                NavHostFragment.findNavController(ProfileFragment.this)
                        .navigate(R.id.action_profile_to_home, args);
            }

            @Override
            public void onDeleteClick(SavedSearch savedSearch, int position) {
                if (getContext() == null || savedSearch == null || savedSearch.getId() == null) return;
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.perfil_mis_busquedas)
                        .setMessage(R.string.eliminar_busqueda_confirm)
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            savedSearchesDataStoreManager.removeSearch(String.valueOf(savedSearch.getId()));
                            viewModel.deleteSavedSearch(savedSearch.getId());
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
        rvSavedSearches.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSavedSearches.setAdapter(savedSearchAdapter);
        rvSavedSearches.setNestedScrollingEnabled(false);
    }

    // ==================== FAVORITOS ====================

    private void setupRecyclerView() {
        if (rvFavorites == null) return;
        adapter = new PublicationAdapter(favoritePublications, this::abrirDetalle, this::onFavoriteClick);
        rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvFavorites.setAdapter(adapter);
        rvFavorites.setNestedScrollingEnabled(false);
    }

    private void updateFavoritesList(List<Publicacion> favorites) {
        favoritePublications.clear();
        if (favorites != null && getContext() != null) {
            Map<String, Boolean> hasUpdatesMap = favoritesDataStoreManager.getHasUpdatesMap();
            for (Publicacion p : favorites) {
                p.setFavorite(true);
                if (p.getLastSeenPrice() != null && p.getPrice() < p.getLastSeenPrice()) {
                    favoritesDataStoreManager.setHasUpdates(p.getId(), true);
                    p.setHasUpdates(true);
                } else if (p.getId() != null && hasUpdatesMap.containsKey(p.getId())) {
                    p.setHasUpdates(Boolean.TRUE.equals(hasUpdatesMap.get(p.getId())));
                }
            }
            favoritePublications.addAll(favorites);
        }
        if (adapter != null) adapter.notifyDataSetChanged();

        if (favoritePublications.isEmpty()) {
            if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
        } else {
            if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
        }
    }

    private void abrirDetalle(Publicacion publicacion) {
        Bundle args = new Bundle();
        args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, publicacion);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_profile_to_detalle, args);
    }

    private void onFavoriteClick(Publicacion publicacion, int position) {
        if (publicacion == null || publicacion.getId() == null) return;
        boolean isFavorite = publicacion.isFavorite();
        String pubId = publicacion.getId();

        if (!isFavorite) {
            favoritesDataStoreManager.addFavorite(pubId);
        } else {
            favoritesDataStoreManager.removeFavorite(pubId);
        }
    }
}
