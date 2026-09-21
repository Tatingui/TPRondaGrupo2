package com.example.tprondagrupo2.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.network.PublicationApiService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MyPublicationsFragment extends Fragment {

    @Inject
    PublicationApiService publicationApiService;

    private RecyclerView rvMyPublications;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MyPublicationsAdapter adapter;
    private List<Publicacion> myPublications = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_publications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvMyPublications = view.findViewById(R.id.rvMyPublications);
        progressBar = view.findViewById(R.id.progressBarMyPubs);
        tvEmpty = view.findViewById(R.id.tvEmptyMyPubs);

        setupRecyclerView();
        fetchMyPublications();
    }

    private void setupRecyclerView() {
        adapter = new MyPublicationsAdapter(myPublications, new MyPublicationsAdapter.OnPublicationActionListener() {
            @Override
            public void onPause(Publicacion pub, int position) {
                updateStatus(pub.getIdLong(), "PAUSED", position);
            }

            @Override
            public void onActivate(Publicacion pub, int position) {
                updateStatus(pub.getIdLong(), "ACTIVE", position);
            }

            @Override
            public void onSell(Publicacion pub, int position) {
                updateStatus(pub.getIdLong(), "SOLD", position);
            }

            @Override
            public void onDelete(Publicacion pub, int position) {
                confirmDelete(pub, position);
            }
        });
        rvMyPublications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyPublications.setAdapter(adapter);
    }

    private void fetchMyPublications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        publicationApiService.getMyPublications().enqueue(new Callback<List<Publicacion>>() {
            @Override
            public void onResponse(Call<List<Publicacion>> call, Response<List<Publicacion>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    myPublications.clear();
                    myPublications.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (myPublications.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar mis publicaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Publicacion>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(Long id, String state, int position) {
        if (id == null) return;
        publicationApiService.updatePublicationStatus(id, state).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myPublications.set(position, response.body());
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(),
                            "Estado actualizado a " + MyPublicationsAdapter.traducirEstado(state),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar estado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Publicacion> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(Publicacion pub, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Dar de baja publicación")
                .setMessage("¿Estás seguro de que querés dar de baja \""
                        + pub.getTitle()
                        + "\"? Se eliminarán la publicación, sus comentarios, ofertas y datos relacionados. Esta acción no se puede deshacer.")
                .setPositiveButton("Dar de baja", (dialog, which) -> deletePublication(pub, position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletePublication(Publicacion pub, int position) {
        Long id = pub.getIdLong();
        if (id == null) return;

        publicationApiService.deletePublication(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    myPublications.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, myPublications.size());
                    if (myPublications.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(getContext(), "Publicación dada de baja", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Error al dar de baja (código " + response.code() + ")";
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
