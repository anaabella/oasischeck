package com.oasischeck.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.VentaDao;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private VentaDao ventaDao;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ventaDao = AppDatabase.getInstance(this).ventaDao();
        cargarDatos();
    }

    private void cargarDatos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Integer unidades = ventaDao.getTotalUnidadesVendidas();
            Double totalVentas = ventaDao.getTotalVentas();

            Double totalFeria = ventaDao.getTotalPorLugar("Feria");
            Double totalVivero = ventaDao.getTotalPorLugar("Vivero");
            Double totalQuiniela = ventaDao.getTotalPorLugar("Quiniela");
            Double totalLibreria = ventaDao.getTotalPorLugar("Libreria");
            Double totalEntrega = ventaDao.getTotalPorLugar("Entrega");

            Double totalPlantas = ventaDao.getTotalPorCategoria("Plantas");
            Double totalStickers = ventaDao.getTotalPorCategoria("Stickers");
            Double totalSemillas = ventaDao.getTotalPorCategoria("Semillas");

            runOnUiThread(() -> {
                TextView tvUnidades = findViewById(R.id.tv_unidades);
                TextView tvTotalVentas = findViewById(R.id.tv_total_ventas);

                tvUnidades.setText(String.valueOf(unidades != null ? unidades : 0));
                tvTotalVentas.setText(currencyFormat.format(totalVentas != null ? totalVentas : 0));

                setupChartLugares(totalFeria, totalVivero, totalQuiniela, totalLibreria, totalEntrega);
                setupChartCategorias(totalPlantas, totalStickers, totalSemillas);
            });
        });
    }

    private void setupChartLugares(Double feria, Double vivero, Double quiniela, Double libreria, Double entrega) {
        BarChart chart = findViewById(R.id.chart_lugares);

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, feria != null ? feria.floatValue() : 0));
        entries.add(new BarEntry(1, vivero != null ? vivero.floatValue() : 0));
        entries.add(new BarEntry(2, quiniela != null ? quiniela.floatValue() : 0));
        entries.add(new BarEntry(3, libreria != null ? libreria.floatValue() : 0));
        entries.add(new BarEntry(4, entrega != null ? entrega.floatValue() : 0));

        BarDataSet dataSet = new BarDataSet(entries, "Ventas por lugar");
        dataSet.setColors(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#009688")
        );
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        chart.setData(barData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Feria", "Vivero", "Quiniela", "Libreria", "Entrega"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(11f);

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setFitBars(true);
        chart.invalidate();
    }

    private void setupChartCategorias(Double plantas, Double stickers, Double semillas) {
        PieChart chart = findViewById(R.id.chart_categorias);

        List<PieEntry> entries = new ArrayList<>();
        if (plantas != null && plantas > 0) entries.add(new PieEntry(plantas.floatValue(), "Plantas"));
        if (stickers != null && stickers > 0) entries.add(new PieEntry(stickers.floatValue(), "Stickers"));
        if (semillas != null && semillas > 0) entries.add(new PieEntry(semillas.floatValue(), "Semillas"));

        if (entries.isEmpty()) {
            chart.setNoDataText("Sin datos");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0")
        );
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        chart.setData(new PieData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(true);
        chart.setEntryLabelTextSize(12f);
        chart.setEntryLabelColor(Color.WHITE);
        chart.animateY(1000);
        chart.invalidate();
    }
}
