package com.oasischeck.ui.configuracion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.oasischeck.R;
import com.oasischeck.ui.MainActivity;

public class ConfiguracionInicialActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "oasischeck_prefs";
    private static final String KEY_MONTO_INICIAL = "monto_inicial";
    private static final String KEY_PRIMERA_VEZ = "primera_vez";

    private EditText etMontoInicial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_inicial);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            // No permitir salir sin configurar el monto inicial
        });

        etMontoInicial = findViewById(R.id.et_monto_inicial);
        Button btnGuardar = findViewById(R.id.btn_guardar_inicial);

        btnGuardar.setOnClickListener(v -> {
            String texto = etMontoInicial.getText().toString().trim();
            if (texto.isEmpty()) {
                etMontoInicial.setError("Ingresa un monto");
                return;
            }

            try {
                double monto = Double.parseDouble(texto);
                if (monto < 0) {
                    etMontoInicial.setError("El monto no puede ser negativo");
                    return;
                }

                // Guardar el monto inicial en preferencias
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit()
                    .putLong(KEY_MONTO_INICIAL, (long) (monto * 100)) // Guardar en centavos para precisión
                    .putBoolean(KEY_PRIMERA_VEZ, false)
                    .apply();

                // Ir a MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } catch (NumberFormatException e) {
                etMontoInicial.setError("Ingresa un número válido");
            }
        });
    }

    @Override
    public void onBackPressed() {
        // No permitir salir sin configurar
    }
}