package com.oasischeck.ui.planificadas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.model.PlannedVenta;
import com.oasischeck.data.repository.PlannedVentaRepository;

import java.util.Calendar;

public class AgregarPlannedVentaActivity extends AppCompatActivity {

    private TextInputEditText etFecha, etCliente, etInfo, etTotal, etNotas;
    private MaterialButton btnGuardar, btnEliminar;
    private PlannedVentaRepository repository;
    private PlannedVenta planificadaExistente = null;
    private long fechaSeleccionada = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_planned);

        repository = new PlannedVentaRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFecha = findViewById(R.id.et_fecha);
        etCliente = findViewById(R.id.et_cliente);
        etInfo = findViewById(R.id.et_info);
        etTotal = findViewById(R.id.et_total);
        etNotas = findViewById(R.id.et_notas);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnEliminar = findViewById(R.id.btn_eliminar);

        long plannedId = getIntent().getLongExtra("planned_id", -1);
        if (plannedId != -1) {
            toolbar.setTitle(R.string.editar_planificada);
            btnEliminar.setVisibility(View.VISIBLE);
            repository.getById(plannedId, p -> {
                planificadaExistente = p;
                runOnUiThread(() -> cargarDatos(p));
            });
        } else {
            btnEliminar.setVisibility(View.GONE);
            Calendar cal = Calendar.getInstance();
            etFecha.setText(String.format("%d/%d/%d", cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));
        }

        etFecha.setOnClickListener(v -> mostrarDatePicker());

        btnGuardar.setOnClickListener(v -> guardar());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());

        findViewById(R.id.btn_calendario).setOnClickListener(v -> agregarAlCalendario());
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fechaSeleccionada);
        new DatePickerDialog(this, (view, year, month, day) -> {
            fechaSeleccionada = new Calendar.Builder()
                    .setDate(year, month, day)
                    .build().getTimeInMillis();
            etFecha.setText(String.format("%d/%d/%d", day, month + 1, year));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void cargarDatos(PlannedVenta p) {
        fechaSeleccionada = p.fecha;
        etFecha.setText(String.format("%d/%d/%d",
                new java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault()).format(new java.util.Date(p.fecha))));
        etCliente.setText(p.cliente);
        etInfo.setText(p.infoVenta);
        etTotal.setText(String.valueOf(p.totalEstimado));
        etNotas.setText(p.notas);
    }

    private void guardar() {
        if (etCliente.getText() == null || etCliente.getText().toString().trim().isEmpty()) {
            etCliente.setError("Requerido");
            return;
        }
        if (etInfo.getText() == null || etInfo.getText().toString().trim().isEmpty()) {
            etInfo.setError("Requerido");
            return;
        }
        if (etTotal.getText() == null || etTotal.getText().toString().trim().isEmpty()) {
            etTotal.setError("Requerido");
            return;
        }

        PlannedVenta p = planificadaExistente != null ? planificadaExistente : new PlannedVenta();
        p.fecha = fechaSeleccionada;
        p.cliente = etCliente.getText().toString().trim();
        p.infoVenta = etInfo.getText().toString().trim();
        try {
            p.totalEstimado = Double.parseDouble(etTotal.getText().toString());
        } catch (NumberFormatException e) {
            etTotal.setError("Precio inválido");
            return;
        }
        p.notas = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";
        p.creadoEn = planificadaExistente != null ? planificadaExistente.creadoEn : System.currentTimeMillis();

        if (planificadaExistente != null) {
            repository.update(p, () -> runOnUiThread(() -> {
                Toast.makeText(this, "Actualizada", Toast.LENGTH_SHORT).show();
                finish();
            }));
        } else {
            repository.insert(p, id -> runOnUiThread(() -> {
                Toast.makeText(this, "Guardada", Toast.LENGTH_SHORT).show();
                finish();
            }));
        }
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar esta planificación?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repository.delete(planificadaExistente, () -> runOnUiThread(() -> {
                        Toast.makeText(this, "Eliminada", Toast.LENGTH_SHORT).show();
                        finish();
                    }));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void agregarAlCalendario() {
        String cliente = etCliente.getText() != null ? etCliente.getText().toString() : "";
        String info = etInfo.getText() != null ? etInfo.getText().toString() : "";
        String total = etTotal.getText() != null ? etTotal.getText().toString() : "0";

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "Venta - " + cliente)
                .putExtra(CalendarContract.Events.DESCRIPTION, info + "\nTotal: $" + total)
                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, fechaSeleccionada)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fechaSeleccionada + 86400000);
        startActivity(intent);
    }
}