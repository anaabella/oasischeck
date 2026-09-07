package com.oasischeck.ui.wishlist;

import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oasischeck.R;
import com.oasischeck.data.model.WishPlanta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    public interface OnClick { void onClick(WishPlanta w); }
    public interface OnComprada { void onComprada(WishPlanta w); }

    private List<WishPlanta> items = new ArrayList<>();
    private final OnClick clickListener;
    private final OnComprada compradaListener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    public WishlistAdapter(OnClick click, OnComprada comprada) {
        this.clickListener = click;
        this.compradaListener = comprada;
    }

    public void setItems(List<WishPlanta> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wish, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        WishPlanta w = items.get(position);
        h.tvNombre.setText(w.nombre);

        if (w.fotoUri != null && !w.fotoUri.isEmpty()) {
            h.ivFotoThumb.setVisibility(View.VISIBLE);
            try {
                Uri uri = Uri.parse(w.fotoUri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.Source source = ImageDecoder.createSource(h.itemView.getContext().getContentResolver(), uri);
                    h.ivFotoThumb.setImageBitmap(ImageDecoder.decodeBitmap(source));
                } else {
                    h.ivFotoThumb.setImageBitmap(BitmapFactory.decodeStream(h.itemView.getContext().getContentResolver().openInputStream(uri)));
                }
            } catch (Exception e) {
                h.ivFotoThumb.setVisibility(View.GONE);
            }
        } else {
            h.ivFotoThumb.setVisibility(View.GONE);
        }

        if (w.comprada) {
            h.tvEstado.setText("COMPRADA");
            h.tvEstado.setBackgroundColor(0xFF4CAF50);
            h.tvNombre.setPaintFlags(h.tvNombre.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.btnComprada.setVisibility(View.GONE);
            if (w.precioComprada > 0) {
                h.tvPrecioVisto.setText("Comprada: " + currencyFormat.format(w.precioComprada));
            }
        } else {
            h.tvEstado.setText("PENDIENTE");
            h.tvEstado.setBackgroundColor(0xFFFF9800);
            h.tvNombre.setPaintFlags(h.tvNombre.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.btnComprada.setVisibility(View.VISIBLE);
        }

        h.tvLugarVisto.setText(w.lugarVisto != null && !w.lugarVisto.isEmpty() ? "Vista en: " + w.lugarVisto : "");
        if (!w.comprada && w.precioVisto > 0) {
            h.tvPrecioVisto.setText("Precio visto: " + currencyFormat.format(w.precioVisto));
        }

        if (w.notas != null && !w.notas.isEmpty()) {
            h.tvNotas.setVisibility(View.VISIBLE);
            h.tvNotas.setText(w.notas);
        } else {
            h.tvNotas.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> clickListener.onClick(w));
        h.btnComprada.setOnClickListener(v -> compradaListener.onComprada(w));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEstado, tvLugarVisto, tvPrecioVisto, tvNotas;
        Button btnComprada;
        ImageView ivFotoThumb;
        ViewHolder(@NonNull View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tv_nombre);
            tvEstado = v.findViewById(R.id.tv_estado);
            tvLugarVisto = v.findViewById(R.id.tv_lugar_visto);
            tvPrecioVisto = v.findViewById(R.id.tv_precio_visto);
            tvNotas = v.findViewById(R.id.tv_notas);
            btnComprada = v.findViewById(R.id.btn_comprada);
            ivFotoThumb = v.findViewById(R.id.iv_foto_thumb);
        }
    }
}