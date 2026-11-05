package com.oasischeck.ui.clientes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClienteHistorialAdapter extends RecyclerView.Adapter<ClienteHistorialAdapter.ViewHolder> {

    public interface Listener {
        void onClick(ClienteResumen cliente);
    }

    private final List<ClienteResumen> items = new ArrayList<>();
    private final Listener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    public ClienteHistorialAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<ClienteResumen> nuevos) {
        items.clear();
        items.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial_cliente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClienteResumen c = items.get(position);
        holder.tvNombre.setText(c.nombre);

        StringBuilder detalle = new StringBuilder();
        if (c.origen != null && !c.origen.isEmpty()) detalle.append(c.origen);
        if (c.ventas > 0) {
            if (detalle.length() > 0) detalle.append(" · ");
            detalle.append(c.ventas).append(" venta(s)");
        }
        if (c.pedidos > 0) {
            if (detalle.length() > 0) detalle.append(" · ");
            detalle.append(c.pedidos).append(" pedido(s)");
        }
        if (c.planificadas > 0) {
            if (detalle.length() > 0) detalle.append(" · ");
            detalle.append(c.planificadas).append(" planificada(s)");
        }
        if (detalle.length() == 0) detalle.append("Sin movimientos");
        holder.tvDetalle.setText(detalle.toString());

        if (c.porCobrar > 0) {
            holder.tvMonto.setText("Debe " + currencyFormat.format(c.porCobrar));
            holder.tvMonto.setTextColor(holder.itemView.getContext().getColor(R.color.green_primary));
        } else if (c.porPagar > 0) {
            holder.tvMonto.setText("Le debés " + currencyFormat.format(c.porPagar));
            holder.tvMonto.setTextColor(holder.itemView.getContext().getColor(R.color.red));
        } else {
            holder.tvMonto.setText(currencyFormat.format(c.getTotalComprado()));
            holder.tvMonto.setTextColor(holder.itemView.getContext().getColor(R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDetalle, tvMonto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_cliente);
            tvDetalle = itemView.findViewById(R.id.tv_detalle_cliente);
            tvMonto = itemView.findViewById(R.id.tv_monto_cliente);
        }
    }
}
