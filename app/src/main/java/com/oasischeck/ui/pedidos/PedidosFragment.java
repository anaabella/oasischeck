package com.oasischeck.ui.pedidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.oasischeck.R;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.repository.PedidoRepository;

import java.util.ArrayList;
import java.util.List;

public class PedidosFragment extends Fragment implements PedidoAdapter.OnPedidoClickListener {

    private PedidoRepository repository;
    private PedidoAdapter adapter;
    private List<Pedido> todos = new ArrayList<>();
    private TextView tvSinDatos;
    private ProgressBar progressBar;
    private int filtroEstado = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pedidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new PedidoRepository(requireActivity().getApplication());

        RecyclerView         recycler = view.findViewById(R.id.recycler_pedidos);
        tvSinDatos = view.findViewById(R.id.tv_sin_datos);
        progressBar = view.findViewById(R.id.progress_bar);
        MaterialButton btnFiltrarEstado = view.findViewById(R.id.btn_filtrar_estado);
        MaterialButton btnBuscarCliente = view.findViewById(R.id.btn_buscar_cliente);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PedidoAdapter(this);
        recycler.setAdapter(adapter);

        btnFiltrarEstado.setOnClickListener(v -> mostrarFiltroEstado());
        btnBuscarCliente.setOnClickListener(v -> buscarCliente());

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
            if (filtroEstado == -1 || p.estado == filtroEstado) {
                filtrados.add(p);
            }
        }
        adapter.setItems(filtrados);
        tvSinDatos.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarFiltroEstado() {
        String[] opciones = {"Todos", "Pendientes", "Preparados", "Entregados"};
        int[] valores = {-1, Pedido.ESTADO_PENDIENTE, Pedido.ESTADO_PREPARADO, Pedido.ESTADO_ENTREGADO};
        new AlertDialog.Builder(requireContext())
                .setTitle("Filtrar por estado")
                .setItems(opciones, (d, which) -> {
                    filtroEstado = valores[which];
                    MaterialButton btn = getView().findViewById(R.id.btn_filtrar_estado);
                    btn.setText(opciones[which]);
                    aplicarFiltro();
                }).show();
    }

    private void buscarCliente() {
        EditText input = new EditText(requireContext());
        input.setHint("Nombre del cliente");
        new AlertDialog.Builder(requireContext())
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
                .setMessage("¿Marcar el pedido de " + p.cliente + " como entregado?")
                .setPositiveButton("Sí", (d, w) -> {
                    p.estado = Pedido.ESTADO_ENTREGADO;
                    p.entregadoEn = System.currentTimeMillis();
                    repository.update(p, () -> {
                        if (getActivity() != null) getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Marcado como entregado", Toast.LENGTH_SHORT).show();
                            cargarTodos();
                        });
                    });
                })
                .setNegativeButton("No", null)
                .show();
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
}