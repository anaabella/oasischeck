package com.oasischeck.ui.lista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;
import com.oasischeck.data.model.Venta;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VentaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER_DIA = 0;
    private static final int TYPE_HEADER_MONTO = 1;
    private static final int TYPE_VENTA = 2;

    public interface OnVentaClickListener {
        void onVentaClick(Venta venta);
    }

    private List<DisplayItem> items = new ArrayList<>();
    private final OnVentaClickListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    public VentaAdapter(OnVentaClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<DisplayItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER_DIA) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header_dia, parent, false);
            return new DiaViewHolder(view);
        } else if (viewType == TYPE_HEADER_MONTO) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_grupo_monto, parent, false);
            return new MontoViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_venta, parent, false);
            return new VentaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = items.get(position);
        int type = item.type;

        if (type == TYPE_HEADER_DIA) {
            DiaViewHolder h = (DiaViewHolder) holder;
            h.tvDia.setText(item.headerDia);
            h.tvCantidad.setText(item.headerCantidad + " venta(s)");
        } else if (type == TYPE_HEADER_MONTO) {
            MontoViewHolder h = (MontoViewHolder) holder;
            h.tvMonto.setText(item.headerMonto);
            h.tvCantidad.setText(item.headerCantidad + " venta(s)");
        } else {
            VentaViewHolder vh = (VentaViewHolder) holder;
            Venta venta = item.venta;
            vh.tvFecha.setText(dateFormat.format(new Date(venta.fecha)));
            vh.tvLugar.setText(venta.lugar);
            vh.tvCategoriaBadge.setText(venta.categoria);
            vh.tvCantidad.setText(venta.cantidad + " u.");
            vh.tvPrecio.setText(currencyFormat.format(venta.precioUnitario));
            vh.tvTotal.setText(currencyFormat.format(venta.total));

            if (venta.infoVenta != null && !venta.infoVenta.isEmpty()) {
                vh.tvInfo.setVisibility(View.VISIBLE);
                vh.tvInfo.setText(venta.infoVenta);
            } else {
                vh.tvInfo.setVisibility(View.GONE);
            }

            vh.itemView.setOnClickListener(v -> listener.onVentaClick(venta));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DiaViewHolder extends RecyclerView.ViewHolder {
        TextView tvDia, tvCantidad;

        DiaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDia = itemView.findViewById(R.id.tv_header_dia);
            tvCantidad = itemView.findViewById(R.id.tv_header_cantidad);
        }
    }

    static class MontoViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonto, tvCantidad;

        MontoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonto = itemView.findViewById(R.id.tv_grupo_monto);
            tvCantidad = itemView.findViewById(R.id.tv_grupo_cantidad);
        }
    }

    static class VentaViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvLugar, tvCategoriaBadge, tvCantidad, tvPrecio, tvTotal, tvInfo;

        VentaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvLugar = itemView.findViewById(R.id.tv_lugar);
            tvCategoriaBadge = itemView.findViewById(R.id.tv_categoria_badge);
            tvCantidad = itemView.findViewById(R.id.tv_cantidad);
            tvPrecio = itemView.findViewById(R.id.tv_precio);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvInfo = itemView.findViewById(R.id.tv_info);
        }
    }
}