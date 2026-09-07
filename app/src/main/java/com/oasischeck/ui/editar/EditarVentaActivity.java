package com.oasischeck.ui.editar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.repository.VentaRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditarVentaActivity extends AppCompatActivity {

    private TextInputEditText etFecha, etCantidad, etPrecio, etInfo;
    private AutoCompleteTextView spinnerLugar, spinnerCategoria, spinnerDuenia, spinnerEstadoDinero;
    private AutoCompleteTextView etNombrePlanta;
    private TextView tvTotal;
    private VentaRepository repository;
    private Venta ventaActual;
    private long fechaSeleccionada;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_venta);

        repository = new VentaRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFecha = findViewById(R.id.et_fecha);
        etCantidad = findViewById(R.id.et_cantidad);
        etPrecio = findViewById(R.id.et_precio);
        etInfo = findViewById(R.id.et_info);
        spinnerLugar = findViewById(R.id.spinner_lugar);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        etNombrePlanta = findViewById(R.id.et_nombre_planta);
        spinnerDuenia = findViewById(R.id.spinner_duenia);
        spinnerEstadoDinero = findViewById(R.id.spinner_estado_dinero);
        tvTotal = findViewById(R.id.tv_total);
        MaterialButton btnGuardar = findViewById(R.id.btn_guardar);
        MaterialButton btnEliminar = findViewById(R.id.btn_eliminar);

        setupSpinners();
        setupDatePicker();
        setupTotalCalculator();

        long ventaId = getIntent().getLongExtra("venta_id", -1);
        if (ventaId != -1) {
            repository.getById(ventaId, venta -> {
                ventaActual = venta;
                runOnUiThread(() -> cargarDatos(venta));
            });
        }

        btnGuardar.setOnClickListener(v -> guardarVenta());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    private void setupSpinners() {
        spinnerLugar.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1_line, com.oasischeck.util.AppConstants.LUGARES));
        spinnerCategoria.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1_line, com.oasischeck.util.AppConstants.CATEGORIAS));
        spinnerDuenia.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1_line, com.oasischeck.util.AppConstants.DUENIAS));
        spinnerEstadoDinero.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1_line, com.oasischeck.util.AppConstants.ESTADOS_DINERO));
    }

    private void setupDatePicker() {
        etFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(fechaSeleccionada);
            new DatePickerDialog(this, (view, year, month, day) -> {
                fechaSeleccionada = new Calendar.Builder()
                        .setDate(year, month, day)
                        .build().getTimeInMillis();
                etFecha.setText(String.format("%d/%d/%d", day, month + 1, year));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupTotalCalculator() {
        etCantidad.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                actualizarTotal();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        etPrecio.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                actualizarTotal();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void actualizarTotal() {
        try {
            int cant = etCantidad.getText() != null ? Integer.parseInt(etCantidad.getText().toString()) : 0;
            double precio = etPrecio.getText() != null ? Double.parseDouble(etPrecio.getText().toString()) : 0;
            tvTotal.setText("Total: " + currencyFormat.format(cant * precio));
        } catch (NumberFormatException e) {
            tvTotal.setText("Total: $0");
        }
    }

    private void cargarDatos(Venta venta) {
        fechaSeleccionada = venta.fecha;
        etFecha.setText(dateFormat.format(new Date(venta.fecha)));
        etCantidad.setText(String.valueOf(venta.cantidad));
        etPrecio.setText(String.valueOf(venta.precioUnitario));
        etInfo.setText(venta.infoVenta);
        spinnerLugar.setText(venta.lugar, false);
        spinnerCategoria.setText(venta.categoria, false);
        etNombrePlanta.setText(venta.nombrePlanta != null ? venta.nombrePlanta : "");
        spinnerDuenia.setText(venta.duenia != null ? venta.duenia : "Ambas", false);
        spinnerEstadoDinero.setText(venta.estadoDinero != null ? venta.estadoDinero : Venta.ESTADO_EMPRENDIMIENTO, false);
        actualizarTotal();
    }

    private void guardarVenta() {
        if (etCantidad.getText() == null || etCantidad.getText().toString().isEmpty()) {
            etCantidad.setError("Requerido");
            return;
        }
        if (etPrecio.getText() == null || etPrecio.getText().toString().isEmpty()) {
            etPrecio.setError("Requerido");
            return;
        }

        ventaActual.fecha = fechaSeleccionada;
        try {
            ventaActual.cantidad = Integer.parseInt(etCantidad.getText().toString());
            ventaActual.precioUnitario = Double.parseDouble(etPrecio.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresá valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return;
        }
        ventaActual.calcularTotal();
        ventaActual.lugar = spinnerLugar.getText().toString();
        ventaActual.categoria = spinnerCategoria.getText().toString();
        ventaActual.infoVenta = etInfo.getText() != null ? etInfo.getText().toString() : "";
        ventaActual.nombrePlanta = etNombrePlanta.getText() != null ? etNombrePlanta.getText().toString().trim() : "";
        ventaActual.duenia = spinnerDuenia.getText().toString();
        ventaActual.estadoDinero = spinnerEstadoDinero.getText().toString();
        ventaActual.sincronizado = false;

        repository.update(ventaActual, () -> runOnUiThread(() -> {
            Toast.makeText(this, "Venta actualizada", Toast.LENGTH_SHORT).show();
            finish();
        }));
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar venta")
                .setMessage("¿Eliminar esta venta?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repository.delete(ventaActual, () -> runOnUiThread(() -> {
                        Toast.makeText(this, "Venta eliminada", Toast.LENGTH_SHORT).show();
                        finish();
                    }));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
