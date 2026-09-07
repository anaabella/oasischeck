package com.oasischeck.ui.gastos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.model.Gasto;
import com.oasischeck.data.repository.GastoRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AgregarGastoActivity extends AppCompatActivity {

    private TextInputEditText etFecha, etConcepto, etMonto, etNotas;
    private AutoCompleteTextView spinnerTipo;
    private MaterialSwitch switchRecurrente;
    private GastoRepository repository;
    private Gasto existente = null;
    private long fechaSeleccionada = System.currentTimeMillis();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_gasto);

        repository = new GastoRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFecha = findViewById(R.id.et_fecha);
        etConcepto = findViewById(R.id.et_concepto);
        etMonto = findViewById(R.id.et_monto);
        etNotas = findViewById(R.id.et_notas);
        spinnerTipo = findViewById(R.id.spinner_tipo);
        switchRecurrente = findViewById(R.id.switch_recurrente);

        spinnerTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1_line,
                com.oasischeck.util.AppConstants.TIPOS_GASTO));

        etFecha.setText(dateFormat.format(fechaSeleccionada));
        etFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(fechaSeleccionada);
            new DatePickerDialog(this, (view, y, m, d) -> {
                fechaSeleccionada = new Calendar.Builder().setDate(y, m, d).build().getTimeInMillis();
                etFecha.setText(String.format("%d/%d/%d", d, m + 1, y));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        long id = getIntent().getLongExtra("gasto_id", -1);
        if (id != -1) {
            toolbar.setTitle("Editar Gasto");
            repository.getAll(lista -> {
                for (Gasto g : lista) {
                    if (g.id == id) {
                        existente = g;
                        runOnUiThread(() -> cargarDatos(g));
                        break;
                    }
                }
            });
        }

        findViewById(R.id.btn_guardar).setOnClickListener(v -> guardar());
    }

    private void cargarDatos(Gasto g) {
        fechaSeleccionada = g.fecha;
        etFecha.setText(dateFormat.format(g.fecha));
        etConcepto.setText(g.concepto);
        etMonto.setText(String.valueOf(g.monto));
        spinnerTipo.setText(g.tipo, false);
        etNotas.setText(g.notas);
        switchRecurrente.setChecked(g.recurrente);
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

        Gasto g = existente != null ? existente : new Gasto();
        g.fecha = fechaSeleccionada;
        g.concepto = etConcepto.getText().toString().trim();
        try {
            g.monto = Double.parseDouble(etMonto.getText().toString());
        } catch (NumberFormatException e) {
            etMonto.setError("Precio inválido");
            return;
        }
        g.tipo = spinnerTipo.getText().toString();
        g.recurrente = switchRecurrente.isChecked();
        g.notas = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";

        if (existente != null) {
            repository.update(g, () -> runOnUiThread(() -> {
                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
                finish();
            }));
        } else {
            repository.insert(g, id -> runOnUiThread(() -> {
                Toast.makeText(this, "Gasto guardado", Toast.LENGTH_SHORT).show();
                finish();
            }));
        }
    }
}