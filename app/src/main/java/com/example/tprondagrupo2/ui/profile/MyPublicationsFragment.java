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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;
import com.example.tprondagrupo2.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPublicationsFragment extends Fragment {

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
        });
        rvMyPublications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyPublications.setAdapter(adapter);
    }

    private void fetchMyPublications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.getPublicationService().getMyPublications().enqueue(new Callback<List<Publicacion>>() {
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
        ApiClient.getPublicationService().updatePublicationStatus(id, state).enqueue(new Callback<Publicacion>() {
            @Override
            public void onResponse(Call<Publicacion> call, Response<Publicacion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myPublications.set(position, response.body());
                    adapter.notifyItemChanged(position);
                    Toast.makeText(getContext(), "Estado actualizado a " + state, Toast.LENGTH_SHORT).show();
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
}
