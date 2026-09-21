package com.example.tprondagrupo2.ui.historial;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.data.repository.HistorialRepository;
import com.example.tprondagrupo2.data.repository.RepoCallback;
import com.example.tprondagrupo2.model.Calificacion;
import com.example.tprondagrupo2.model.CalificacionRequest;
import com.example.tprondagrupo2.model.OperacionHistorial;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment para el historial de operaciones concretadas (compras y ventas),
 * con pestañas TabLayout para cambiar de tipo de operación, filtros estilo pill/chip por rango de fechas,
 * y sistema de calificaciones.
 */
@AndroidEntryPoint
public class HistorialFragment extends Fragment {

    @Inject
    HistorialRepository historialRepository;

    private TabLayout tabLayoutTipo;

    private Chip chipFechaDesde;
    private Chip chipFechaHasta;
    private Chip chipLimpiarFechas;

    private ProgressBar pbLoading;
    private TextView tvEmptyState;
    private RecyclerView rvHistorial;

    private HistorialAdapter adapter;

    // Estado de filtros
    private String selectedTipo = "COMPRA"; // "COMPRA" o "VENTA"
    private Calendar fromCalendar = null;
    private Calendar toCalendar = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        loadHistory();
    }

    /**
     * Inicializa las referencias a los componentes de la interfaz de usuario.
     *
     * @param view Vista raíz inflada del fragmento.
     */
    private void initViews(View view) {
        tabLayoutTipo = view.findViewById(R.id.tabLayoutTipo);

        chipFechaDesde = view.findViewById(R.id.chipFechaDesde);
        chipFechaHasta = view.findViewById(R.id.chipFechaHasta);
        chipLimpiarFechas = view.findViewById(R.id.chipLimpiarFechas);

        pbLoading = view.findViewById(R.id.pbLoading);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        rvHistorial = view.findViewById(R.id.rvHistorial);
    }

    /**
     * Configura el RecyclerView con su layout manager y el adaptador del historial.
     */
    private void setupRecyclerView() {
        adapter = new HistorialAdapter(
                this::showRatingDialog,
                this::confirmDelivery
        );
        rvHistorial.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistorial.setAdapter(adapter);
    }

    /**
     * Configura los escuchadores de eventos para la interacción con pestañas y filtros de fecha.
     */
    private void setupListeners() {
        tabLayoutTipo.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String newTipo = tab.getPosition() == 0 ? "COMPRA" : "VENTA";
                if (!newTipo.equals(selectedTipo)) {
                    selectedTipo = newTipo;
                    loadHistory();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        chipFechaDesde.setOnClickListener(v -> showDatePicker(true));
        chipFechaHasta.setOnClickListener(v -> showDatePicker(false));

        chipLimpiarFechas.setOnClickListener(v -> {
            fromCalendar = null;
            toCalendar = null;
            resetChipStyle(chipFechaDesde, "Desde");
            resetChipStyle(chipFechaHasta, "Hasta");
            chipLimpiarFechas.setVisibility(View.GONE);
            loadHistory();
        });
    }

    /**
     * Muestra el selector de fecha (DatePickerDialog) para establecer el límite inferior ("Desde")
     * o superior ("Hasta") del rango de fechas.
     *
     * @param isFromDate true si corresponde al filtro "Desde", false si corresponde a "Hasta".
     */
    private void showDatePicker(boolean isFromDate) {
        Calendar current = isFromDate ? fromCalendar : toCalendar;
        if (current == null) {
            current = Calendar.getInstance();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);

                    if (isFromDate) {
                        fromCalendar = selected;
                        String label = String.format(Locale.getDefault(), "Desde: %02d/%02d/%04d",
                                dayOfMonth, month + 1, year);
                        setChipSelectedStyle(chipFechaDesde, label);
                    } else {
                        toCalendar = selected;
                        String label = String.format(Locale.getDefault(), "Hasta: %02d/%02d/%04d",
                                dayOfMonth, month + 1, year);
                        setChipSelectedStyle(chipFechaHasta, label);
                    }

                    chipLimpiarFechas.setVisibility(View.VISIBLE);
                    loadHistory();
                },
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    /**
     * Aplica el estilo visual de Chip activo/seleccionado (fondo de color relleno con texto blanco).
     *
     * @param chip Componente Chip a modificar.
     * @param text Texto descriptivo que se mostrará en el Chip.
     */
    private void setChipSelectedStyle(Chip chip, String text) {
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.purple_500);
        chip.setTextColor(requireContext().getColor(R.color.white));
        chip.setChipIconTintResource(R.color.white);
        chip.setChipStrokeWidth(0f);
    }

    /**
     * Restablece el estilo visual predeterminado del Chip (fondo blanco con borde contorneado).
     *
     * @param chip Componente Chip a restablecer.
     * @param text Texto predeterminado del filtro.
     */
    private void resetChipStyle(Chip chip, String text) {
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.white);
        chip.setTextColor(requireContext().getColor(R.color.purple_500));
        chip.setChipIconTintResource(R.color.purple_500);
        chip.setChipStrokeColorResource(R.color.purple_500);
        chip.setChipStrokeWidth(1f);
    }

    /**
     * Consulta el historial de operaciones desde el repositorio aplicando los filtros
     * de tipo (COMPRA/VENTA) y rango de fechas seleccionados.
     */
    private void loadHistory() {
        pbLoading.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        rvHistorial.setVisibility(View.GONE);

        HistorialRepository.HistorialCallback callback = new HistorialRepository.HistorialCallback() {
            @Override
            public void onSuccess(List<OperacionHistorial> operaciones, int totalPages, boolean isLast) {
                if (!isAdded()) return;

                pbLoading.setVisibility(View.GONE);

                if (operaciones == null || operaciones.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvHistorial.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    rvHistorial.setVisibility(View.VISIBLE);
                    adapter.setOperaciones(operaciones);
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;

                pbLoading.setVisibility(View.GONE);
                tvEmptyState.setText("Error al cargar historial: " + message);
                tvEmptyState.setVisibility(View.VISIBLE);
                rvHistorial.setVisibility(View.GONE);
            }

            @Override
            public void onNetworkError() {
                if (!isAdded()) return;

                pbLoading.setVisibility(View.GONE);
                tvEmptyState.setText("Error de conexión al cargar historial");
                tvEmptyState.setVisibility(View.VISIBLE);
                rvHistorial.setVisibility(View.GONE);
            }
        };

        if (fromCalendar == null && toCalendar == null) {
            historialRepository.getHistory(selectedTipo, 0, 50, callback);
        } else {
            String fromStr;
            if (fromCalendar != null) {
                fromStr = String.format(Locale.US, "%04d-%02d-%02dT00:00:00",
                        fromCalendar.get(Calendar.YEAR),
                        fromCalendar.get(Calendar.MONTH) + 1,
                        fromCalendar.get(Calendar.DAY_OF_MONTH));
            } else {
                fromStr = "1970-01-01T00:00:00";
            }

            String toStr;
            if (toCalendar != null) {
                toStr = String.format(Locale.US, "%04d-%02d-%02dT23:59:59",
                        toCalendar.get(Calendar.YEAR),
                        toCalendar.get(Calendar.MONTH) + 1,
                        toCalendar.get(Calendar.DAY_OF_MONTH));
            } else {
                toStr = "2099-12-31T23:59:59";
            }

            historialRepository.getHistoryWithDates(selectedTipo, fromStr, toStr, 0, 50, callback);
        }
    }

    /**
     * Muestra el cuadro de diálogo modal para permitir al usuario calificar a la contraparte
     * con estrellas (1 a 5) y un comentario breve opcional.
     *
     * @param operacion Objeto OperacionHistorial a calificar.
     */
    private void showRatingDialog(OperacionHistorial operacion) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_calificar, null);

        TextView tvTituloDialog = dialogView.findViewById(R.id.tvTituloDialogCalificar);
        RatingBar rbEstrellas = dialogView.findViewById(R.id.rbEstrellas);
        TextInputEditText etComentario = dialogView.findViewById(R.id.etComentarioCalificacion);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarCalificar);
        MaterialButton btnEnviar = dialogView.findViewById(R.id.btnEnviarCalificacion);

        String contraparte = operacion.getNombreContraparte() != null ? operacion.getNombreContraparte() : "usuario";
        tvTituloDialog.setText("Calificar a " + contraparte);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnEnviar.setOnClickListener(v -> {
            int stars = (int) rbEstrellas.getRating();
            if (stars < 1) {
                Toast.makeText(requireContext(), "Por favor seleccioná al menos 1 estrella", Toast.LENGTH_SHORT).show();
                return;
            }

            String comment = etComentario.getText() != null ? etComentario.getText().toString().trim() : "";

            btnEnviar.setEnabled(false);

            CalificacionRequest request = new CalificacionRequest(stars, comment);
            historialRepository.rate(operacion.getId(), request, new HistorialRepository.CalificacionCallback() {
                @Override
                public void onSuccess(Calificacion calificacion) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "¡Calificación enviada con éxito!", Toast.LENGTH_SHORT).show();
                    operacion.setYaCalificado(true);
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }

                @Override
                public void onEmpty() {
                    if (!isAdded()) return;
                    dialog.dismiss();
                }

                @Override
                public void onError(String message) {
                    if (!isAdded()) return;
                    btnEnviar.setEnabled(true);
                    Toast.makeText(requireContext(), "Error al calificar: " + message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNetworkError() {
                    if (!isAdded()) return;
                    btnEnviar.setEnabled(true);
                    Toast.makeText(requireContext(), "Error de conexión al enviar calificación", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    /**
     * Solicita la confirmación del usuario para marcar la entrega de la operación como realizada.
     *
     * @param operacion Objeto OperacionHistorial cuya entrega se confirmará.
     */
    private void confirmDelivery(OperacionHistorial operacion) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar entrega")
                .setMessage("¿Confirmás que la entrega de esta operación fue realizada?")
                .setPositiveButton("Sí, confirmar", (dialog, which) -> {
                    historialRepository.confirmDelivery(operacion.getId(), new HistorialRepository.OperacionCallback() {
                        @Override
                        public void onSuccess(OperacionHistorial updatedOp) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Entrega confirmada con éxito", Toast.LENGTH_SHORT).show();
                            loadHistory();
                        }

                        @Override
                        public void onError(String message) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNetworkError() {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
