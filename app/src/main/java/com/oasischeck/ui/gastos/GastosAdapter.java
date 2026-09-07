package com.oasischeck.ui.gastos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;
import com.oasischeck.data.model.Gasto;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GastosAdapter extends RecyclerView.Adapter<GastosAdapter.ViewHolder> {

    public interface OnClick { void onClick(Gasto g); }
    public interface OnLongClick { void onLongClick(Gasto g); }

    private List<Gasto> items = new ArrayList<>();
    private final OnClick clickListener;
    private final OnLongClick longClickListener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M", Locale.getDefault());

    public GastosAdapter(OnClick click, OnLongClick longClick) {
        this.clickListener = click;
        this.longClickListener = longClick;
    }

    public void setItems(List<Gasto> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gasto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Gasto g = items.get(position);
        h.tvConcepto.setText(g.concepto);
        h.tvTipo.setText(g.tipo + (g.recurrente ? " • Recurrente" : "") + " • " + dateFormat.format(new Date(g.fecha)));
        h.tvMonto.setText("- " + currencyFormat.format(g.monto));

        h.itemView.setOnClickListener(v -> clickListener.onClick(g));
        h.itemView.setOnLongClickListener(v -> { longClickListener.onLongClick(g); return true; });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConcepto, tvTipo, tvMonto;
        ViewHolder(@NonNull View v) {
            super(v);
            tvConcepto = v.findViewById(R.id.tv_concepto);
            tvTipo = v.findViewById(R.id.tv_tipo);
            tvMonto = v.findViewById(R.id.tv_monto);
        }
    }
}