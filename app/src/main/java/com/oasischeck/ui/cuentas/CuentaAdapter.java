package com.oasischeck.ui.cuentas;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;
import com.oasischeck.data.model.Cuenta;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CuentaAdapter extends RecyclerView.Adapter<CuentaAdapter.ViewHolder> {

    public interface Listener {
        void onClick(Cuenta cuenta);
        void onLongClick(Cuenta cuenta);
    }

    private final Listener listener;
    private List<Cuenta> items = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    public CuentaAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Cuenta> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cuenta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Cuenta c = items.get(position);
        h.tvConcepto.setText(c.concepto);
        h.tvPersona.setText((c.persona != null && !c.persona.isEmpty() ? c.persona + " • " : "") + c.tipo);

        String vencimiento = "";
        if (c.vencimiento > 0) {
            vencimiento = " • Vence: " + dateFormat.format(new Date(c.vencimiento));
        }
        h.tvFechas.setText(dateFormat.format(new Date(c.fecha)) + vencimiento);

        boolean cobrar = Cuenta.TIPO_COBRAR.equals(c.tipo);
        h.tvMonto.setText((cobrar ? "+ " : "- ") + currencyFormat.format(c.monto));
        h.tvMonto.setTextColor(h.itemView.getContext().getColor(cobrar ? R.color.green_primary : R.color.red));

        if (c.pagada) {
            h.tvEstado.setText("PAGADA");
            h.tvEstado.setBackgroundColor(h.itemView.getContext().getColor(R.color.blue));
        } else if (c.estaVencida()) {
            h.tvEstado.setText("VENCIDA");
            h.tvEstado.setBackgroundColor(h.itemView.getContext().getColor(R.color.red));
        } else {
            h.tvEstado.setText("PENDIENTE");
            h.tvEstado.setBackgroundColor(h.itemView.getContext().getColor(R.color.orange));
        }

        h.itemView.setOnClickListener(v -> listener.onClick(c));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(c);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConcepto, tvEstado, tvPersona, tvFechas, tvMonto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConcepto = itemView.findViewById(R.id.tv_concepto);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvPersona = itemView.findViewById(R.id.tv_persona);
            tvFechas = itemView.findViewById(R.id.tv_fechas);
            tvMonto = itemView.findViewById(R.id.tv_monto);
        }
    }
}
