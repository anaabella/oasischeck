package com.oasischeck.ui.reportes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;
import com.oasischeck.data.db.VentaDao;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TopPlantasAdapter extends RecyclerView.Adapter<TopPlantasAdapter.ViewHolder> {

    private List<VentaDao.PlantaStats> items = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    public TopPlantasAdapter(List<VentaDao.PlantaStats> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top_planta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        VentaDao.PlantaStats s = items.get(position);
        h.tvRank.setText("#" + (position + 1));
        h.tvNombre.setText(s.nombrePlanta);
        h.tvStats.setText(s.cant + " u. • " + currencyFormat.format(s.tot));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvNombre, tvStats;
        ViewHolder(@NonNull View v) {
            super(v);
            tvRank = v.findViewById(R.id.tv_rank);
            tvNombre = v.findViewById(R.id.tv_nombre_planta);
            tvStats = v.findViewById(R.id.tv_stats);
        }
    }
}