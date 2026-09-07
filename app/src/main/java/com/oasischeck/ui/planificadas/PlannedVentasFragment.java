package com.oasischeck.ui.planificadas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.oasischeck.R;
import com.oasischeck.data.model.PlannedVenta;
import com.oasischeck.data.repository.PlannedVentaRepository;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlannedVentasFragment extends Fragment implements PlannedVentaAdapter.OnPlannedVentaClickListener {

    private PlannedVentaRepository repository;
    private PlannedVentaAdapter adapter;
    private List<PlannedVenta> todas = new java.util.ArrayList<>();
    private TextView tvSinDatos;
    private ProgressBar progressBar;
    private int filtroEstado = -1; // -1 = todas

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planned_ventas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new PlannedVentaRepository(requireActivity().getApplication());

        RecyclerView         recycler = view.findViewById(R.id.recycler_planned);
        tvSinDatos = view.findViewById(R.id.tv_sin_datos);
        progressBar = view.findViewById(R.id.progress_bar);
        MaterialButton btnFiltrarEstado = view.findViewById(R.id.btn_filtrar_estado);
        MaterialButton btnBuscarCliente = view.findViewById(R.id.btn_buscar_cliente);
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab_agregar);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlannedVentaAdapter(this);
        recycler.setAdapter(adapter);

        btnFiltrarEstado.setOnClickListener(v -> mostrarFiltroEstado());
        btnBuscarCliente.setOnClickListener(v -> buscarCliente());

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AgregarPlannedVentaActivity.class);
            startActivity(intent);
        });

        cargarTodas();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarTodas();
    }

    private void cargarTodas() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll(ventas -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    todas = ventas;
                    aplicarFiltro();
                });
            }
        });
    }

    private void aplicarFiltro() {
        List<PlannedVenta> filtradas = new java.util.ArrayList<>();
        for (PlannedVenta p : todas) {
            if (filtroEstado == -1 || p.estado == filtroEstado) {
                filtradas.add(p);
            }
        }
        adapter.setItems(filtradas);
        tvSinDatos.setVisibility(filtradas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarFiltroEstado() {
        String[] opciones = {"Todas", "Pendientes", "Vendidas", "Canceladas"};
        int[] valores = {-1, PlannedVenta.ESTADO_PENDIENTE, PlannedVenta.ESTADO_VENDIDA, PlannedVenta.ESTADO_CANCELADA};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Filtrar por estado")
                .setItems(opciones, (d, which) -> {
                    filtroEstado = valores[which];
                    MaterialButton btn = getView().findViewById(R.id.btn_filtrar_estado);
                    btn.setText(opciones[which]);
                    aplicarFiltro();
                }).show();
    }

    private void buscarCliente() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Nombre del cliente");
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Buscar cliente")
                .setView(input)
                .setPositiveButton("Buscar", (d, w) -> {
                    String query = input.getText().toString().trim();
                    if (!query.isEmpty()) {
                        repository.searchByCliente(query, lista -> {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    adapter.setItems(lista);
                                    tvSinDatos.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                                });
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onClick(PlannedVenta p) {
        // Click normal - ver detalle o editar
        Intent intent = new Intent(getContext(), AgregarPlannedVentaActivity.class);
        intent.putExtra("planned_id", p.id);
        startActivity(intent);
    }

    @Override
    public void onMarcarVendida(PlannedVenta p) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Marcar como vendida")
                .setMessage("¿Convertir a venta real? Se abrirá el formulario con los datos cargados.")
                .setPositiveButton("Sí, crear venta", (d, w) -> convertirAVentaReal(p))
                .setNegativeButton("Solo marcar", (d, w) -> soloMarcarVendida(p))
                .show();
    }

    @Override
    public void onCancelar(PlannedVenta p) {
        p.estado = PlannedVenta.ESTADO_CANCELADA;
        repository.update(p, () -> {
            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Marcada como cancelada", Toast.LENGTH_SHORT).show();
                cargarTodas();
            });
        });
    }

    @Override
    public void onEditar(PlannedVenta p) {
        Intent intent = new Intent(getContext(), AgregarPlannedVentaActivity.class);
        intent.putExtra("planned_id", p.id);
        startActivity(intent);
    }

    @Override
    public void onEliminar(PlannedVenta p) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar")
                .setMessage("¿Eliminar esta planificación?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repository.delete(p, () -> {
                        if (getActivity() != null) getActivity().runOnUiThread(() -> cargarTodas());
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void soloMarcarVendida(PlannedVenta p) {
        p.estado = PlannedVenta.ESTADO_VENDIDA;
        repository.update(p, () -> {
            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Marcada como vendida", Toast.LENGTH_SHORT).show();
                cargarTodas();
            });
        });
    }

    private void convertirAVentaReal(PlannedVenta p) {
        Intent intent = new Intent(getContext(), com.oasischeck.ui.agregar.AgregarVentaActivity.class);
        intent.putExtra("from_planned", true);
        intent.putExtra("planned_id", p.id);
        intent.putExtra("prefill_fecha", p.fecha);
        intent.putExtra("prefill_info", p.infoVenta);
        intent.putExtra("prefill_total", p.totalEstimado);
        intent.putExtra("prefill_cliente", p.cliente);
        startActivity(intent);
    }
}