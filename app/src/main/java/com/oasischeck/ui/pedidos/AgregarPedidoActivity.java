package com.oasischeck.ui.pedidos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.repository.PedidoRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AgregarPedidoActivity extends AppCompatActivity {

    private TextInputEditText etFecha, etCliente, etProductos, etTotal, etNotas;
    private MaterialButton btnGuardar, btnEliminar;
    private PedidoRepository repository;
    private Pedido pedidoExistente = null;
    private long fechaSeleccionada = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_pedido);

        repository = new PedidoRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFecha = findViewById(R.id.et_fecha);
        etCliente = findViewById(R.id.et_cliente);
        etProductos = findViewById(R.id.et_productos);
        etTotal = findViewById(R.id.et_total);
        etNotas = findViewById(R.id.et_notas);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnEliminar = findViewById(R.id.btn_eliminar);

        long pedidoId = getIntent().getLongExtra("pedido_id", -1);
        if (pedidoId != -1) {
            toolbar.setTitle("Editar Pedido");
            btnEliminar.setVisibility(android.view.View.VISIBLE);
            repository.getById(pedidoId, p -> {
                pedidoExistente = p;
                runOnUiThread(() -> cargarDatos(p));
            });
        } else {
            btnEliminar.setVisibility(android.view.View.GONE);
            Calendar cal = Calendar.getInstance();
            etFecha.setText(String.format("%d/%d/%d", cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));
        }

        etFecha.setOnClickListener(v -> mostrarDatePicker());

        btnGuardar.setOnClickListener(v -> guardar());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
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

    private void cargarDatos(Pedido p) {
        fechaSeleccionada = p.fecha;
        etFecha.setText(new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date(p.fecha)));
        etCliente.setText(p.cliente);
        etProductos.setText(p.productos);
        etTotal.setText(String.valueOf(p.total));
        etNotas.setText(p.notas);
    }

    private void guardar() {
        if (etCliente.getText() == null || etCliente.getText().toString().trim().isEmpty()) {
            etCliente.setError("Requerido");
            return;
        }
        if (etProductos.getText() == null || etProductos.getText().toString().trim().isEmpty()) {
            etProductos.setError("Requerido");
            return;
        }
        if (etTotal.getText() == null || etTotal.getText().toString().trim().isEmpty()) {
            etTotal.setError("Requerido");
            return;
        }

        Pedido p = pedidoExistente != null ? pedidoExistente : new Pedido();
        p.fecha = fechaSeleccionada;
        p.cliente = etCliente.getText().toString().trim();
        p.productos = etProductos.getText().toString().trim();
        try {
            p.total = Double.parseDouble(etTotal.getText().toString());
        } catch (NumberFormatException e) {
            etTotal.setError("Precio inválido");
            return;
        }
        p.notas = etNotas.getText() != null ? etNotas.getText().toString().trim() : "";
        if (pedidoExistente == null) p.creadoEn = System.currentTimeMillis();

        if (pedidoExistente != null) {
            repository.update(p, () -> runOnUiThread(() -> {
                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
                finish();
            }));
        } else {
            repository.insert(p, id -> runOnUiThread(() -> {
                Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
                finish();
            }));
        }
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar este pedido?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repository.delete(pedidoExistente, () -> runOnUiThread(() -> {
                        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show();
                        finish();
                    }));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}