package com.oasischeck.ui.pedidos;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.oasischeck.R;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.repository.PedidoRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PedidosFragment extends Fragment implements PedidoAdapter.OnPedidoClickListener {

    private static final int FILTRO_ACTIVOS = -1;

    private PedidoRepository repository;
    private PedidoAdapter adapter;
    private List<Pedido> todos = new ArrayList<>();
    private TextView tvSinDatos;
    private ProgressBar progressBar;
    private int filtroEstado = FILTRO_ACTIVOS;
    private String query = "";

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pedidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new PedidoRepository(requireActivity().getApplication());

        RecyclerView recycler = view.findViewById(R.id.recycler_pedidos);
        tvSinDatos = view.findViewById(R.id.tv_sin_datos);
        progressBar = view.findViewById(R.id.progress_bar);
        MaterialButton btnFiltrarEstado = view.findViewById(R.id.btn_filtrar_estado);
        SearchView searchView = view.findViewById(R.id.search_view);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PedidoAdapter(this);
        recycler.setAdapter(adapter);

        btnFiltrarEstado.setOnClickListener(v -> mostrarFiltroEstado());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String s) { return false; }
            @Override public boolean onQueryTextChange(String s) {
                query = s != null ? s.trim().toLowerCase(Locale.getDefault()) : "";
                aplicarFiltro();
                return true;
            }
        });

        cargarTodos();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarTodos();
    }

    private void cargarTodos() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll(pedidos -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    todos = pedidos;
                    aplicarFiltro();
                });
            }
        });
    }

    private void aplicarFiltro() {
        List<Pedido> filtrados = new ArrayList<>();
        for (Pedido p : todos) {
            boolean pasaEstado;
            if (filtroEstado == FILTRO_ACTIVOS) {
                pasaEstado = p.estado != Pedido.ESTADO_ENTREGADO;
            } else {
                pasaEstado = p.estado == filtroEstado;
            }

            boolean pasaQuery = query.isEmpty()
                    || (p.cliente != null && p.cliente.toLowerCase(Locale.getDefault()).contains(query))
                    || (p.productos != null && p.productos.toLowerCase(Locale.getDefault()).contains(query))
                    || (p.notas != null && p.notas.toLowerCase(Locale.getDefault()).contains(query));

            if (pasaEstado && pasaQuery) filtrados.add(p);
        }
        adapter.setItems(filtrados);

        boolean esHistorial = filtroEstado == Pedido.ESTADO_ENTREGADO;
        tvSinDatos.setText(esHistorial ? "No hay pedidos en el historial" : "No hay pedidos activos");
        tvSinDatos.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarFiltroEstado() {
        String[] opciones = {"Activos", "Pendientes", "Preparados", "Historial"};
        int[] valores = {FILTRO_ACTIVOS, Pedido.ESTADO_PENDIENTE, Pedido.ESTADO_PREPARADO, Pedido.ESTADO_ENTREGADO};
        new AlertDialog.Builder(requireContext())
                .setTitle("Filtrar pedidos")
                .setItems(opciones, (d, which) -> {
                    filtroEstado = valores[which];
                    MaterialButton btn = getView().findViewById(R.id.btn_filtrar_estado);
                    btn.setText(opciones[which]);
                    aplicarFiltro();
                }).show();
    }

    @Override
    public void onClick(Pedido p) {
        Intent intent = new Intent(getContext(), AgregarPedidoActivity.class);
        intent.putExtra("pedido_id", p.id);
        startActivity(intent);
    }

    @Override
    public void onPreparado(Pedido p) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Marcar como preparado")
                .setMessage("¿Marcar el pedido de " + p.cliente + " como preparado?")
                .setPositiveButton("Sí", (d, w) -> {
                    p.estado = Pedido.ESTADO_PREPARADO;
                    p.preparadoEn = System.currentTimeMillis();
                    repository.update(p, () -> {
                        if (getActivity() != null) getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Marcado como preparado", Toast.LENGTH_SHORT).show();
                            cargarTodos();
                        });
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onEntregado(Pedido p) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Marcar como entregado")
                .setMessage("El pedido de " + p.cliente + " pasará al historial y dejará de aparecer en los activos. ¿Continuar?")
                .setPositiveButton("Sí", (d, w) -> {
                    p.estado = Pedido.ESTADO_ENTREGADO;
                    p.entregadoEn = System.currentTimeMillis();
                    repository.update(p, () -> {
                        if (getActivity() != null) getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Pedido movido al historial", Toast.LENGTH_SHORT).show();
                            cargarTodos();
                        });
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onCompartir(Pedido p) {
        String texto = "Pedido de " + p.cliente + "\n" +
                "Fecha: " + dateFormat.format(new Date(p.fecha)) + "\n" +
                "Productos: " + (p.productos != null ? p.productos : "") + "\n" +
                "Total: " + currencyFormat.format(p.total) + "\n" +
                "Estado: " + estadoTexto(p.estado) + "\n" +
                (p.notas != null && !p.notas.isEmpty() ? "Notas: " + p.notas + "\n" : "") +
                "\nOasisCheck";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, texto);
        startActivity(Intent.createChooser(shareIntent, "Compartir pedido"));
    }

    @Override
    public void onEliminar(Pedido p) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar pedido")
                .setMessage("¿Eliminar el pedido de " + p.cliente + "?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repository.delete(p, () -> {
                        if (getActivity() != null) getActivity().runOnUiThread(() -> cargarTodos());
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private static String estadoTexto(int estado) {
        switch (estado) {
            case Pedido.ESTADO_PREPARADO: return "Preparado";
            case Pedido.ESTADO_ENTREGADO: return "Entregado";
            default: return "Pendiente";
        }
    }
}
