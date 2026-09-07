package com.oasischeck.ui.pedidos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;
import com.oasischeck.data.model.Pedido;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.ViewHolder> {

    public interface OnPedidoClickListener {
        void onClick(Pedido p);
        void onPreparado(Pedido p);
        void onEntregado(Pedido p);
        void onEliminar(Pedido p);
    }

    private List<Pedido> items = new ArrayList<>();
    private final OnPedidoClickListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    public PedidoAdapter(OnPedidoClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Pedido> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido p = items.get(position);

        holder.tvFecha.setText(dateFormat.format(new Date(p.fecha)));
        holder.tvCliente.setText(p.cliente);
        holder.tvProductos.setText(p.productos != null ? p.productos : "");
        holder.tvTotal.setText(currencyFormat.format(p.total));

        String estadoTexto;
        int estadoColor;
        switch (p.estado) {
            case Pedido.ESTADO_PREPARADO:
                estadoTexto = "PREPARADO";
                estadoColor = holder.itemView.getContext().getColor(R.color.blue);
                break;
            case Pedido.ESTADO_ENTREGADO:
                estadoTexto = "ENTREGADO";
                estadoColor = holder.itemView.getContext().getColor(R.color.green_primary);
                break;
            default:
                estadoTexto = "PENDIENTE";
                estadoColor = holder.itemView.getContext().getColor(R.color.orange);
                break;
        }
        holder.tvEstado.setText(estadoTexto);
        holder.tvEstado.setBackgroundColor(estadoColor);

        boolean esPendiente = p.estado == Pedido.ESTADO_PENDIENTE;
        boolean esPreparado = p.estado == Pedido.ESTADO_PREPARADO;
        holder.btnPreparado.setVisibility(esPendiente ? View.VISIBLE : View.GONE);
        holder.btnEntregado.setVisibility(esPreparado ? View.VISIBLE : View.GONE);

        holder.btnPreparado.setOnClickListener(v -> listener.onPreparado(p));
        holder.btnEntregado.setOnClickListener(v -> listener.onEntregado(p));

        holder.itemView.setOnClickListener(v -> listener.onClick(p));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onEliminar(p);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvEstado, tvCliente, tvProductos, tvTotal;
        Button btnPreparado, btnEntregado;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvCliente = itemView.findViewById(R.id.tv_cliente);
            tvProductos = itemView.findViewById(R.id.tv_productos);
            tvTotal = itemView.findViewById(R.id.tv_total);
            btnPreparado = itemView.findViewById(R.id.btn_preparado);
            btnEntregado = itemView.findViewById(R.id.btn_entregado);
        }
    }
}