package com.oasischeck.ui.planificadas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;
import com.oasischeck.data.model.PlannedVenta;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlannedVentaAdapter extends RecyclerView.Adapter<PlannedVentaAdapter.ViewHolder> {

    public interface OnPlannedVentaClickListener {
        void onClick(PlannedVenta p);
        void onMarcarVendida(PlannedVenta p);
        void onCancelar(PlannedVenta p);
        void onEditar(PlannedVenta p);
        void onEliminar(PlannedVenta p);
    }

    private List<PlannedVenta> items = new ArrayList<>();
    private final OnPlannedVentaClickListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d/M", Locale.getDefault());

    public PlannedVentaAdapter(OnPlannedVentaClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<PlannedVenta> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_planned_venta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlannedVenta p = items.get(position);

        holder.tvFecha.setText(dateFormat.format(new Date(p.fecha)));
        holder.tvCliente.setText(p.cliente);
        holder.tvInfo.setText(p.infoVenta != null ? p.infoVenta : "");
        holder.tvTotalEstimado.setText("Estimado: " + currencyFormat.format(p.totalEstimado));

        // Estado
        String estadoTexto;
        int estadoColor;
        switch (p.estado) {
            case PlannedVenta.ESTADO_VENDIDA:
                estadoTexto = "VENDIDA";
                estadoColor = holder.itemView.getContext().getColor(R.color.green_primary);
                break;
            case PlannedVenta.ESTADO_CANCELADA:
                estadoTexto = "CANCELADA";
                estadoColor = holder.itemView.getContext().getColor(R.color.red);
                break;
            default:
                estadoTexto = "PENDIENTE";
                estadoColor = holder.itemView.getContext().getColor(R.color.orange);
                break;
        }
        holder.tvEstado.setText(estadoTexto);
        holder.tvEstado.setBackgroundColor(estadoColor);

        // Botones según estado
        boolean esPendiente = p.estado == PlannedVenta.ESTADO_PENDIENTE;
        holder.btnMarcarVendida.setVisibility(esPendiente ? View.VISIBLE : View.GONE);
        holder.btnCancelar.setVisibility(esPendiente ? View.VISIBLE : View.GONE);

        holder.btnMarcarVendida.setOnClickListener(v -> listener.onMarcarVendida(p));
        holder.btnCancelar.setOnClickListener(v -> listener.onCancelar(p));

        holder.itemView.setOnClickListener(v -> listener.onClick(p));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onEditar(p);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvEstado, tvCliente, tvInfo, tvTotalEstimado;
        Button btnMarcarVendida, btnCancelar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvCliente = itemView.findViewById(R.id.tv_cliente);
            tvInfo = itemView.findViewById(R.id.tv_info);
            tvTotalEstimado = itemView.findViewById(R.id.tv_total_estimado);
            btnMarcarVendida = itemView.findViewById(R.id.btn_marcar_vendida);
            btnCancelar = itemView.findViewById(R.id.btn_cancelar);
        }
    }
}