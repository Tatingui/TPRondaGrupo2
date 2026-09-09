package com.example.tprondagrupo2.ui;

import android.os.Build;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Publication;
import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.model.Vendedor;
import com.example.tprondagrupo2.network.ApiClient;
import com.example.tprondagrupo2.network.PublicationPageResponse;
import com.example.tprondagrupo2.ui.detalle.DetallePublicacionFragment;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView rvPublications;
    private PublicationAdapter adapter;
    private List<Publication> displayedPublications;
    private EditText etSearch;
    private Set<Long> favoriteIds = new HashSet<>();

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
        checkIncomingSavedSearch();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFavoriteIds();
    }

    private void fetchFavoriteIds() {
        ApiClient.getPublicationService().getFavorites().enqueue(new Callback<List<Publication>>() {
            @Override
            public void onResponse(Call<List<Publication>> call, Response<List<Publication>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteIds.clear();
                    for (Publication p : response.body()) {
                        favoriteIds.add(p.getId());
                    }
                    refreshData();
                } else if (response.code() == 401 || response.code() == 403) {
                    Toast.makeText(getContext(), "Sesión vencida o inválida. Iniciá sesión de nuevo.", Toast.LENGTH_SHORT).show();
                    refreshData();
                } else {
                    refreshData();
                }
            }

            @Override
            public void onFailure(Call<List<Publication>> call, Throwable t) {
                refreshData();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new PublicationAdapter(displayedPublications, this::abrirDetalle, this::onFavoriteClick);
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
                    
                    // Sincronizar estado de favoritos
                    for (Publication p : newItems) {
                        p.setFavorite(favoriteIds.contains(p.getId()));
                    }

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
        Publicacion p = mapearADetalle(publication);
        p.setFavorite(publication.isFavorite());
        Log.d(TAG, "Abriendo detalle para ID: " + p.getId());
        args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, p);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_detalle, args);
    }

    private void onFavoriteClick(Publication publication, int position) {
        boolean isFavorite = publication.isFavorite();
        String pubId = String.valueOf(publication.getId());

        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    publication.setFavorite(!isFavorite);
                    if (publication.isFavorite()) {
                        favoriteIds.add(publication.getId());
                    } else {
                        favoriteIds.remove(publication.getId());
                    }
                    adapter.notifyItemChanged(position);
                } else {
                    Toast.makeText(getContext(), "Error al actualizar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        };

        if (isFavorite) {
            ApiClient.getPublicationService().unmarkAsFavorite(pubId).enqueue(callback);
        } else {
            ApiClient.getPublicationService().markAsFavorite(pubId).enqueue(callback);
        }
    }

    private Publicacion mapearADetalle(Publication publication) {
        Vendedor vendedor = null;
        if (publication.getSellerId() != null) {
            vendedor = new Vendedor(
                    publication.getSellerId().toString(),
                    publication.getSellerName(),
                    4.5, // Reputacion mock
                    15,  // Ventas mock
                    10,  // Opiniones mock
                    "2 años",
                    publication.getLocation()
            );
        }

        return new Publicacion(
                publication.getId() != null ? publication.getId().toString() : "0",
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
        view.findViewById(R.id.chipSaveSearch).setOnClickListener(v -> guardarBusquedaActual());
        view.findViewById(R.id.chipSort).setOnClickListener(v -> showSortDialog());
        view.findViewById(R.id.chipFilterCategory).setOnClickListener(v -> showCategoryDialog());
        view.findViewById(R.id.chipFilterPrice).setOnClickListener(v -> showPriceDialog());
        view.findViewById(R.id.chipFilterCondition).setOnClickListener(v -> showConditionDialog());
        view.findViewById(R.id.chipFilterLocation).setOnClickListener(v -> showLocationDialog());
    }

    private void guardarBusquedaActual() {
        SavedSearch savedSearch = new SavedSearch(
                null,
                currentSearchText,
                selectedCategoryId,
                selectedCategoryName,
                selectedCondition,
                selectedConditionName,
                selectedLocation,
                selectedLocationName,
                minPrice,
                maxPrice,
                currentSort,
                currentSortName,
                null
        );

        if (!savedSearch.hasAnyFilterOrQuery()) {
            Toast.makeText(getContext(), R.string.busqueda_vacia_error, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getSavedSearchService().saveSearch(savedSearch).enqueue(new Callback<SavedSearch>() {
            @Override
            public void onResponse(Call<SavedSearch> call, Response<SavedSearch> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.busqueda_guardada_exito, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al guardar la búsqueda", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SavedSearch> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIncomingSavedSearch() {
        if (getArguments() != null && getArguments().containsKey("saved_search")) {
            SavedSearch savedSearch;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedSearch = getArguments().getSerializable("saved_search", SavedSearch.class);
            } else {
                //noinspection deprecation
                savedSearch = (SavedSearch) getArguments().getSerializable("saved_search");
            }
            if (savedSearch != null) {
                applySavedSearch(savedSearch);
                getArguments().remove("saved_search");
            }
        }
    }

    public void applySavedSearch(SavedSearch search) {
        if (search == null) return;
        this.currentSearchText = search.getQuery() != null ? search.getQuery() : "";
        if (etSearch != null) {
            etSearch.setText(this.currentSearchText);
        }
        this.selectedCategoryId = search.getCategoryId();
        this.selectedCategoryName = search.getCategoryName() != null ? search.getCategoryName() : "Categoría";
        this.selectedCondition = search.getCondition();
        this.selectedConditionName = search.getConditionName() != null ? search.getConditionName() : "Estado";
        this.selectedLocation = search.getLocation();
        this.selectedLocationName = search.getLocationName() != null ? search.getLocationName() : "Zona";
        this.minPrice = search.getMinPrice();
        this.maxPrice = search.getMaxPrice();
        this.currentSort = search.getSort() != null ? search.getSort() : "createdAt,desc";
        this.currentSortName = search.getSortName() != null ? search.getSortName() : "Ordenar por";

        updateChipLabels();
        refreshData();
    }

    private void updateChipLabels() {
        View view = getView();
        if (view == null) return;
        Chip chipSort = view.findViewById(R.id.chipSort);
        Chip chipCat = view.findViewById(R.id.chipFilterCategory);
        Chip chipCond = view.findViewById(R.id.chipFilterCondition);
        Chip chipLoc = view.findViewById(R.id.chipFilterLocation);

        if (chipSort != null) chipSort.setText("Orden: " + currentSortName);
        if (chipCat != null) chipCat.setText(selectedCategoryName);
        if (chipCond != null) chipCond.setText(selectedConditionName);
        if (chipLoc != null) chipLoc.setText(selectedLocationName);
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
