package com.oasischeck.ui.intercambios;

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
import com.oasischeck.data.model.Intercambio;
import com.oasischeck.data.repository.IntercambioRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IntercambiosFragment extends Fragment {

    private IntercambioRepository repository;
    private IntercambioAdapter adapter;
    private List<Intercambio> todosLosIntercambios = new ArrayList<>();
    private TextView tvSinDatos;
    private ProgressBar progressBar;
    private MaterialButton btnFiltrarFecha;
    private MaterialButton btnFiltrarPersona;
    private String filtroPersona = null;
    private long filtroFechaInicio = -1;
    private long filtroFechaFin = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intercambios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new IntercambioRepository(requireActivity().getApplication());

        RecyclerView recycler = view.findViewById(R.id.recycler_intercambios);
        tvSinDatos = view.findViewById(R.id.tv_sin_datos);
        progressBar = view.findViewById(R.id.progress_bar);
        btnFiltrarFecha = view.findViewById(R.id.btn_filtrar_fecha);
        btnFiltrarPersona = view.findViewById(R.id.btn_filtrar_persona);
        SearchView searchView = view.findViewById(R.id.search_view);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IntercambioAdapter(intercambio -> {
            Intent intent = new Intent(getContext(), AgregarIntercambioActivity.class);
            intent.putExtra("intercambio_id", intercambio.id);
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        btnFiltrarFecha.setOnClickListener(v -> mostrarFiltroFecha());
        btnFiltrarPersona.setOnClickListener(v -> mostrarFiltroPersona());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) { cargarIntercambios(); return true; }
                repository.search(newText, intercambios -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapter.setItems(intercambios);
                            tvSinDatos.setVisibility(intercambios.isEmpty() ? View.VISIBLE : View.GONE);
                        });
                    }
                });
                return true;
            }
        });

        cargarIntercambios();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarIntercambios();
    }

    private void cargarIntercambios() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll(intercambios -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    todosLosIntercambios = intercambios;
                    aplicarFiltros();
                });
            }
        });
    }

    private void aplicarFiltros() {
        List<Intercambio> filtrados = new ArrayList<>();
        for (Intercambio i : todosLosIntercambios) {
            boolean pasaPersona = filtroPersona == null || i.persona.equals(filtroPersona);
            boolean pasaFecha = filtroFechaInicio == -1 || (i.fecha >= filtroFechaInicio && i.fecha <= filtroFechaFin);
            if (pasaPersona && pasaFecha) {
                filtrados.add(i);
            }
        }
        adapter.setItems(filtrados);
        tvSinDatos.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarFiltroFecha() {
        String[] opciones = {"Hoy", "Esta semana", "Este mes", "Todas"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Filtrar por fecha")
                .setItems(opciones, (d, which) -> {
                    Calendar cal = Calendar.getInstance();
                    switch (which) {
                        case 0:
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
                        case 1:
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
                        case 2:
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
                        case 3:
                            filtroFechaInicio = -1;
                            filtroFechaFin = -1;
                            btnFiltrarFecha.setText("Todas");
                            break;
                    }
                    aplicarFiltros();
                }).show();
    }

    private void mostrarFiltroPersona() {
        List<String> personas = new ArrayList<>();
        personas.add("Todas");
        for (Intercambio i : todosLosIntercambios) {
            if (i.persona != null && !i.persona.isEmpty() && !personas.contains(i.persona)) {
                personas.add(i.persona);
            }
        }
        String[] opciones = personas.toArray(new String[0]);
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Filtrar por persona")
                .setItems(opciones, (d, which) -> {
                    filtroPersona = which == 0 ? null : opciones[which];
                    btnFiltrarPersona.setText(which == 0 ? "Todas" : opciones[which]);
                    aplicarFiltros();
                }).show();
    }
}
