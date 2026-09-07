package com.oasischeck.ui.agregar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.repository.VentaRepository;
import com.oasischeck.sheets.GoogleSheetsService;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class AgregarVentaActivity extends AppCompatActivity {

    private TextInputEditText etFecha, etCantidad, etPrecio, etInfo, etNombrePlanta;
    private AutoCompleteTextView spinnerLugar, spinnerCategoria, spinnerDuenia;
    private TextView tvTotal;
    private VentaRepository repository;
    private long fechaSeleccionada = System.currentTimeMillis();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_venta);

        repository = new VentaRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFecha = findViewById(R.id.et_fecha);
        etCantidad = findViewById(R.id.et_cantidad);
        etPrecio = findViewById(R.id.et_precio);
        etInfo = findViewById(R.id.et_info);
        etNombrePlanta = findViewById(R.id.et_nombre_planta);
        spinnerLugar = findViewById(R.id.spinner_lugar);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        spinnerDuenia = findViewById(R.id.spinner_duenia);
        tvTotal = findViewById(R.id.tv_total);
        MaterialButton btnGuardar = findViewById(R.id.btn_guardar);

        setupSpinners();
        setupDatePicker();
        setupTotalCalculator();

        boolean fromPlanned = getIntent().getBooleanExtra("from_planned", false);
        if (fromPlanned) {
            long prefillFecha = getIntent().getLongExtra("prefill_fecha", System.currentTimeMillis());
            String prefillInfo = getIntent().getStringExtra("prefill_info");
            double prefillTotal = getIntent().getDoubleExtra("prefill_total", 0);
            String prefillCliente = getIntent().getStringExtra("prefill_cliente");
            fechaSeleccionada = prefillFecha;
            etFecha.setText(new java.text.SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new java.util.Date(prefillFecha)));
            String infoCompleta = (prefillCliente != null ? prefillCliente + ": " : "") + (prefillInfo != null ? prefillInfo : "");
            etInfo.setText(infoCompleta);
            etPrecio.setText(String.valueOf(prefillTotal));
            etCantidad.setText("1");
            actualizarTotal();
        } else {
            Calendar cal = Calendar.getInstance();
            etFecha.setText(String.format("%d/%d/%d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));
        }

        btnGuardar.setOnClickListener(v -> guardarVenta(getIntent().getLongExtra("planned_id", -1)));
    }

    private void setupSpinners() {
        spinnerLugar.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1_line,
                com.oasischeck.util.AppConstants.LUGARES));
        spinnerCategoria.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1_line,
                com.oasischeck.util.AppConstants.CATEGORIAS));
        spinnerDuenia.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1_line,
                com.oasischeck.util.AppConstants.DUENIAS));
    }

    private void setupDatePicker() {
        etFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                fechaSeleccionada = new Calendar.Builder().setDate(year, month, day).build().getTimeInMillis();
                etFecha.setText(String.format("%d/%d/%d", day, month + 1, year));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupTotalCalculator() {
        android.text.TextWatcher tw = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { actualizarTotal(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        etCantidad.addTextChangedListener(tw);
        etPrecio.addTextChangedListener(tw);
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

    private void guardarVenta(long plannedId) {
        if (etCantidad.getText() == null || etCantidad.getText().toString().isEmpty()) { etCantidad.setError("Requerido"); return; }
        if (etPrecio.getText() == null || etPrecio.getText().toString().isEmpty()) { etPrecio.setError("Requerido"); return; }
        if (spinnerLugar.getText().toString().isEmpty()) { spinnerLugar.setError("Requerido"); return; }
        if (spinnerCategoria.getText().toString().isEmpty()) { spinnerCategoria.setError("Requerido"); return; }

        int cantidad;
        double precio;
        try {
            cantidad = Integer.parseInt(etCantidad.getText().toString());
            precio = Double.parseDouble(etPrecio.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresá un número válido", Toast.LENGTH_SHORT).show();
            return;
        }
        String lugar = spinnerLugar.getText().toString();
        String categoria = spinnerCategoria.getText().toString();
        String info = etInfo.getText() != null ? etInfo.getText().toString() : "";

        Venta venta = new Venta(fechaSeleccionada, cantidad, precio, lugar, categoria, info);
        venta.nombrePlanta = etNombrePlanta.getText() != null ? etNombrePlanta.getText().toString().trim() : "";
        venta.duenia = spinnerDuenia.getText().toString();
        venta.estadoDinero = Venta.ESTADO_EMPRENDIMIENTO;

        repository.insert(venta, id -> {
            venta.id = id;
            GoogleSheetsService.sincronizarVenta(this, venta);
            if (plannedId != -1) {
                new Thread(() -> {
                    com.oasischeck.data.db.AppDatabase db = com.oasischeck.data.db.AppDatabase.getInstance(getApplicationContext());
                    com.oasischeck.data.model.PlannedVenta p = db.plannedVentaDao().getById(plannedId);
                    if (p != null) { p.estado = com.oasischeck.data.model.PlannedVenta.ESTADO_VENDIDA; p.ventaRealId = id; db.plannedVentaDao().update(p); }
                }).start();
            }
            runOnUiThread(() -> { Toast.makeText(this, "Venta guardada", Toast.LENGTH_SHORT).show(); finish(); });
        });
    }
}