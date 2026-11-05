package com.oasischeck.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.oasischeck.R;
import com.oasischeck.ui.balance.BalanceActivity;
import com.oasischeck.ui.cierre.CierreCajaActivity;
import com.oasischeck.ui.export.ExportActivity;
import com.oasischeck.ui.reportes.ReportesActivity;

public class ConfiguracionActivity extends AppCompatActivity {

    private static final String KEY_TEMA_OSCURO = "tema_oscuro";

    private boolean esModoOscuro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Cargar preferencia de tema
        SharedPreferences prefs = getSharedPreferences("oasischeck_prefs", MODE_PRIVATE);
        esModoOscuro = prefs.getBoolean(KEY_TEMA_OSCURO, true);
        aplicarTema(esModoOscuro);

        Button btnTema = findViewById(R.id.btn_config_tema);
        btnTema.setText(esModoOscuro ? "Cambiar a Modo Claro (Día)" : "Cambiar a Modo Oscuro (Noche)");

        // Opciones del menú anterior (tres puntos) - solo las que no están en el navbar

        Button btnBalance = findViewById(R.id.btn_config_balance);
        btnBalance.setOnClickListener(v -> {
            startActivity(new Intent(this, BalanceActivity.class));
        });

        Button btnCierre = findViewById(R.id.btn_config_cierre);
        btnCierre.setOnClickListener(v -> {
            startActivity(new Intent(this, CierreCajaActivity.class));
        });

        Button btnSheets = findViewById(R.id.btn_config_sheets);
        btnSheets.setOnClickListener(v -> {
            mostrarDialogoConfigSheets();
        });

        Button btnReportes = findViewById(R.id.btn_config_reportes);
        btnReportes.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportesActivity.class));
        });

        Button btnCompartir = findViewById(R.id.btn_config_compartir);
        btnCompartir.setOnClickListener(v -> {
            compartirResumen();
        });

        Button btnExport = findViewById(R.id.btn_config_export);
        btnExport.setOnClickListener(v -> {
            startActivity(new Intent(this, ExportActivity.class));
        });

        // Cambiar tema oscuro/claro
        btnTema.setOnClickListener(v -> {
            esModoOscuro = !esModoOscuro;
            aplicarTema(esModoOscuro);
            prefs.edit().putBoolean(KEY_TEMA_OSCURO, esModoOscuro).apply();
            btnTema.setText(esModoOscuro ? "Cambiar a Modo Claro (Día)" : "Cambiar a Modo Oscuro (Noche)");
            Toast.makeText(this, esModoOscuro ? "Modo Oscuro activado" : "Modo Claro activado", Toast.LENGTH_SHORT).show();
            recreate();
        });
    }

    private void aplicarTema(boolean oscuro) {
        AppCompatDelegate.setDefaultNightMode(
            oscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void mostrarDialogoConfigSheets() {
        android.widget.EditText input = new android.widget.EditText(this);
        SharedPreferences prefs = getSharedPreferences("oasischeck_sheets", MODE_PRIVATE);
        input.setText(prefs.getString("sheets_webhook_url", ""));
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Configurar Google Sheets")
                .setView(input)
                .setPositiveButton("Guardar", (d, w) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        prefs.edit().putString("sheets_webhook_url", url).apply();
                        Toast.makeText(this, "URL guardada", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void compartirResumen() {
        new Thread(() -> {
            com.oasischeck.data.db.AppDatabase db = com.oasischeck.data.db.AppDatabase.getInstance(this);
            Double totalVentas = db.ventaDao().getTotalVentas();
            Integer totalUnidades = db.ventaDao().getTotalUnidadesVendidas();
            Double totalGastos = db.gastoDao().getTotalGeneral();
            double ventas = totalVentas != null ? totalVentas : 0;
            double gastos = totalGastos != null ? totalGastos : 0;
            int unidades = totalUnidades != null ? totalUnidades : 0;
            String fecha = new java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

            String resumen = "Resumen " + fecha + "\n" +
                    "━━━━━━━━━━━━━━━━\n" +
                    "Ventas: $" + String.format("%,.0f", ventas) + " (" + unidades + " ventas)\n" +
                    "Gastos: $" + String.format("%,.0f", gastos) + "\n" +
                    "Ganancia: $" + String.format("%,.0f", ventas - gastos) + "\n" +
                    "━━━━━━━━━━━━━━━━\n" +
                    "OasisCheck";

            runOnUiThread(() -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, resumen);
                startActivity(Intent.createChooser(shareIntent, "Compartir resumen"));
            });
        }).start();
    }
}