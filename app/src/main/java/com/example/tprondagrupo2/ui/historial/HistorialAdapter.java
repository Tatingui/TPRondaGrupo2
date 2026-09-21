package com.example.tprondagrupo2.ui.historial;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.OperacionHistorial;
import com.example.tprondagrupo2.util.FormatUtils;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador de RecyclerView para renderizar cada tarjeta de transacción del historial de operaciones.
 */
public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    /** Escuchador para la acción de calificar una operación. */
    public interface OnRateClickListener {
        void onRateClick(OperacionHistorial operacion);
    }

    /** Escuchador para la acción de confirmar la entrega de una operación. */
    public interface OnConfirmDeliveryClickListener {
        void onConfirmDeliveryClick(OperacionHistorial operacion);
    }

    private final List<OperacionHistorial> operaciones = new ArrayList<>();
    private final OnRateClickListener rateClickListener;
    private final OnConfirmDeliveryClickListener confirmDeliveryClickListener;

    public HistorialAdapter(OnRateClickListener rateClickListener,
                            OnConfirmDeliveryClickListener confirmDeliveryClickListener) {
        this.rateClickListener = rateClickListener;
        this.confirmDeliveryClickListener = confirmDeliveryClickListener;
    }

    /**
     * Reemplaza los elementos actuales de la lista por nuevas operaciones y refresca la vista.
     *
     * @param nuevasOperaciones Lista actualizada de objetos OperacionHistorial.
     */
    public void setOperaciones(List<OperacionHistorial> nuevasOperaciones) {
        this.operaciones.clear();
        if (nuevasOperaciones != null) {
            this.operaciones.addAll(nuevasOperaciones);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_operacion_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OperacionHistorial operacion = operaciones.get(position);
        Context context = holder.itemView.getContext();

        String contraparte = operacion.getNombreContraparte() != null ? operacion.getNombreContraparte() : "-";

        // 1. Tipo de operación
        if (operacion.isCompra()) {
            holder.tvTipoOperacion.setText(context.getString(R.string.historial_compra));
            holder.tvTipoOperacion.setBackgroundColor(Color.parseColor("#E8F5E9"));
            holder.tvTipoOperacion.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvOtroUsuario.setText(context.getString(R.string.historial_vendedor, contraparte));
        } else {
            holder.tvTipoOperacion.setText(context.getString(R.string.historial_venta));
            holder.tvTipoOperacion.setBackgroundColor(Color.parseColor("#F3E5F5"));
            holder.tvTipoOperacion.setTextColor(Color.parseColor("#6A1B9A"));
            holder.tvOtroUsuario.setText(context.getString(R.string.historial_comprador, contraparte));
        }

        // 2. Fecha de transacción
        holder.tvFecha.setText(formatFechaDisplay(operacion.getFecha()));

        // 3. Título del artículo
        holder.tvTituloArticulo.setText(operacion.getTituloPublicacion() != null
                ? operacion.getTituloPublicacion()
                : context.getString(R.string.historial_sin_titulo));

        // 4. Monto final
        holder.tvMontoFinal.setText(FormatUtils.formatPrice(operacion.getMontoFinal()));

        // 5. Imagen del artículo
        Glide.with(context)
                .load(operacion.getImagenUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.ivImagenArticulo);

        // 6. Estado de la entrega
        if (operacion.entregaConfirmada()) {
            String fechaEntregaFormat = formatFechaDisplay(operacion.getFechaEntrega());
            holder.tvEstadoEntrega.setText(context.getString(R.string.historial_entrega_confirmada, fechaEntregaFormat));
            holder.tvEstadoEntrega.setTextColor(Color.parseColor("#1B5E20"));
            holder.btnConfirmarEntrega.setVisibility(View.GONE);
        } else {
            holder.tvEstadoEntrega.setText(context.getString(R.string.historial_entrega_pendiente));
            holder.tvEstadoEntrega.setTextColor(Color.parseColor("#E65100"));
            if (confirmDeliveryClickListener != null) {
                holder.btnConfirmarEntrega.setVisibility(View.VISIBLE);
                holder.btnConfirmarEntrega.setOnClickListener(v -> confirmDeliveryClickListener.onConfirmDeliveryClick(operacion));
            } else {
                holder.btnConfirmarEntrega.setVisibility(View.GONE);
            }
        }

        // 7. Botón para Calificar
        boolean enVentana7Dias = isDentroDe7DiasEntrega(operacion);
        boolean puedeCalificar = operacion.entregaConfirmada() && (enVentana7Dias || operacion.isPuedeCalificar());

        if (operacion.isYaCalificado()) {
            holder.btnCalificar.setVisibility(View.VISIBLE);
            holder.btnCalificar.setEnabled(false);
            holder.btnCalificar.setText(context.getString(R.string.historial_calificado));
            holder.btnCalificar.setOnClickListener(null);
        } else if (puedeCalificar) {
            holder.btnCalificar.setVisibility(View.VISIBLE);
            holder.btnCalificar.setEnabled(true);
            holder.btnCalificar.setText(context.getString(R.string.historial_calificar));
            holder.btnCalificar.setOnClickListener(v -> {
                if (rateClickListener != null) {
                    rateClickListener.onRateClick(operacion);
                }
            });
        } else {
            holder.btnCalificar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return operaciones.size();
    }

    /**
     * Verifica si la fecha de entrega de la operación se produjo dentro de los 7 días anteriores
     * respecto al momento actual.
     *
     * @param operacion Transacción a evaluar.
     * @return true si la entrega fue confirmada y está dentro de la ventana de 7 días, false en caso contrario.
     */
    private boolean isDentroDe7DiasEntrega(OperacionHistorial operacion) {
        if (!operacion.entregaConfirmada()) {
            return false;
        }
        String fechaEntregaStr = operacion.getFechaEntrega();
        if (fechaEntregaStr == null || fechaEntregaStr.trim().isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf;
            if (fechaEntregaStr.contains("T")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }
            Date deliveryDate = sdf.parse(fechaEntregaStr);
            if (deliveryDate != null) {
                long diffMs = System.currentTimeMillis() - deliveryDate.getTime();
                long maxMs = 7L * 24 * 60 * 60 * 1000L;
                return diffMs >= 0 && diffMs <= maxMs;
            }
        } catch (Exception ignored) { }
        return operacion.isPuedeCalificar();
    }

    /**
     * Convierte una cadena de texto en formato ISO a una representación de fecha/hora legible
     * en formato dd/MM/yyyy HH:mm.
     *
     * @param isoDate Cadena de texto que contiene la fecha en ISO (ej. "2025-05-10T14:30:00").
     * @return Cadena formateada o la cadena original si ocurrió un error en el procesamiento.
     */
    private String formatFechaDisplay(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return "-";
        }
        try {
            SimpleDateFormat inFormat;
            if (isoDate.contains("T")) {
                inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            } else {
                inFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }
            Date date = inFormat.parse(isoDate);
            if (date != null) {
                SimpleDateFormat outFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                return outFormat.format(date);
            }
        } catch (Exception ignored) { }
        return isoDate;
    }

    /**
     * ViewHolder que mantiene las referencias de las vistas internas de cada tarjeta de transacción.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTipoOperacion;
        final TextView tvFecha;
        final TextView tvTituloArticulo;
        final TextView tvMontoFinal;
        final TextView tvOtroUsuario;
        final TextView tvEstadoEntrega;
        final ImageView ivImagenArticulo;
        final MaterialButton btnCalificar;
        final MaterialButton btnConfirmarEntrega;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipoOperacion = itemView.findViewById(R.id.tvTipoOperacion);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvTituloArticulo = itemView.findViewById(R.id.tvTituloArticulo);
            tvMontoFinal = itemView.findViewById(R.id.tvMontoFinal);
            tvOtroUsuario = itemView.findViewById(R.id.tvOtroUsuario);
            tvEstadoEntrega = itemView.findViewById(R.id.tvEstadoEntrega);
            ivImagenArticulo = itemView.findViewById(R.id.ivImagenArticulo);
            btnCalificar = itemView.findViewById(R.id.btnCalificar);
            btnConfirmarEntrega = itemView.findViewById(R.id.btnConfirmarEntrega);
        }
    }
}
