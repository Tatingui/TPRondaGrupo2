package com.example.tprondagrupo2.ui;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.SavedSearch;
import com.example.tprondagrupo2.network.FavoritesDataStoreManager;
import com.example.tprondagrupo2.network.NetworkObserver;
import com.example.tprondagrupo2.network.SavedSearchesDataStoreManager;
import com.example.tprondagrupo2.ui.detalle.DetallePublicacionFragment;
import com.example.tprondagrupo2.ui.home.HomeViewModel;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    @Inject
    FavoritesDataStoreManager favoritesDataStoreManager;

    @Inject
    SavedSearchesDataStoreManager savedSearchesDataStoreManager;

    private HomeViewModel viewModel;

    private RecyclerView rvPublications;
    private PublicationAdapter adapter;
    private final List<Publicacion> displayedPublications = new ArrayList<>();
    private EditText etSearch;
    private TextView tvOfflineBanner;
    private NetworkObserver networkObserver;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        rvPublications = view.findViewById(R.id.rvPublications);
        etSearch = view.findViewById(R.id.etSearch);
        tvOfflineBanner = view.findViewById(R.id.tvOfflineBanner);

        networkObserver = new NetworkObserver(requireContext());

        setupRecyclerView();
        setupSearchLogic();
        setupFilters(view);
        setupViewModelObservers();
        checkIncomingSavedSearch();

        networkObserver.getIsConnected().observe(getViewLifecycleOwner(), connected -> {
            if (Boolean.TRUE.equals(connected)) {
                tvOfflineBanner.setVisibility(View.GONE);
                refreshData();
            } else {
                tvOfflineBanner.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.fetchFavoriteIds(this::refreshData);
    }

    private void setupViewModelObservers() {
        viewModel.getPublications().observe(getViewLifecycleOwner(), publications -> {
            displayedPublications.clear();
            if (publications != null) {
                displayedPublications.addAll(publications);
            }
            adapter.notifyDataSetChanged();
        });

        viewModel.getOfflineBannerVisible().observe(getViewLifecycleOwner(), visible -> {
            tvOfflineBanner.setVisibility(Boolean.TRUE.equals(visible) ? View.VISIBLE : View.GONE);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && isAdded()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSavedSearchSuccess().observe(getViewLifecycleOwner(), savedSearch -> {
            if (savedSearch != null && isAdded()) {
                List<String> pubIds = new ArrayList<>();
                for (Publicacion p : displayedPublications) {
                    if (p.getId() != null) {
                        pubIds.add(p.getId());
                    }
                }
                if (savedSearch.getId() != null && getContext() != null) {
                    savedSearchesDataStoreManager.saveSearch(String.valueOf(savedSearch.getId()), pubIds);
                }
                Toast.makeText(getContext(), R.string.busqueda_guardada_exito, Toast.LENGTH_SHORT).show();
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
                boolean isLoading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
                boolean isLastPage = Boolean.TRUE.equals(viewModel.getIsLastPage().getValue());

                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        currentPage++;
                        fetchPublications();
                    }
                }
            }
        });
    }

    private void fetchPublications() {
        boolean connected = networkObserver.isCurrentlyConnected();
        viewModel.loadPublications(
                currentSearchText.isEmpty() ? null : currentSearchText,
                selectedCategoryId,
                minPrice,
                maxPrice,
                selectedCondition,
                selectedLocation,
                currentPage,
                PAGE_SIZE,
                currentSort,
                connected
        );
    }

    private void refreshData() {
        currentPage = 0;
        fetchPublications();
    }

    private void abrirDetalle(Publicacion publicacion) {
        Bundle args = new Bundle();
        args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, publicacion);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_detalle, args);
    }

    private void onFavoriteClick(Publicacion publicacion, int position) {
        if (publicacion == null || publicacion.getId() == null) return;
        boolean wasFavorite = publicacion.isFavorite();
        String pubId = publicacion.getId();

        viewModel.toggleFavorite(publicacion);

        if (!wasFavorite) {
            favoritesDataStoreManager.addFavorite(pubId);
        } else {
            favoritesDataStoreManager.removeFavorite(pubId);
        }
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
        if (!networkObserver.isCurrentlyConnected()) {
            Toast.makeText(getContext(), "Se necesita conexión para guardar búsquedas", Toast.LENGTH_SHORT).show();
            return;
        }

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

        viewModel.saveSearch(savedSearch);
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
                    View view = getView();
                    if (view != null) {
                        Chip chip = view.findViewById(R.id.chipSort);
                        if (chip != null) chip.setText("Orden: " + currentSortName);
                    }
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
                    View view = getView();
                    if (view != null) {
                        Chip chip = view.findViewById(R.id.chipFilterCategory);
                        if (chip != null) chip.setText(selectedCategoryName);
                    }
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
        String[] values = {null, "NEW", "LIKE_NEW", "USED"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Estado del Artículo")
                .setItems(options, (dialog, which) -> {
                    selectedCondition = values[which];
                    selectedConditionName = options[which];
                    View view = getView();
                    if (view != null) {
                        Chip chip = view.findViewById(R.id.chipFilterCondition);
                        if (chip != null) chip.setText(selectedConditionName);
                    }
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
                    View view = getView();
                    if (view != null) {
                        Chip chip = view.findViewById(R.id.chipFilterLocation);
                        if (chip != null) chip.setText(selectedLocationName);
                    }
                    refreshData();
                })
                .show();
    }
}
