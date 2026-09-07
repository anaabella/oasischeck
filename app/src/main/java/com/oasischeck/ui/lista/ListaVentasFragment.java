package com.oasischeck.ui.lista;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.oasischeck.R;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.repository.VentaRepository;
import com.oasischeck.ui.editar.EditarVentaActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ListaVentasFragment extends Fragment {

    private VentaRepository repository;
    private VentaAdapter adapter;
    private List<Venta> todasLasVentas = new ArrayList<>();
    private TextView tvTotalFiltrado, tvSinDatos;
    private ProgressBar progressBar;
    private MaterialButton btnFiltrarFecha;
    private String filtroLugar = null;
    private long filtroFechaInicio = -1;
    private long filtroFechaFin = -1;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_ventas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new VentaRepository(requireActivity().getApplication());

        RecyclerView recycler = view.findViewById(R.id.recycler_ventas);
        tvTotalFiltrado = view.findViewById(R.id.tv_total_filtrado);
        tvSinDatos = view.findViewById(R.id.tv_sin_datos);
        progressBar = view.findViewById(R.id.progress_bar);
        btnFiltrarFecha = view.findViewById(R.id.btn_filtrar_fecha);
        MaterialButton btnFiltrarLugar = view.findViewById(R.id.btn_filtrar_lugar);
        SearchView searchView = view.findViewById(R.id.search_view);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VentaAdapter(venta -> {
            Intent intent = new Intent(getContext(), EditarVentaActivity.class);
            intent.putExtra("venta_id", venta.id);
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        btnFiltrarFecha.setOnClickListener(v -> mostrarFiltroFecha());
        btnFiltrarLugar.setOnClickListener(v -> mostrarFiltroLugar());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) { cargarVentas(); return true; }
                repository.search(newText, ventas -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            List<DisplayItem> items = agruparPorDiaYMonto(ventas);
                            adapter.setItems(items);
                            tvSinDatos.setVisibility(ventas.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    }
                });
                return true;
            }
        });

        cargarVentas();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarVentas();
    }

    private void cargarVentas() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll(ventas -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    todasLasVentas = ventas;
                    aplicarFiltros();
                });
            }
        });
    }

    private void aplicarFiltros() {
        List<Venta> filtradas = new ArrayList<>();
        double totalFiltrado = 0;
        for (Venta v : todasLasVentas) {
            boolean pasaLugar = filtroLugar == null || v.lugar.equals(filtroLugar);
            boolean pasaFecha = filtroFechaInicio == -1 || (v.fecha >= filtroFechaInicio && v.fecha <= filtroFechaFin);
            if (pasaLugar && pasaFecha) {
                filtradas.add(v);
                if (!v.categoria.equals("Inversion")) totalFiltrado += v.total;
            }
        }
        List<DisplayItem> items = agruparPorDiaYMonto(filtradas);
        adapter.setItems(items);
        tvTotalFiltrado.setText("Total filtrado: " + currencyFormat.format(totalFiltrado));
        tvSinDatos.setVisibility(filtradas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<DisplayItem> agruparPorDiaYMonto(List<Venta> ventas) {
        List<DisplayItem> resultado = new ArrayList<>();
        if (ventas.isEmpty()) return resultado;

        java.util.Collections.sort(ventas, (a, b) -> {
            int fechaCompare = Long.compare(b.fecha, a.fecha);
            if (fechaCompare != 0) return fechaCompare;
            return Double.compare(b.total, a.total);
        });

        int i = 0;
        while (i < ventas.size()) {
            Venta primera = ventas.get(i);
            String diaKey = new java.text.SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new java.util.Date(primera.fecha));
            List<Venta> ventasDelDia = new ArrayList<>();
            while (i < ventas.size()) {
                Venta v = ventas.get(i);
                String vDia = new java.text.SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new java.util.Date(v.fecha));
                if (!vDia.equals(diaKey)) break;
                ventasDelDia.add(v);
                i++;
            }
            resultado.add(DisplayItem.headerDia(diaKey, ventasDelDia.size()));
            resultado.addAll(agruparPorMonto(ventasDelDia));
        }
        return resultado;
    }

    private List<DisplayItem> agruparPorMonto(List<Venta> ventas) {
        List<DisplayItem> resultado = new ArrayList<>();
        if (ventas.isEmpty()) return resultado;
        int j = 0;
        while (j < ventas.size()) {
            Venta primera = ventas.get(j);
            double monto = primera.total;
            List<Venta> grupo = new ArrayList<>();
            while (j < ventas.size() && Double.compare(ventas.get(j).total, monto) == 0) {
                grupo.add(ventas.get(j));
                j++;
            }
            if (grupo.size() == 1) {
                resultado.add(DisplayItem.venta(grupo.get(0)));
            } else {
                resultado.add(DisplayItem.header(currencyFormat.format(monto), grupo.size()));
                for (Venta v : grupo) resultado.add(DisplayItem.venta(v));
            }
        }
        return resultado;
    }

    private void mostrarFiltroFecha() {
        String[] opciones = {"Hoy", "Esta semana", "Este mes", "Rango personalizado", "Todas"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Filtrar por fecha")
                .setItems(opciones, (d, which) -> {
                    Calendar cal = Calendar.getInstance();
                    switch (which) {
                        case 0: // Hoy
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            filtroFechaInicio = cal.getTimeInMillis();
                            cal.set(Calendar.HOUR_OF_DAY, 23);
                            cal.set(Calendar.MINUTE, 59);
                            cal.set(Calendar.SECOND, 59);
                            filtroFechaFin = cal.getTimeInMillis();
                            btnFiltrarFecha.setText("Hoy");
                            break;
                        case 1: // Esta semana (lunes a hoy)
                            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            filtroFechaInicio = cal.getTimeInMillis();
                            Calendar ahora = Calendar.getInstance();
                            ahora.set(Calendar.HOUR_OF_DAY, 23);
                            ahora.set(Calendar.MINUTE, 59);
                            ahora.set(Calendar.SECOND, 59);
                            filtroFechaFin = ahora.getTimeInMillis();
                            btnFiltrarFecha.setText("Semana");
                            break;
                        case 2: // Este mes
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            filtroFechaInicio = cal.getTimeInMillis();
                            Calendar ahoraMes = Calendar.getInstance();
                            ahoraMes.set(Calendar.HOUR_OF_DAY, 23);
                            ahoraMes.set(Calendar.MINUTE, 59);
                            ahoraMes.set(Calendar.SECOND, 59);
                            filtroFechaFin = ahoraMes.getTimeInMillis();
                            btnFiltrarFecha.setText("Mes");
                            break;
                        case 3: // Rango personalizado
                            mostrarRangoPersonalizado();
                            return;
                        case 4: // Todas
                            filtroFechaInicio = -1;
                            filtroFechaFin = -1;
                            btnFiltrarFecha.setText("Todas");
                            break;
                    }
                    aplicarFiltros();
                }).show();
    }

    private void mostrarRangoPersonalizado() {
        Calendar calInicio = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            Calendar inicio = Calendar.getInstance();
            inicio.set(year, month, day, 0, 0, 0);
            inicio.set(Calendar.MILLISECOND, 0);
            long fechaInicio = inicio.getTimeInMillis();

            Calendar calFin = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view2, year2, month2, day2) -> {
                Calendar fin = Calendar.getInstance();
                fin.set(year2, month2, day2, 23, 59, 59);
                fin.set(Calendar.MILLISECOND, 999);
                filtroFechaInicio = fechaInicio;
                filtroFechaFin = fin.getTimeInMillis();
                btnFiltrarFecha.setText(day + "/" + (month + 1) + " - " + day2 + "/" + (month2 + 1));
                aplicarFiltros();
            }, calFin.get(Calendar.YEAR), calFin.get(Calendar.MONTH), calFin.get(Calendar.DAY_OF_MONTH)).show();
        }, calInicio.get(Calendar.YEAR), calInicio.get(Calendar.MONTH), calInicio.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarFiltroLugar() {
        String[] opciones = {"Todas", "Feria", "Vivero", "Quiniela", "Libreria", "Entrega", "Inversion"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Filtrar por lugar")
                .setItems(opciones, (d, which) -> {
                    filtroLugar = which == 0 ? null : opciones[which];
                    aplicarFiltros();
                }).show();
    }
}