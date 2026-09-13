package com.oasischeck.ui.intercambios;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.model.Intercambio;
import com.oasischeck.data.repository.IntercambioRepository;
import com.oasischeck.sheets.GoogleSheetsService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AgregarIntercambioActivity extends AppCompatActivity {

    private TextInputEditText etFecha, etPlantaEntregada, etPlantaRecibida, etPersona, etNotas;
    private IntercambioRepository repository;
    private long fechaSeleccionada = System.currentTimeMillis();
    private Intercambio intercambioActual;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_intercambio);

        repository = new IntercambioRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFecha = findViewById(R.id.et_fecha);
        etPlantaEntregada = findViewById(R.id.et_planta_entregada);
        etPlantaRecibida = findViewById(R.id.et_planta_recibida);
        etPersona = findViewById(R.id.et_persona);
        etNotas = findViewById(R.id.et_notas);
        MaterialButton btnGuardar = findViewById(R.id.btn_guardar);
        MaterialButton btnEliminar = findViewById(R.id.btn_eliminar);

        Calendar cal = Calendar.getInstance();
        etFecha.setText(String.format("%d/%d/%d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));

        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                c.set(year, month, day, 0, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                fechaSeleccionada = c.getTimeInMillis();
                etFecha.setText(String.format("%d/%d/%d", day, month + 1, year));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        long intercambioId = getIntent().getLongExtra("intercambio_id", -1);
        if (intercambioId != -1) {
            toolbar.setTitle("Editar Intercambio");
            btnEliminar.setVisibility(android.view.View.VISIBLE);
            repository.getById(intercambioId, intercambio -> {
                intercambioActual = intercambio;
                runOnUiThread(() -> cargarDatos(intercambio));
            });
        }

        btnGuardar.setOnClickListener(v -> guardarIntercambio());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    private void cargarDatos(Intercambio intercambio) {
        fechaSeleccionada = intercambio.fecha;
        etFecha.setText(dateFormat.format(new Date(intercambio.fecha)));
        etPlantaEntregada.setText(intercambio.plantaEntregada);
        etPlantaRecibida.setText(intercambio.plantaRecibida);
        etPersona.setText(intercambio.persona);
        etNotas.setText(intercambio.notas);
    }

    private void guardarIntercambio() {
        if (etPlantaEntregada.getText() == null || etPlantaEntregada.getText().toString().isEmpty()) {
            etPlantaEntregada.setError("Requerido");
            return;
        }
        if (etPlantaRecibida.getText() == null || etPlantaRecibida.getText().toString().isEmpty()) {
            etPlantaRecibida.setError("Requerido");
            return;
        }
        if (etPersona.getText() == null || etPersona.getText().toString().isEmpty()) {
            etPersona.setError("Requerido");
            return;
        }

        String plantaEntregada = etPlantaEntregada.getText().toString().trim();
        String plantaRecibida = etPlantaRecibida.getText().toString().trim();
        String persona = etPersona.getText().toString().trim();
        String notas = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";

        if (intercambioActual != null) {
            intercambioActual.fecha = fechaSeleccionada;
            intercambioActual.plantaEntregada = plantaEntregada;
            intercambioActual.plantaRecibida = plantaRecibida;
            intercambioActual.persona = persona;
            intercambioActual.notas = notas;
            intercambioActual.sincronizado = false;

            repository.update(intercambioActual, () -> runOnUiThread(() -> {
                Toast.makeText(this, "Intercambio actualizado", Toast.LENGTH_SHORT).show();
                finish();
            }));
        } else {
            Intercambio intercambio = new Intercambio(fechaSeleccionada, plantaEntregada, plantaRecibida, persona, notas);
            repository.insert(intercambio, id -> {
                intercambio.id = id;
                GoogleSheetsService.sincronizarIntercambio(this, intercambio);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Intercambio guardado", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar intercambio")
                .setMessage("¿Eliminar este intercambio?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repository.delete(intercambioActual, () -> runOnUiThread(() -> {
                        Toast.makeText(this, "Intercambio eliminado", Toast.LENGTH_SHORT).show();
                        finish();
                    }));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
