package com.example.tprondagrupo2.ui.historial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tprondagrupo2.R;

/**
 * Fragment placeholder para el historial de operaciones (punto 9).
 * La UI completa (RecyclerView, tabs compra/venta, filtros, calificación)
 * será implementada por el compañero en los pasos 4 y 5.
 */
public class HistorialFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }
}
