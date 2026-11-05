package com.oasischeck.ui.cierre;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Cuenta;
import com.oasischeck.data.model.Venta;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CierreCajaActivity extends AppCompatActivity {

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat fechaLarga = new SimpleDateFormat("EEEE d/M/yyyy", new Locale("es", "AR"));

    private double ventas;
    private double gastos;
    private double neto;
    private int unidades;
    private int cantidadVentas;
    private double porCobrar;
    private double porPagar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cierre_caja);



        findViewById(R.id.btn_compartir_cierre).setOnClickListener(v -> compartir());
        cargar();
    }

    private void cargar() {
        Calendar inicio = Calendar.getInstance();
        inicio.set(Calendar.HOUR_OF_DAY, 0);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        inicio.set(Calendar.MILLISECOND, 0);
        long desde = inicio.getTimeInMillis();

        Calendar fin = Calendar.getInstance();
        fin.set(Calendar.HOUR_OF_DAY, 23);
        fin.set(Calendar.MINUTE, 59);
        fin.set(Calendar.SECOND, 59);
        fin.set(Calendar.MILLISECOND, 999);
        long hasta = fin.getTimeInMillis();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            Double v = db.ventaDao().getTotalRango(desde, hasta);
            Integer u = db.ventaDao().getUnidadesRango(desde, hasta);
            Double g = db.gastoDao().getTotalRango(desde, hasta);
            List<Venta> ventasHoy = db.ventaDao().getByFechaRango(desde, hasta);
            Double c = db.cuentaDao().getTotalPendiente(Cuenta.TIPO_COBRAR);
            Double p = db.cuentaDao().getTotalPendiente(Cuenta.TIPO_PAGAR);

            double ventasVal = v != null ? v : 0;
            double gastosVal = g != null ? g : 0;
            int unidadesVal = u != null ? u : 0;

            runOnUiThread(() -> {
                ventas = ventasVal;
                gastos = gastosVal;
                neto = ventasVal - gastosVal;
                unidades = unidadesVal;
                cantidadVentas = ventasHoy.size();
                porCobrar = c != null ? c : 0;
                porPagar = p != null ? p : 0;

                ((TextView) findViewById(R.id.tv_fecha_cierre)).setText("Cierre del " + fechaLarga.format(new Date()));
                ((TextView) findViewById(R.id.tv_neto_dia)).setText(currencyFormat.format(neto));
                ((TextView) findViewById(R.id.tv_cierre_ventas)).setText("Ventas: " + currencyFormat.format(ventas) + " (" + cantidadVentas + " operaciones)");
                ((TextView) findViewById(R.id.tv_cierre_unidades)).setText("Unidades vendidas: " + unidades);
                ((TextView) findViewById(R.id.tv_cierre_gastos)).setText("Gastos: " + currencyFormat.format(gastos));
                ((TextView) findViewById(R.id.tv_cierre_neto)).setText("Neto: " + currencyFormat.format(neto));
                ((TextView) findViewById(R.id.tv_cierre_cobrar)).setText("Por cobrar pendiente: " + currencyFormat.format(porCobrar));
                ((TextView) findViewById(R.id.tv_cierre_pagar)).setText("Por pagar pendiente: " + currencyFormat.format(porPagar));
            });
        });
    }

    private void compartir() {
        String resumen = "Cierre de caja " + fechaLarga.format(new Date()) + "\n" +
                "━━━━━━━━━━━━━━━━\n" +
                "Ventas: " + currencyFormat.format(ventas) + " (" + cantidadVentas + " operaciones)\n" +
                "Unidades: " + unidades + "\n" +
                "Gastos: " + currencyFormat.format(gastos) + "\n" +
                "Neto: " + currencyFormat.format(neto) + "\n" +
                "━━━━━━━━━━━━━━━━\n" +
                "Por cobrar: " + currencyFormat.format(porCobrar) + "\n" +
                "Por pagar: " + currencyFormat.format(porPagar) + "\n" +
                "OasisCheck";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, resumen);
        startActivity(Intent.createChooser(shareIntent, "Compartir cierre"));
    }
}
