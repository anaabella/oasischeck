package com.oasischeck.ui.reportes;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.VentaDao;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReportesActivity extends AppCompatActivity {

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        cargarDatos();
    }

    private void cargarDatos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            VentaDao dao = AppDatabase.getInstance(this).ventaDao();
            List<VentaDao.PlantaStats> topPlantas = dao.getTopPlantas(10);

            Double totalVentas = dao.getTotalVentas();
            double ventas = totalVentas != null ? totalVentas : 0;
            double emprendimiento = ventas * 0.50;
            double ana = ventas * 0.25;
            double clau = ventas * 0.25;
            double wish = ventas * 0.10;

            Double divAna = dao.getTotalPorDuenia("Ana");
            Double divClau = dao.getTotalPorDuenia("Clau");

            Double emp = dao.getTotalPorEstadoDinero("Emprendimiento");
            Double ret = dao.getTotalPorEstadoDinero("Retirado");
            Double oas = dao.getTotalPorEstadoDinero("Dejado en Oasis");

            runOnUiThread(() -> {
                RecyclerView recycler = findViewById(R.id.recycler_top_plantas);
                recycler.setLayoutManager(new LinearLayoutManager(this));
                recycler.setAdapter(new TopPlantasAdapter(topPlantas));

                TextView tvAna = findViewById(R.id.tv_div_ana);
                TextView tvClau = findViewById(R.id.tv_div_clau);
                TextView tvWish = findViewById(R.id.tv_div_wish);
                TextView tvEmp = findViewById(R.id.tv_div_emprendimiento);

                tvAna.setText("Ana (25%): " + currencyFormat.format(ana));
                tvClau.setText("Clau (25%): " + currencyFormat.format(clau));
                tvWish.setText("Fondo Wishlist (10%): " + currencyFormat.format(wish));
                tvEmp.setText("Emprendimiento (50%): " + currencyFormat.format(emprendimiento));

                TextView tvEmpState = findViewById(R.id.tv_estado_emprendimiento);
                TextView tvRet = findViewById(R.id.tv_estado_retirado);
                TextView tvOas = findViewById(R.id.tv_estado_oasis);

                tvEmpState.setText("Emprendimiento: " + currencyFormat.format(emp != null ? emp : 0));
                tvRet.setText("Retirado: " + currencyFormat.format(ret != null ? ret : 0));
                tvOas.setText("Dejado en Oasis: " + currencyFormat.format(oas != null ? oas : 0));
            });
        });
    }
}