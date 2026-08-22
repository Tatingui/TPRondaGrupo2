package com.example.tprondagrupo2.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publication;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvPublications;
    private PublicationAdapter adapter;
    private List<Publication> allPublications;
    private EditText etSearch;

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
        
        setupRecyclerView();
        setupSearchLogic();
        setupFilters(view);
    }

    private void setupRecyclerView() {
        adapter = new PublicationAdapter(new ArrayList<>(allPublications), publication -> {
            Toast.makeText(getContext(), "Click en: " + publication.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Navegar al detalle
        });
        rvPublications.setAdapter(adapter);
    }

    private void setupSearchLogic() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterByText(String text) {
        List<Publication> filteredList = new ArrayList<>();
        for (Publication pub : allPublications) {
            if (pub.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                pub.getDescription().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(pub);
            }
        }
        adapter.updateList(filteredList);
    }

    private void setupFilters(View view) {
        // TODO: Implementar diálogos o menús para los filtros (Categoría, Precio, Estado)
        // TODO: Implementar la lógica de ordenamiento (chipSort)
        
        view.findViewById(R.id.chipSort).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Funcionalidad de ordenamiento próximamente", Toast.LENGTH_SHORT).show()
        );
        
        view.findViewById(R.id.chipFilterCategory).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Filtros por categoría próximamente", Toast.LENGTH_SHORT).show()
        );
        
        view.findViewById(R.id.chipFilterPrice).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Filtros por precio próximamente", Toast.LENGTH_SHORT).show()
        );
    }

    private List<Publication> getMockPublications() {
        List<Publication> list = new ArrayList<>();
        list.add(new Publication("1", "Bicicleta de montaña", "Excelente estado, poco uso.", 150000.0, "Como nuevo", "Palermo", "Deportes", ""));
        list.add(new Publication("2", "Sillón 3 cuerpos", "Cómodo y amplio.", 85000.0, "Usado", "Almagro", "Hogar", ""));
        list.add(new Publication("3", "iPhone 13 128GB", "En caja con accesorios.", 950000.0, "Nuevo", "Belgrano", "Electrónica", ""));
        list.add(new Publication("4", "Mesa de luz", "Madera pino barnizada.", 25000.0, "Usado", "Caballito", "Hogar", ""));
        list.add(new Publication("5", "Zapatillas running", "Talle 42, color azul.", 45000.0, "Nuevo", "Villa Urquiza", "Ropa", ""));
        return list;
    }
}
