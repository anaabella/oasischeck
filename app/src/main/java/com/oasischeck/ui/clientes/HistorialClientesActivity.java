package com.oasischeck.ui.clientes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.ClienteInfo;
import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.model.PlannedVenta;
import com.oasischeck.data.model.Venta;

import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class HistorialClientesActivity extends AppCompatActivity {

    private final List<ClienteResumen> todos = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private ClienteHistorialAdapter adapter;
    private EditText etBuscar;
    private TextView tvResumen, tvSinClientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_clientes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etBuscar = findViewById(R.id.et_buscar);
        tvResumen = findViewById(R.id.tv_resumen_clientes);
        tvSinClientes = findViewById(R.id.tv_sin_clientes);

        RecyclerView recycler = findViewById(R.id.recycler_clientes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClienteHistorialAdapter(c -> {
            android.content.Intent intent = new android.content.Intent(this, DetalleClienteActivity.class);
            intent.putExtra(DetalleClienteActivity.EXTRA_CLIENTE, c.nombre);
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { aplicarFiltro(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        cargar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Map<String, ClienteResumen> mapa = new LinkedHashMap<>();

            for (Venta v : db.ventaDao().getAll()) {
                if (esVacia(v.cliente)) continue;
                if ("Inversion".equals(v.categoria)) continue;
                ClienteResumen r = obtener(mapa, v.cliente);
                r.ventas++;
                r.totalVentas += v.total;
                r.ultimaActividad = Math.max(r.ultimaActividad, v.fecha);
            }

            for (Pedido p : db.pedidoDao().getAll()) {
                if (esVacia(p.cliente)) continue;
                ClienteResumen r = obtener(mapa, p.cliente);
                r.pedidos++;
                r.totalPedidos += p.total;
                r.ultimaActividad = Math.max(r.ultimaActividad, p.fecha);
            }

            for (PlannedVenta pv : db.plannedVentaDao().getAll()) {
                if (esVacia(pv.cliente)) continue;
                if (pv.estado != PlannedVenta.ESTADO_PENDIENTE) continue;
                ClienteResumen r = obtener(mapa, pv.cliente);
                r.planificadas++;
                r.totalPlanificadas += pv.totalEstimado;
                r.ultimaActividad = Math.max(r.ultimaActividad, pv.fecha);
            }

            for (Cuenta c : db.cuentaDao().getAll()) {
                if (esVacia(c.persona)) continue;
                double saldo = c.getSaldoPendiente();
                if (saldo <= 0) continue;
                ClienteResumen r = obtener(mapa, c.persona);
                r.cuentasPendientes++;
                if (Cuenta.TIPO_COBRAR.equals(c.tipo)) r.porCobrar += saldo;
                if (Cuenta.TIPO_PAGAR.equals(c.tipo)) r.porPagar += saldo;
                r.ultimaActividad = Math.max(r.ultimaActividad, c.fecha);
            }

            Map<String, ClienteInfo> infos = new HashMap<>();
            for (ClienteInfo info : db.clienteInfoDao().getAll()) {
                infos.put(info.clave, info);
            }

            List<ClienteResumen> lista = new ArrayList<>(mapa.values());
            for (ClienteResumen r : lista) {
                ClienteInfo info = infos.get(normalizar(r.nombre));
                if (info != null && info.origen != null) r.origen = info.origen;
            }

            Collator collator = Collator.getInstance(new Locale("es", "AR"));
            Collections.sort(lista, (a, b) -> collator.compare(a.nombre, b.nombre));

            runOnUiThread(() -> {
                todos.clear();
                todos.addAll(lista);
                aplicarFiltro();
            });
        });
    }

    private void aplicarFiltro() {
        String busqueda = normalizar(etBuscar.getText() != null ? etBuscar.getText().toString() : "");
        List<ClienteResumen> filtrados = new ArrayList<>();
        double ventas = 0;
        double porCobrar = 0;

        for (ClienteResumen c : todos) {
            if (!busqueda.isEmpty() && !normalizar(c.nombre).contains(busqueda)) continue;
            filtrados.add(c);
            ventas += c.getTotalComprado();
            porCobrar += c.porCobrar;
        }

        adapter.setItems(filtrados);
        tvSinClientes.setVisibility(filtrados.isEmpty() ? TextView.VISIBLE : TextView.GONE);
        tvResumen.setText(filtrados.size() + " cliente(s) · Vendido: " + currencyFormat.format(ventas)
                + " · Por cobrar: " + currencyFormat.format(porCobrar));
    }

    static ClienteResumen obtener(Map<String, ClienteResumen> mapa, String nombre) {
        String clave = normalizar(nombre);
        ClienteResumen r = mapa.get(clave);
        if (r == null) {
            r = new ClienteResumen();
            r.nombre = nombre.trim().replaceAll("\\s+", " ");
            mapa.put(clave, r);
        }
        return r;
    }

    static String normalizar(String texto) {
        if (texto == null) return "";
        return texto.trim().replaceAll("\\s+", " ").toLowerCase(new Locale("es", "AR"));
    }

    private static boolean esVacia(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
