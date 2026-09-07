package com.oasischeck.ui.gastos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.oasischeck.R;
import com.oasischeck.data.model.Gasto;
import com.oasischeck.data.repository.GastoRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GastosFragment extends Fragment {

    private GastoRepository repository;
    private GastosAdapter adapter;
    private TextView tvTotalGastos, tvSinDatos;
    private ProgressBar progressBar;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private int filtroTipo = -1;
    private List<Gasto> todos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gastos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new GastoRepository(requireActivity().getApplication());

        RecyclerView recycler = view.findViewById(R.id.recycler_gastos);
        tvTotalGastos = view.findViewById(R.id.tv_total_gastos);
        tvSinDatos = view.findViewById(R.id.tv_sin_datos);
        progressBar = view.findViewById(R.id.progress_bar);
        MaterialButton btnFiltrar = view.findViewById(R.id.btn_filtrar_tipo);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GastosAdapter(
                g -> {
                    Intent i = new Intent(getContext(), AgregarGastoActivity.class);
                    i.putExtra("gasto_id", g.id);
                    startActivity(i);
                },
                g -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Eliminar gasto")
                            .setMessage("¿Eliminar " + g.concepto + "?")
                            .setPositiveButton("Eliminar", (d, w) -> {
                                repository.delete(g, () -> {
                                    if (getActivity() != null) getActivity().runOnUiThread(this::cargar);
                                });
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
        );
        recycler.setAdapter(adapter);

        String[] tipos = {"Todos", "Viaje", "Fijo", "Insumo", "Otro"};
        btnFiltrar.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Filtrar por tipo")
                    .setItems(tipos, (d, which) -> {
                        filtroTipo = which - 1;
                        btnFiltrar.setText(tipos[which]);
                        aplicarFiltro();
                    }).show();
        });

        cargar();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll(lista -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    todos = lista;
                    aplicarFiltro();
                });
            }
        });
    }

    private void aplicarFiltro() {
        List<Gasto> filtrados = new ArrayList<>();
        double total = 0;
        for (Gasto g : todos) {
            boolean pasa = filtroTipo == -1 || g.tipo.equals(Gasto.TIPO_VIAJE) && filtroTipo == 0 ||
                    g.tipo.equals(Gasto.TIPO_FIJO) && filtroTipo == 1 ||
                    g.tipo.equals(Gasto.TIPO_INSUMO) && filtroTipo == 2 ||
                    g.tipo.equals(Gasto.TIPO_OTRO) && filtroTipo == 3;
            if (filtroTipo == -1 || pasa) {
                filtrados.add(g);
                total += g.monto;
            }
        }
        adapter.setItems(filtrados);
        tvTotalGastos.setText("Total: " + currencyFormat.format(total));
        tvSinDatos.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }
}