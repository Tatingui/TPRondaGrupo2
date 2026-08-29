package com.example.tprondagrupo2.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Publication;
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.ApiClient;
import com.example.tprondagrupo2.network.PublicationPageResponse;
import com.example.tprondagrupo2.ui.detalle.DetallePublicacionFragment;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView rvPublications;
    private PublicationAdapter adapter;
    private List<Publication> displayedPublications;
    private EditText etSearch;

    // Filter states
    private String currentSearchText = "";
    private Long selectedCategoryId = null;
    private String selectedCategoryName = "Categoría";
    private String selectedCondition = null;
    private String selectedConditionName = "Estado";
    private String selectedLocation = null;
    private String selectedLocationName = "Zona";
    private Double minPrice = null;
    private Double maxPrice = null;
    private String currentSort = "createdAt,desc";
    private String currentSortName = "Ordenar por";

    // Pagination states
    private int currentPage = 0;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPublications = view.findViewById(R.id.rvPublications);
        etSearch = view.findViewById(R.id.etSearch);

        displayedPublications = new ArrayList<>();

        setupRecyclerView();
        setupSearchLogic();
        setupFilters(view);

        refreshData();
    }

    private void setupRecyclerView() {
        adapter = new PublicationAdapter(displayedPublications, this::abrirDetalle);
        rvPublications.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvPublications.setAdapter(adapter);

        rvPublications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        fetchPublications();
                    }
                }
            }
        });
    }

    private void fetchPublications() {
        if (isLoading) return;
        isLoading = true;

        ApiClient.getPublicationService().getPublications(
                currentSearchText.isEmpty() ? null : currentSearchText,
                selectedCategoryId,
                minPrice,
                maxPrice,
                selectedCondition,
                selectedLocation,
                currentPage,
                PAGE_SIZE,
                currentSort
        ).enqueue(new Callback<PublicationPageResponse>() {
            @Override
            public void onResponse(Call<PublicationPageResponse> call, Response<PublicationPageResponse> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Publication> newItems = response.body().getContent();
                    if (currentPage == 0) {
                        displayedPublications.clear();
                        displayedPublications.addAll(newItems);
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter.addItems(newItems);
                    }

                    isLastPage = response.body().isLast();
                    if (!isLastPage) {
                        currentPage++;
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar publicaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PublicationPageResponse> call, Throwable t) {
                isLoading = false;
                Log.e(TAG, "Falla en la peticion", t);
                Toast.makeText(getContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshData() {
        currentPage = 0;
        isLastPage = false;
        fetchPublications();
    }

    private void abrirDetalle(Publication publication) {
        Bundle args = new Bundle();
        args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, mapearADetalle(publication));

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_detalle, args);
    }

    private Publicacion mapearADetalle(Publication publication) {
        Vendedor vendedor = new Vendedor(
                publication.getSellerId().toString(),
                publication.getSellerName(),
                4.5, // Reputacion mock
                15,  // Ventas mock
                10,  // Opiniones mock
                "2 años",
                publication.getLocation()
        );

        return new Publicacion(
                publication.getId().toString(),
                publication.getTitle(),
                publication.getImageUrls(),
                publication.getDescription(),
                publication.getCategoryName(),
                publication.getStatus(),
                publication.getPrice(),
                publication.getCreatedAt(),
                vendedor);
    }

    private void setupSearchLogic() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString();
                refreshData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters(View view) {
        view.findViewById(R.id.chipSort).setOnClickListener(v -> showSortDialog());
        view.findViewById(R.id.chipFilterCategory).setOnClickListener(v -> showCategoryDialog());
        view.findViewById(R.id.chipFilterPrice).setOnClickListener(v -> showPriceDialog());
        view.findViewById(R.id.chipFilterCondition).setOnClickListener(v -> showConditionDialog());
        view.findViewById(R.id.chipFilterLocation).setOnClickListener(v -> showLocationDialog());
    }

    private void showSortDialog() {
        String[] options = {"Recientes", "Menor precio", "Mayor precio"};
        String[] sortValues = {"createdAt,desc", "price,asc", "price,desc"};
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Ordenar por")
                .setItems(options, (dialog, which) -> {
                    currentSort = sortValues[which];
                    currentSortName = options[which];
                    ((Chip) getView().findViewById(R.id.chipSort)).setText("Orden: " + currentSortName);
                    refreshData();
                })
                .show();
    }

    private void showCategoryDialog() {
        String[] categories = {"Todas", "Deportes", "Hogar", "Electrónica", "Ropa", "Otros"};
        Long[] ids = {null, 1L, 2L, 3L, 4L, 5L};

        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar Categoría")
                .setItems(categories, (dialog, which) -> {
                    selectedCategoryId = ids[which];
                    selectedCategoryName = categories[which];
                    ((Chip) getView().findViewById(R.id.chipFilterCategory)).setText(selectedCategoryName);
                    refreshData();
                })
                .show();
    }

    private void showPriceDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_price_filter, null);
        EditText etMin = dialogView.findViewById(R.id.etMinPrice);
        EditText etMax = dialogView.findViewById(R.id.etMaxPrice);

        if (minPrice != null) etMin.setText(String.valueOf(minPrice));
        if (maxPrice != null) etMax.setText(String.valueOf(maxPrice));

        new AlertDialog.Builder(requireContext())
                .setTitle("Rango de Precio")
                .setView(dialogView)
                .setPositiveButton("Aplicar", (dialog, which) -> {
                    String minStr = etMin.getText().toString();
                    String maxStr = etMax.getText().toString();
                    minPrice = minStr.isEmpty() ? null : Double.parseDouble(minStr);
                    maxPrice = maxStr.isEmpty() ? null : Double.parseDouble(maxStr);
                    refreshData();
                })
                .setNegativeButton("Limpiar", (dialog, which) -> {
                    minPrice = null;
                    maxPrice = null;
                    refreshData();
                })
                .show();
    }

    private void showConditionDialog() {
        String[] options = {"Cualquiera", "Nuevo", "Como nuevo", "Usado"};
        String[] values = {null, "NUEVO", "COMO_NUEVO", "USADO"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Estado del Artículo")
                .setItems(options, (dialog, which) -> {
                    selectedCondition = values[which];
                    selectedConditionName = options[which];
                    ((Chip) getView().findViewById(R.id.chipFilterCondition)).setText(selectedConditionName);
                    refreshData();
                })
                .show();
    }

    private void showLocationDialog() {
        String[] locations = {"Todas", "Palermo", "Almagro", "Belgrano", "Caballito", "Villa Urquiza"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Zona del Vendedor")
                .setItems(locations, (dialog, which) -> {
                    selectedLocation = locations[which].equals("Todas") ? null : locations[which];
                    selectedLocationName = locations[which];
                    ((Chip) getView().findViewById(R.id.chipFilterLocation)).setText(selectedLocationName);
                    refreshData();
                })
                .show();
    }
}
