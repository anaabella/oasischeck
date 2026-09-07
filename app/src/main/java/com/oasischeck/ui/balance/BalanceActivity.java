package com.oasischeck.ui.balance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.VentaDao;
import com.oasischeck.ui.reportes.ReportesActivity;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

public class BalanceActivity extends AppCompatActivity {

    private VentaDao ventaDao;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ventaDao = AppDatabase.getInstance(this).ventaDao();
        cargarBalance();
    }

    private void cargarBalance() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Double totalVentas = ventaDao.getTotalVentas();
            Double totalInversiones = ventaDao.getTotalInversiones();
            Double feria = ventaDao.getTotalPorLugar("Feria");
            Double vivero = ventaDao.getTotalPorLugar("Vivero");
            Double quiniela = ventaDao.getTotalPorLugar("Quiniela");
            Double libreria = ventaDao.getTotalPorLugar("Libreria");
            Double entrega = ventaDao.getTotalPorLugar("Entrega");

            double ventas = totalVentas != null ? totalVentas : 0;
            double inversiones = totalInversiones != null ? totalInversiones : 0;
            double gananciaNeta = ventas - inversiones;

            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.tv_ganancia_neta)).setText(currencyFormat.format(gananciaNeta));
                ((TextView) findViewById(R.id.tv_ingresos)).setText(currencyFormat.format(ventas));
                ((TextView) findViewById(R.id.tv_inversiones)).setText(currencyFormat.format(inversiones));
                ((TextView) findViewById(R.id.tv_balance_feria)).setText("Feria: " + currencyFormat.format(feria != null ? feria : 0));
                ((TextView) findViewById(R.id.tv_balance_vivero)).setText("Vivero: " + currencyFormat.format(vivero != null ? vivero : 0));
                ((TextView) findViewById(R.id.tv_balance_quiniela)).setText("Quiniela: " + currencyFormat.format(quiniela != null ? quiniela : 0));
                ((TextView) findViewById(R.id.tv_balance_libreria)).setText("Librería: " + currencyFormat.format(libreria != null ? libreria : 0));
                ((TextView) findViewById(R.id.tv_balance_entrega)).setText("Entrega: " + currencyFormat.format(entrega != null ? entrega : 0));
            });
        });
    }
}