package com.example.tprondagrupo2.ui.historial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tprondagrupo2.R;
import com.example.tprondagrupo2.model.OperacionHistorial;
import com.example.tprondagrupo2.util.FormatUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

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
            holder.tvTipoOperacion.setBackgroundColor(ContextCompat.getColor(context, R.color.historial_compra_bg));
            holder.tvTipoOperacion.setTextColor(ContextCompat.getColor(context, R.color.historial_compra_text));
            holder.tvOtroUsuario.setText(context.getString(R.string.historial_vendedor, contraparte));
        } else {
            holder.tvTipoOperacion.setText(context.getString(R.string.historial_venta));
            holder.tvTipoOperacion.setBackgroundColor(ContextCompat.getColor(context, R.color.historial_venta_bg));
            holder.tvTipoOperacion.setTextColor(ContextCompat.getColor(context, R.color.historial_venta_text));
            holder.tvOtroUsuario.setText(context.getString(R.string.historial_comprador, contraparte));
        }

        // 2. Fecha de transacción
        holder.tvFecha.setText(FormatUtils.formatFechaDisplay(operacion.getFecha()));

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
            String fechaEntregaFormat = FormatUtils.formatFechaDisplay(operacion.getFechaEntrega());
            holder.tvEstadoEntrega.setText(context.getString(R.string.historial_entrega_confirmada, fechaEntregaFormat));
            holder.tvEstadoEntrega.setTextColor(ContextCompat.getColor(context, R.color.historial_entrega_confirmada));
            holder.btnConfirmarEntrega.setVisibility(View.GONE);
        } else {
            holder.tvEstadoEntrega.setText(context.getString(R.string.historial_entrega_pendiente));
            holder.tvEstadoEntrega.setTextColor(ContextCompat.getColor(context, R.color.historial_entrega_pendiente));
            if (confirmDeliveryClickListener != null) {
                holder.btnConfirmarEntrega.setVisibility(View.VISIBLE);
                holder.btnConfirmarEntrega.setOnClickListener(v -> confirmDeliveryClickListener.onConfirmDeliveryClick(operacion));
            } else {
                holder.btnConfirmarEntrega.setVisibility(View.GONE);
            }
        }

        // 7. Botón para Calificar
        boolean enVentana7Dias = operacion.entregaConfirmada()
                && FormatUtils.isDentroDe7Dias(operacion.getFechaEntrega());
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
