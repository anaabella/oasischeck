package com.oasischeck.ui.cuentas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.oasischeck.R;
import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.repository.CuentaRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CuentasActivity extends AppCompatActivity {

    private static final int FILTRO_TODAS = 0;
    private static final int FILTRO_COBRAR = 1;
    private static final int FILTRO_PAGAR = 2;
    private static final int FILTRO_PENDIENTES = 3;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    private CuentaRepository repository;
    private CuentaAdapter adapter;
    private List<Cuenta> todas = new ArrayList<>();
    private TextView tvTotalCobrar, tvTotalPagar, tvSinDatos;
    private ProgressBar progressBar;
    private int filtro = FILTRO_TODAS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuentas);

        repository = new CuentaRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTotalCobrar = findViewById(R.id.tv_total_cobrar);
        tvTotalPagar = findViewById(R.id.tv_total_pagar);
        tvSinDatos = findViewById(R.id.tv_sin_datos);
        progressBar = findViewById(R.id.progress_bar);
        MaterialButton btnFiltrar = findViewById(R.id.btn_filtrar_cuentas);
        FloatingActionButton fab = findViewById(R.id.fab_agregar_cuenta);

        RecyclerView recycler = findViewById(R.id.recycler_cuentas);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CuentaAdapter(new CuentaAdapter.Listener() {
            @Override
            public void onClick(Cuenta cuenta) {
                abrirEdicion(cuenta);
            }

            @Override
            public void onLongClick(Cuenta cuenta) {
                confirmarEliminar(cuenta);
            }
        });
        recycler.setAdapter(adapter);

        String[] opciones = {"Todas", "Por cobrar", "Por pagar", "Solo pendientes"};
        btnFiltrar.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Filtrar cuentas")
                .setItems(opciones, (d, which) -> {
                    filtro = which;
                    btnFiltrar.setText(opciones[which]);
                    aplicarFiltro();
                }).show());

        fab.setOnClickListener(v -> abrirEdicion(null));
        cargar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll(lista -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            todas = lista;
            aplicarFiltro();
        }));
    }

    private void aplicarFiltro() {
        List<Cuenta> filtradas = new ArrayList<>();
        double totalCobrar = 0;
        double totalPagar = 0;

        for (Cuenta c : todas) {
            if (!c.pagada) {
                if (Cuenta.TIPO_COBRAR.equals(c.tipo)) totalCobrar += c.monto;
                if (Cuenta.TIPO_PAGAR.equals(c.tipo)) totalPagar += c.monto;
            }
            boolean pasa;
            switch (filtro) {
                case FILTRO_COBRAR:
                    pasa = Cuenta.TIPO_COBRAR.equals(c.tipo);
                    break;
                case FILTRO_PAGAR:
                    pasa = Cuenta.TIPO_PAGAR.equals(c.tipo);
                    break;
                case FILTRO_PENDIENTES:
                    pasa = !c.pagada;
                    break;
                default:
                    pasa = true;
                    break;
            }
            if (pasa) filtradas.add(c);
        }

        adapter.setItems(filtradas);
        tvTotalCobrar.setText(currencyFormat.format(totalCobrar));
        tvTotalPagar.setText(currencyFormat.format(totalPagar));
        tvSinDatos.setVisibility(filtradas.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void abrirEdicion(Cuenta cuenta) {
        Intent intent = new Intent(this, AgregarCuentaActivity.class);
        if (cuenta != null) intent.putExtra("cuenta_id", cuenta.id);
        startActivity(intent);
    }

    private void confirmarEliminar(Cuenta cuenta) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("¿Eliminar \"" + cuenta.concepto + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> repository.delete(cuenta, this::cargar))
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
