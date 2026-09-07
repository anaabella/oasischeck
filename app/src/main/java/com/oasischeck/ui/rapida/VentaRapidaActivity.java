package com.oasischeck.ui.rapida;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.oasischeck.R;
import com.oasischeck.data.model.Venta;
import com.oasischeck.data.repository.VentaRepository;
import com.oasischeck.sheets.GoogleSheetsService;

public class VentaRapidaActivity extends AppCompatActivity {

    private TextInputEditText etPlanta, etPrecio, etInfo;
    private AutoCompleteTextView spinnerLugar, spinnerDuenia;
    private VentaRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta_rapida);

        repository = new VentaRepository(getApplication());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etPlanta = findViewById(R.id.et_planta);
        etPrecio = findViewById(R.id.et_precio);
        etInfo = findViewById(R.id.et_info);
        spinnerLugar = findViewById(R.id.spinner_lugar);
        spinnerDuenia = findViewById(R.id.spinner_duenia);

        spinnerLugar.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1_line,
                com.oasischeck.util.AppConstants.LUGARES));
        spinnerDuenia.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1_line,
                com.oasischeck.util.AppConstants.DUENIAS));

        findViewById(R.id.btn_guardar).setOnClickListener(v -> guardar());
    }

    private void guardar() {
        if (etPlanta.getText() == null || etPlanta.getText().toString().trim().isEmpty()) {
            etPlanta.setError("Requerido");
            return;
        }
        if (etPrecio.getText() == null || etPrecio.getText().toString().trim().isEmpty()) {
            etPrecio.setError("Requerido");
            return;
        }
        if (spinnerLugar.getText().toString().isEmpty()) {
            spinnerLugar.setError("Requerido");
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(etPrecio.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresá un precio válido", Toast.LENGTH_SHORT).show();
            return;
        }

        Venta v = new Venta(
                System.currentTimeMillis(),
                1,
                precio,
                spinnerLugar.getText().toString(),
                "Plantas",
                etInfo.getText() != null ? etInfo.getText().toString() : ""
        );
        v.nombrePlanta = etPlanta.getText().toString().trim();
        v.duenia = spinnerDuenia.getText().toString();
        v.estadoDinero = Venta.ESTADO_EMPRENDIMIENTO;

        repository.insert(v, id -> {
            v.id = id;
            GoogleSheetsService.sincronizarVenta(this, v);
            runOnUiThread(() -> {
                Toast.makeText(this, "Venta guardada", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}