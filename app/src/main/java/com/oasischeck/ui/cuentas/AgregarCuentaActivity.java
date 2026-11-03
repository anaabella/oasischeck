package com.oasischeck.ui.cuentas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.repository.CuentaRepository;
import com.oasischeck.data.repository.VentaRepository;
import com.oasischeck.sheets.GoogleSheetsService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AgregarCuentaActivity extends AppCompatActivity {

    private TextInputEditText etConcepto, etMonto, etPersona, etFecha, etVencimiento, etNotas;
    private MaterialAutoCompleteTextView spinnerTipo;
    private MaterialSwitch switchPagada;
    private MaterialButton btnEliminar;

    private CuentaRepository repository;
    private VentaRepository ventaRepository;
    private Cuenta cuentaExistente;
    private long fechaSeleccionada = System.currentTimeMillis();
    private long vencimientoSeleccionado = 0;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_cuenta);

        repository = new CuentaRepository(getApplication());
        ventaRepository = new VentaRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerTipo = findViewById(R.id.spinner_tipo);
        etConcepto = findViewById(R.id.et_concepto);
        etMonto = findViewById(R.id.et_monto);
        etPersona = findViewById(R.id.et_persona);
        etFecha = findViewById(R.id.et_fecha);
        etVencimiento = findViewById(R.id.et_vencimiento);
        etNotas = findViewById(R.id.et_notas);
        switchPagada = findViewById(R.id.switch_pagada);
        btnEliminar = findViewById(R.id.btn_eliminar);

        spinnerTipo.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{Cuenta.TIPO_COBRAR, Cuenta.TIPO_PAGAR}));
        spinnerTipo.setText(Cuenta.TIPO_COBRAR, false);

        etFecha.setText(dateFormat.format(new Date(fechaSeleccionada)));
        etFecha.setOnClickListener(v -> mostrarDatePicker(true));
        etVencimiento.setOnClickListener(v -> mostrarDatePicker(false));

        long cuentaId = getIntent().getLongExtra("cuenta_id", -1);
        if (cuentaId != -1) {
            toolbar.setTitle("Editar cuenta");
            btnEliminar.setVisibility(android.view.View.VISIBLE);
            repository.getById(cuentaId, c -> runOnUiThread(() -> cargarDatos(c)));
        } else {
            btnEliminar.setVisibility(android.view.View.GONE);
        }

        findViewById(R.id.btn_guardar).setOnClickListener(v -> guardar());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    private void cargarDatos(Cuenta c) {
        if (c == null) return;
        cuentaExistente = c;
        spinnerTipo.setText(c.tipo, false);
        etConcepto.setText(c.concepto);
        etMonto.setText(String.valueOf(c.monto));
        etPersona.setText(c.persona);
        etNotas.setText(c.notas);
        switchPagada.setChecked(c.pagada);
        fechaSeleccionada = c.fecha;
        etFecha.setText(dateFormat.format(new Date(c.fecha)));
        vencimientoSeleccionado = c.vencimiento;
        if (c.vencimiento > 0) {
            etVencimiento.setText(dateFormat.format(new Date(c.vencimiento)));
        }
    }

    private void mostrarDatePicker(boolean esFecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(esFecha ? fechaSeleccionada
                : (vencimientoSeleccionado > 0 ? vencimientoSeleccionado : System.currentTimeMillis()));
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            if (esFecha) {
                fechaSeleccionada = c.getTimeInMillis();
                etFecha.setText(String.format("%d/%d/%d", day, month + 1, year));
            } else {
                vencimientoSeleccionado = c.getTimeInMillis();
                etVencimiento.setText(String.format("%d/%d/%d", day, month + 1, year));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void guardar() {
        if (etConcepto.getText() == null || etConcepto.getText().toString().trim().isEmpty()) {
            etConcepto.setError("Requerido");
            return;
        }
        if (etMonto.getText() == null || etMonto.getText().toString().trim().isEmpty()) {
            etMonto.setError("Requerido");
            return;
        }
        String tipo = spinnerTipo.getText().toString();
        if (tipo.isEmpty()) {
            spinnerTipo.setError("Requerido");
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(etMonto.getText().toString());
        } catch (NumberFormatException e) {
            etMonto.setError("Monto inválido");
            return;
        }
        if (monto <= 0) {
            etMonto.setError("El monto debe ser mayor a cero");
            return;
        }

        boolean estabaPagada = cuentaExistente != null && cuentaExistente.pagada;

        Cuenta c = cuentaExistente != null ? cuentaExistente : new Cuenta();
        c.fecha = fechaSeleccionada;
        c.tipo = tipo;
        c.concepto = etConcepto.getText().toString().trim();
        c.monto = monto;
        c.persona = etPersona.getText() != null ? etPersona.getText().toString().trim() : "";
        c.vencimiento = vencimientoSeleccionado;
        c.pagada = switchPagada.isChecked();
        c.notas = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";
        if (cuentaExistente == null) c.creadoEn = System.currentTimeMillis();

        if (cuentaExistente != null) {
            repository.update(c, () -> {
                generarVentaSiCorresponde(c, estabaPagada);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            repository.insert(c, id -> {
                c.id = id;
                generarVentaSiCorresponde(c, false);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }

    private void generarVentaSiCorresponde(Cuenta cuenta, boolean estabaPagada) {
        if (!cuenta.pagada || estabaPagada) return;
        if (!Cuenta.TIPO_COBRAR.equals(cuenta.tipo)) return;
        if (cuenta.ventaGeneradaId > 0) return;

        Venta venta = new Venta(cuenta.fecha, 1, cuenta.monto, "Entrega", "Plantas",
                "Cobro: " + (cuenta.concepto != null ? cuenta.concepto : ""));
        venta.nombrePlanta = cuenta.concepto != null ? cuenta.concepto : "";
        venta.duenia = "Ambas";
        venta.estadoDinero = Venta.ESTADO_EMPRENDIMIENTO;
        venta.sincronizado = false;

        ventaRepository.insert(venta, id -> {
            venta.id = id;
            GoogleSheetsService.sincronizarVenta(getApplicationContext(), venta);
            new Thread(() -> {
                cuenta.ventaGeneradaId = id;
                AppDatabase.getInstance(getApplicationContext()).cuentaDao().update(cuenta);
            }).start();
        });
    }

    private void confirmarEliminar() {
        if (cuentaExistente == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar esta cuenta?")
                .setPositiveButton("Eliminar", (d, w) ->
                        repository.delete(cuentaExistente, () -> runOnUiThread(() -> {
                            Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                            finish();
                        })))
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
