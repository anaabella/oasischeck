package com.oasischeck.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.sheets.GoogleSheetsService;
import com.oasischeck.ui.agregar.AgregarVentaActivity;
import com.oasischeck.ui.balance.BalanceActivity;
import com.oasischeck.ui.gastos.AgregarGastoActivity;
import com.oasischeck.ui.gastos.GastosFragment;
import com.oasischeck.ui.lista.ListaVentasFragment;
import com.oasischeck.ui.pedidos.AgregarPedidoActivity;
import com.oasischeck.ui.pedidos.PedidosFragment;
import com.oasischeck.ui.planificadas.AgregarPlannedVentaActivity;
import com.oasischeck.ui.planificadas.PlannedVentasFragment;
import com.oasischeck.ui.rapida.VentaRapidaActivity;
import com.oasischeck.ui.export.ExportActivity;
import com.oasischeck.ui.reportes.ReportesActivity;
import com.oasischeck.ui.wishlist.AgregarWishActivity;
import com.oasischeck.ui.wishlist.WishlistFragment;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequestBuilder;
import androidx.work.WorkManager;

import com.oasischeck.worker.RecurringGastoWorker;
import com.oasischeck.worker.SyncRetryWorker;

import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_SHEETS = "oasischeck_sheets";
    private static final String KEY_WEBHOOK = "sheets_webhook_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        SharedPreferences prefs = getSharedPreferences(PREFS_SHEETS, MODE_PRIVATE);
        if (prefs.getString(KEY_WEBHOOK, "").isEmpty()) {
            GoogleSheetsService.configurarWebhook(this,
                "https://script.google.com/macros/s/AKfycbznIbSVphiZYwtbWEqq3bX75d9Zbic_CZYIL_YCx7LxSlF8W2po1JjEQP9AJpJOX8TbCw/exec");
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        FloatingActionButton fab = findViewById(R.id.fab_agregar);

        if (savedInstanceState == null) loadFragment(new ListaVentasFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_ventas) {
                fragment = new ListaVentasFragment();
                fab.show();
                fab.setOnClickListener(v -> startActivity(new Intent(this, AgregarVentaActivity.class)));
            } else if (id == R.id.nav_planificadas) {
                fragment = new PlannedVentasFragment();
                fab.show();
                fab.setOnClickListener(v -> startActivity(new Intent(this, AgregarPlannedVentaActivity.class)));
            } else if (id == R.id.nav_pedidos) {
                fragment = new PedidosFragment();
                fab.show();
                fab.setOnClickListener(v -> startActivity(new Intent(this, AgregarPedidoActivity.class)));
            } else if (id == R.id.nav_wishlist) {
                fragment = new WishlistFragment();
                fab.show();
                fab.setOnClickListener(v -> startActivity(new Intent(this, AgregarWishActivity.class)));
            } else if (id == R.id.nav_gastos) {
                fragment = new GastosFragment();
                fab.show();
                fab.setOnClickListener(v -> startActivity(new Intent(this, AgregarGastoActivity.class)));
            } else if (id == R.id.nav_balance) {
                startActivity(new Intent(this, BalanceActivity.class));
                fab.hide();
                return false;
            }
            if (fragment != null) loadFragment(fragment);
            return true;
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, AgregarVentaActivity.class)));

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_gastos", ExistingPeriodicWorkPolicy.KEEP,
            new PeriodicWorkRequestBuilder<RecurringGastoWorker>(1, TimeUnit.DAYS).build()
        );

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "retry_sync", ExistingPeriodicWorkPolicy.KEEP,
            new PeriodicWorkRequestBuilder<SyncRetryWorker>(30, TimeUnit.MINUTES).build()
        );
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_configurar_sheets) {
            mostrarDialogoConfigSheets();
            return true;
        } else if (id == R.id.action_ventas_rapidas) {
            startActivity(new Intent(this, VentaRapidaActivity.class));
            return true;
        } else if (id == R.id.action_reportes) {
            startActivity(new Intent(this, ReportesActivity.class));
            return true;
        } else if (id == R.id.action_share) {
            compartirResumen();
            return true;
        } else if (id == R.id.action_export_import) {
            startActivity(new Intent(this, ExportActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoConfigSheets() {
        SharedPreferences prefs = getSharedPreferences(PREFS_SHEETS, MODE_PRIVATE);
        EditText input = new EditText(this);
        input.setText(prefs.getString(KEY_WEBHOOK, ""));
        new AlertDialog.Builder(this)
                .setTitle("Configurar Google Sheets")
                .setView(input)
                .setPositiveButton("Guardar", (d, w) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        prefs.edit().putString(KEY_WEBHOOK, url).apply();
                        Toast.makeText(this, "URL guardada", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void compartirResumen() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Double totalVentas = db.ventaDao().getTotalVentas();
            Integer totalUnidades = db.ventaDao().getTotalUnidadesVendidas();
            Double totalGastos = db.gastoDao().getTotalGeneral();
            double ventas = totalVentas != null ? totalVentas : 0;
            double gastos = totalGastos != null ? totalGastos : 0;
            int unidades = totalUnidades != null ? totalUnidades : 0;
            String fecha = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());

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
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}