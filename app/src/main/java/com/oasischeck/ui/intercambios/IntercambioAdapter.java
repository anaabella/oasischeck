package com.oasischeck.ui.intercambios;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;
import com.oasischeck.data.model.Intercambio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IntercambioAdapter extends RecyclerView.Adapter<IntercambioAdapter.IntercambioViewHolder> {

    public interface OnIntercambioClickListener {
        void onIntercambioClick(Intercambio intercambio);
    }

    private List<Intercambio> items = new ArrayList<>();
    private final OnIntercambioClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    public IntercambioAdapter(OnIntercambioClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Intercambio> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IntercambioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_intercambio, parent, false);
        return new IntercambioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IntercambioViewHolder holder, int position) {
        Intercambio intercambio = items.get(position);
        holder.tvFecha.setText(dateFormat.format(new Date(intercambio.fecha)));
        holder.tvPersona.setText(intercambio.persona);
        holder.tvPlantaEntregada.setText(intercambio.plantaEntregada);
        holder.tvPlantaRecibida.setText(intercambio.plantaRecibida);

        if (intercambio.notas != null && !intercambio.notas.isEmpty()) {
            holder.tvNotas.setVisibility(View.VISIBLE);
            holder.tvNotas.setText(intercambio.notas);
        } else {
            holder.tvNotas.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onIntercambioClick(intercambio));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class IntercambioViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvPersona, tvPlantaEntregada, tvPlantaRecibida, tvNotas;

        IntercambioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvPersona = itemView.findViewById(R.id.tv_persona);
            tvPlantaEntregada = itemView.findViewById(R.id.tv_planta_entregada);
            tvPlantaRecibida = itemView.findViewById(R.id.tv_planta_recibida);
            tvNotas = itemView.findViewById(R.id.tv_notas);
        }
    }
}
