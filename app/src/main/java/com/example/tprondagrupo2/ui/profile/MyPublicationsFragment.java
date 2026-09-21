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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.Publicacion;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyPublicationsFragment extends Fragment {

    private MyPublicationsViewModel viewModel;

    private RecyclerView rvMyPublications;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private MyPublicationsAdapter adapter;
    private final List<Publicacion> myPublications = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_publications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MyPublicationsViewModel.class);

        rvMyPublications = view.findViewById(R.id.rvMyPublications);
        progressBar = view.findViewById(R.id.progressBarMyPubs);
        tvEmpty = view.findViewById(R.id.tvEmptyMyPubs);

        setupRecyclerView();
        setupViewModelObservers();

        viewModel.fetchMyPublications();
    }

    private void setupViewModelObservers() {
        viewModel.getMyPublications().observe(getViewLifecycleOwner(), publications -> {
            myPublications.clear();
            if (publications != null) {
                myPublications.addAll(publications);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getEmptyVisible().observe(getViewLifecycleOwner(), isEmpty -> {
            if (tvEmpty != null) {
                tvEmpty.setVisibility(Boolean.TRUE.equals(isEmpty) ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && isAdded()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new MyPublicationsAdapter(myPublications, new MyPublicationsAdapter.OnPublicationActionListener() {
            @Override
            public void onPause(Publicacion pub, int position) {
                viewModel.updateStatus(pub.getIdLong(), "PAUSED");
            }

            @Override
            public void onActivate(Publicacion pub, int position) {
                viewModel.updateStatus(pub.getIdLong(), "ACTIVE");
            }

            @Override
            public void onDelete(Publicacion pub, int position) {
                confirmDelete(pub);
            }
        });
        rvMyPublications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyPublications.setAdapter(adapter);
    }

    private void confirmDelete(Publicacion pub) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Dar de baja publicación")
                .setMessage("¿Estás seguro de que querés dar de baja \""
                        + pub.getTitle()
                        + "\"? Se eliminarán la publicación, sus comentarios, ofertas y datos relacionados. Esta acción no se puede deshacer.")
                .setPositiveButton("Dar de baja", (dialog, which) -> viewModel.deletePublication(pub))
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
