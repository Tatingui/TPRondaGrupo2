package com.example.tprondagrupo2.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.model.Publication;
import com.example.tprondagrupo2.ui.detalle.DetallePublicacionFragment;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvPublications;
    private PublicationAdapter adapter;
    private List<Publication> allPublications;
    private List<Publication> displayedPublications;
    private EditText etSearch;

    // Filter states
    private String currentSearchText = "";
    private String selectedCategory = "Todas";
    private String selectedCondition = "Cualquiera";
    private String selectedLocation = "Todas";
    private Double minPrice = null;
    private Double maxPrice = null;
    private String currentSort = "Recientes";

    // Pagination states
    private int currentPage = 1;
    private final int PAGE_SIZE = 5;
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

        allPublications = getMockPublications();
        displayedPublications = new ArrayList<>();

        setupRecyclerView();
        setupSearchLogic();
        setupFilters(view);

        loadNextPage();
    }

    private void setupRecyclerView() {
        adapter = new PublicationAdapter(displayedPublications, this::abrirDetalle);
        rvPublications.setAdapter(adapter);

        rvPublications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadNextPage();
                    }
                }
            }
        });
    }

    private void abrirDetalle(Publication publication) {
        Bundle args = new Bundle();
        args.putSerializable(DetallePublicacionFragment.ARG_PUBLICACION, mapearADetalle(publication));

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_detalle, args);
    }

    private Publicacion mapearADetalle(Publication publication) {
        List<String> fotos = Arrays.asList("Foto 1", "Foto 2", "Foto 3");

        String descripcionCompleta = obtenerDescripcionCompleta(publication.getId(),
                publication.getDescription());

        String fecha = obtenerFechaMock(publication.getId());

        return new Publicacion(
                publication.getId(),
                publication.getTitle(),
                fotos,
                descripcionCompleta,
                publication.getCategory(),
                publication.getCondition(),
                publication.getPrice(),
                fecha);
    }

    private String obtenerDescripcionCompleta(String id, String descCorta) {
        switch (id) {
            case "1":
                return "Bicicleta de montaña rodado 29, cuadro de aluminio, 21 velocidades. "
                        + "Muy poco uso, siempre guardada en lugar techado. Frenos a disco "
                        + "delanteros y traseros recién calibrados. Incluye luces y timbre.";
            case "2":
                return "Sillón de 3 cuerpos tapizado en tela gris. Muy cómodo y amplio, "
                        + "ideal para living. Patas de madera. Se retira por Almagro. "
                        + "Acepto oferta razonable.";
            case "3":
                return "iPhone 13 de 128GB, color azul medianoche. En caja original con "
                        + "cargador, cable y auriculares. Batería al 94%. Liberado de fábrica, "
                        + "funciona con cualquier operador.";
            case "4":
                return "Mesa de luz de pino macizo barnizado. Medidas: 45x35x50cm. "
                        + "Tiene un cajón amplio con tirador metálico. Ideal para dormitorio.";
            case "5":
                return "Zapatillas de running talle 42, color azul con detalles blancos. "
                        + "Sin uso, nuevas en caja. Suela con amortiguación para asfalto.";
            default:
                return descCorta;
        }
    }

    private String obtenerFechaMock(String id) {
        switch (id) {
            case "1": return "15/08/2026";
            case "2": return "10/08/2026";
            case "3": return "18/08/2026";
            case "4": return "05/08/2026";
            case "5": return "20/08/2026";
            default: return "";
        }
    }

    private void loadNextPage() {
        isLoading = true;
        rvPublications.postDelayed(() -> {
            List<Publication> filtered = getFilteredList();
            int start = (currentPage - 1) * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, filtered.size());

            if (start < filtered.size()) {
                List<Publication> newItems = filtered.subList(start, end);
                displayedPublications.addAll(newItems);
                adapter.notifyDataSetChanged();
                currentPage++;
            }

            if (end >= filtered.size()) {
                isLastPage = true;
            }
            isLoading = false;
        }, 500);
    }

    private void setupSearchLogic() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString();
                resetAndApplyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void resetAndApplyFilters() {
        currentPage = 1;
        isLastPage = false;
        displayedPublications.clear();
        adapter.notifyDataSetChanged();
        loadNextPage();
    }

    private List<Publication> getFilteredList() {
        List<Publication> filtered = new ArrayList<>();
        for (Publication pub : allPublications) {
            boolean matchesSearch = pub.getTitle().toLowerCase().contains(currentSearchText.toLowerCase()) ||
                    pub.getDescription().toLowerCase().contains(currentSearchText.toLowerCase());
            boolean matchesCategory = selectedCategory.equals("Todas") || pub.getCategory().equals(selectedCategory);
            boolean matchesCondition = selectedCondition.equals("Cualquiera") || pub.getCondition().equalsIgnoreCase(selectedCondition);
            boolean matchesLocation = selectedLocation.equals("Todas") || pub.getLocation().equalsIgnoreCase(selectedLocation);
            boolean matchesPrice = (minPrice == null || pub.getPrice() >= minPrice) &&
                                   (maxPrice == null || pub.getPrice() <= maxPrice);

            if (matchesSearch && matchesCategory && matchesCondition && matchesLocation && matchesPrice) {
                filtered.add(pub);
            }
        }

        if (currentSort.equals("Menor precio")) {
            Collections.sort(filtered, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        } else if (currentSort.equals("Mayor precio")) {
            Collections.sort(filtered, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        } else {
            Collections.sort(filtered, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
        }

        return filtered;
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
        new AlertDialog.Builder(requireContext())
                .setTitle("Ordenar por")
                .setItems(options, (dialog, which) -> {
                    currentSort = options[which];
                    ((Chip) getView().findViewById(R.id.chipSort)).setText("Orden: " + currentSort);
                    resetAndApplyFilters();
                })
                .show();
    }

    private void showCategoryDialog() {
        String[] categories = {"Todas", "Deportes", "Hogar", "Electrónica", "Ropa", "Otros"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar Categoría")
                .setItems(categories, (dialog, which) -> {
                    selectedCategory = categories[which];
                    ((Chip) getView().findViewById(R.id.chipFilterCategory)).setText(selectedCategory);
                    resetAndApplyFilters();
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
                    resetAndApplyFilters();
                })
                .setNegativeButton("Limpiar", (dialog, which) -> {
                    minPrice = null;
                    maxPrice = null;
                    resetAndApplyFilters();
                })
                .show();
    }

    private void showConditionDialog() {
        String[] conditions = {"Cualquiera", "Nuevo", "Como nuevo", "Usado"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Estado del Artículo")
                .setItems(conditions, (dialog, which) -> {
                    selectedCondition = conditions[which];
                    ((Chip) getView().findViewById(R.id.chipFilterCondition)).setText(selectedCondition);
                    resetAndApplyFilters();
                })
                .show();
    }

    private void showLocationDialog() {
        String[] locations = {"Todas", "Palermo", "Almagro", "Belgrano", "Caballito", "Villa Urquiza"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Zona del Vendedor")
                .setItems(locations, (dialog, which) -> {
                    selectedLocation = locations[which];
                    ((Chip) getView().findViewById(R.id.chipFilterLocation)).setText(selectedLocation);
                    resetAndApplyFilters();
                })
                .show();
    }

    private List<Publication> getMockPublications() {
        List<Publication> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        list.add(new Publication("1", "Bicicleta de montaña", "Excelente estado, poco uso.", 150000.0, "Como nuevo", "Palermo", "Deportes", "", now - 100000));
        list.add(new Publication("2", "Sillón 3 cuerpos", "Cómodo y amplio.", 85000.0, "Usado", "Almagro", "Hogar", "", now - 200000));
        list.add(new Publication("3", "iPhone 13 128GB", "En caja con accesorios.", 950000.0, "Nuevo", "Belgrano", "Electrónica", "", now - 300000));
        list.add(new Publication("4", "Mesa de luz", "Madera pino barnizada.", 25000.0, "Usado", "Caballito", "Hogar", "", now - 400000));
        list.add(new Publication("5", "Zapatillas running", "Talle 42, color azul.", 45000.0, "Nuevo", "Villa Urquiza", "Ropa", "", now - 500000));
        list.add(new Publication("6", "Raqueta de Tenis", "Wilson Pro Staff.", 120000.0, "Nuevo", "Palermo", "Deportes", "", now - 600000));
        list.add(new Publication("7", "Cafetera Nespresso", "Casi nueva.", 60000.0, "Como nuevo", "Almagro", "Hogar", "", now - 700000));
        list.add(new Publication("8", "MacBook Air M1", "8GB RAM, 256GB SSD.", 1200000.0, "Usado", "Belgrano", "Electrónica", "", now - 800000));
        list.add(new Publication("9", "Monitor 24 pulgadas", "Samsung IPS.", 180000.0, "Nuevo", "Caballito", "Electrónica", "", now - 900000));
        list.add(new Publication("10", "Escritorio oficina", "Metal y madera.", 95000.0, "Usado", "Villa Urquiza", "Hogar", "", now - 1000000));
        list.add(new Publication("11", "Pelota de Fútbol", "Adidas Al Rihla.", 35000.0, "Nuevo", "Palermo", "Deportes", "", now - 1100000));
        list.add(new Publication("12", "Lámpara de pie", "Diseño moderno.", 40000.0, "Usado", "Almagro", "Hogar", "", now - 1200000));
        return list;
    }
}
