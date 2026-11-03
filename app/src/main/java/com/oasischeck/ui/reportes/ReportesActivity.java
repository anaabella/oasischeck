package com.oasischeck.ui.reportes;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.db.GastoDao;
import com.oasischeck.data.db.VentaDao;
import com.oasischeck.data.model.Gasto;
import com.oasischeck.data.model.Venta;
import com.oasischeck.util.Periodo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReportesActivity extends AppCompatActivity {

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    private Periodo periodoActual = Periodo.todo();

    private double totalVentas;
    private double totalGastos;
    private double emprendimiento;
    private double ana;
    private double clau;
    private double wish;
    private double estadoEmprendimiento;
    private double estadoRetirado;
    private double estadoOasis;
    private List<VentaDao.PlantaStats> topPlantas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btn_exportar_csv).setOnClickListener(v -> exportarCsv());
        findViewById(R.id.btn_exportar_pdf).setOnClickListener(v -> exportarPdf());
        findViewById(R.id.btn_periodo).setOnClickListener(v -> mostrarSelectorPeriodo());

        actualizarBotonPeriodo();
        cargarDatos();
    }

    private void mostrarSelectorPeriodo() {
        String[] opciones = {"Hoy", "Esta semana", "Este mes", "Rango personalizado", "Todo"};
        new AlertDialog.Builder(this)
                .setTitle("Período del reporte")
                .setItems(opciones, (dialog, which) -> {
                    Calendar hoy = Calendar.getInstance();
                    switch (which) {
                        case 0:
                            periodoActual = Periodo.hoy(hoy);
                            break;
                        case 1:
                            periodoActual = Periodo.semana(hoy);
                            break;
                        case 2:
                            periodoActual = Periodo.mes(hoy);
                            break;
                        case 3:
                            elegirRangoPersonalizado(hoy);
                            return;
                        default:
                            periodoActual = Periodo.todo();
                            break;
                    }
                    actualizarBotonPeriodo();
                    cargarDatos();
                })
                .show();
    }

    private void elegirRangoPersonalizado(Calendar referencia) {
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar inicio = Calendar.getInstance();
            inicio.set(year, month, day, 0, 0, 0);
            inicio.set(Calendar.MILLISECOND, 0);

            new DatePickerDialog(this, (view2, year2, month2, day2) -> {
                Calendar fin = Calendar.getInstance();
                fin.set(year2, month2, day2, 23, 59, 59);
                fin.set(Calendar.MILLISECOND, 999);
                if (fin.getTimeInMillis() < inicio.getTimeInMillis()) {
                    Toast.makeText(this, "La fecha final no puede ser anterior a la inicial",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                periodoActual = Periodo.personalizado(inicio.getTimeInMillis(), fin.getTimeInMillis());
                actualizarBotonPeriodo();
                cargarDatos();
            }, year, month, day).show();
        }, referencia.get(Calendar.YEAR), referencia.get(Calendar.MONTH),
                referencia.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void actualizarBotonPeriodo() {
        MaterialButton boton = findViewById(R.id.btn_periodo);
        if (periodoActual.todo) {
            boton.setText("Todo");
        } else if ("Personalizado".equals(periodoActual.etiqueta)) {
            boton.setText(dateFormat.format(new Date(periodoActual.inicio)) + " - " +
                    dateFormat.format(new Date(periodoActual.fin)));
        } else {
            boton.setText(periodoActual.etiqueta);
        }
    }

    private void cargarDatos() {
        final Periodo periodo = periodoActual;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            VentaDao ventaDao = db.ventaDao();
            GastoDao gastoDao = db.gastoDao();

            List<VentaDao.PlantaStats> top;
            double ventas;
            double gastos;
            Double emp;
            Double ret;
            Double oas;

            if (periodo.todo) {
                top = ventaDao.getTopPlantas(10);
                Double v = ventaDao.getTotalVentas();
                ventas = v != null ? v : 0;
                Double g = gastoDao.getTotalGeneral();
                gastos = g != null ? g : 0;
                emp = ventaDao.getTotalPorEstadoDinero("Emprendimiento");
                ret = ventaDao.getTotalPorEstadoDinero("Retirado");
                oas = ventaDao.getTotalPorEstadoDinero("Dejado en Oasis");
            } else {
                top = ventaDao.getTopPlantasRango(periodo.inicio, periodo.fin);
                Double v = ventaDao.getTotalRango(periodo.inicio, periodo.fin);
                ventas = v != null ? v : 0;
                Double g = gastoDao.getTotalRango(periodo.inicio, periodo.fin);
                gastos = g != null ? g : 0;
                emp = ventaDao.getTotalPorEstadoDineroRango("Emprendimiento", periodo.inicio, periodo.fin);
                ret = ventaDao.getTotalPorEstadoDineroRango("Retirado", periodo.inicio, periodo.fin);
                oas = ventaDao.getTotalPorEstadoDineroRango("Dejado en Oasis", periodo.inicio, periodo.fin);
            }

            Periodo anterior = periodo.anterior();
            double ventasAnterior = 0;
            double gastosAnterior = 0;
            if (anterior != null) {
                Double v = ventaDao.getTotalRango(anterior.inicio, anterior.fin);
                ventasAnterior = v != null ? v : 0;
                Double g = gastoDao.getTotalRango(anterior.inicio, anterior.fin);
                gastosAnterior = g != null ? g : 0;
            }

            final double ventasFinal = ventas;
            final double gastosFinal = gastos;
            final double ventasAnt = ventasAnterior;
            final double gastosAnt = gastosAnterior;

            runOnUiThread(() -> {
                topPlantas = top;
                totalVentas = ventasFinal;
                totalGastos = gastosFinal;
                emprendimiento = ventasFinal * 0.50;
                ana = ventasFinal * 0.25;
                clau = ventasFinal * 0.25;
                wish = ventasFinal * 0.10;
                estadoEmprendimiento = emp != null ? emp : 0;
                estadoRetirado = ret != null ? ret : 0;
                estadoOasis = oas != null ? oas : 0;

                RecyclerView recycler = findViewById(R.id.recycler_top_plantas);
                recycler.setLayoutManager(new LinearLayoutManager(this));
                recycler.setAdapter(new TopPlantasAdapter(top));

                ((TextView) findViewById(R.id.tv_div_ana)).setText("Ana (25%): " + currencyFormat.format(ana));
                ((TextView) findViewById(R.id.tv_div_clau)).setText("Clau (25%): " + currencyFormat.format(clau));
                ((TextView) findViewById(R.id.tv_div_wish)).setText("Fondo Wishlist (10%): " + currencyFormat.format(wish));
                ((TextView) findViewById(R.id.tv_div_emprendimiento)).setText("Emprendimiento (50%): " + currencyFormat.format(emprendimiento));

                ((TextView) findViewById(R.id.tv_estado_emprendimiento)).setText("Emprendimiento: " + currencyFormat.format(estadoEmprendimiento));
                ((TextView) findViewById(R.id.tv_estado_retirado)).setText("Retirado: " + currencyFormat.format(estadoRetirado));
                ((TextView) findViewById(R.id.tv_estado_oasis)).setText("Dejado en Oasis: " + currencyFormat.format(estadoOasis));

                TextView tvPeriodo = findViewById(R.id.tv_periodo_actual);
                TextView tvVentas = findViewById(R.id.tv_comp_ventas);
                TextView tvGastos = findViewById(R.id.tv_comp_gastos);
                TextView tvNeto = findViewById(R.id.tv_comp_neto);

                if (anterior == null) {
                    tvPeriodo.setText("Histórico completo (sin comparación)");
                    tvVentas.setText("Ventas: " + currencyFormat.format(ventasFinal));
                    tvGastos.setText("Gastos: " + currencyFormat.format(gastosFinal));
                    tvNeto.setText("Neto: " + currencyFormat.format(ventasFinal - gastosFinal));
                } else {
                    tvPeriodo.setText(periodo.etiqueta + ": " +
                            dateFormat.format(new Date(periodo.inicio)) + " - " +
                            dateFormat.format(new Date(periodo.fin)));
                    tvVentas.setText("Ventas: " + currencyFormat.format(ventasFinal) +
                            "  (antes " + currencyFormat.format(ventasAnt) + ", " +
                            variacion(ventasFinal, ventasAnt) + ")");
                    tvGastos.setText("Gastos: " + currencyFormat.format(gastosFinal) +
                            "  (antes " + currencyFormat.format(gastosAnt) + ", " +
                            variacion(gastosFinal, gastosAnt) + ")");
                    double neto = ventasFinal - gastosFinal;
                    double netoAnt = ventasAnt - gastosAnt;
                    tvNeto.setText("Neto: " + currencyFormat.format(neto) +
                            "  (antes " + currencyFormat.format(netoAnt) + ", " +
                            variacion(neto, netoAnt) + ")");
                }
            });
        });
    }

    private static String variacion(double actual, double previo) {
        if (previo == 0) {
            return actual == 0 ? "sin cambios" : "nuevo";
        }
        double porcentaje = (actual - previo) / Math.abs(previo) * 100.0;
        return String.format(Locale.getDefault(), "%+.1f%%", porcentaje);
    }

    private void exportarCsv() {
        final Periodo periodo = periodoActual;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                List<Venta> ventas = periodo.todo
                        ? db.ventaDao().getAll()
                        : db.ventaDao().getByFechaRango(periodo.inicio, periodo.fin);
                List<Gasto> gastos = periodo.todo
                        ? db.gastoDao().getAll()
                        : db.gastoDao().getByFechaRango(periodo.inicio, periodo.fin);

                File file = new File(getExternalFilesDir(null), "reporte_oasischeck.csv");
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("Período," + csv(descripcionPeriodo(periodo)) + "\n\n");
                    writer.write("Fecha,Tipo,Lugar/Categoria,Detalle,Cantidad,Monto\n");
                    for (Venta v : ventas) {
                        writer.write(csv(dateFormat.format(new Date(v.fecha))) + "," +
                                csv("Venta") + "," +
                                csv(safe(v.lugar) + " / " + safe(v.categoria)) + "," +
                                csv(safe(v.nombrePlanta) + " " + safe(v.infoVenta)) + "," +
                                v.cantidad + "," +
                                v.total + "\n");
                    }
                    for (Gasto g : gastos) {
                        writer.write(csv(dateFormat.format(new Date(g.fecha))) + "," +
                                csv("Gasto") + "," +
                                csv(safe(g.tipo)) + "," +
                                csv(safe(g.concepto)) + ",," +
                                g.monto + "\n");
                    }
                    writer.write("\n");
                    writer.write("Resumen,,\n");
                    writer.write("Total ventas,," + totalVentas + "\n");
                    writer.write("Total gastos,," + totalGastos + "\n");
                    writer.write("Ganancia neta,," + (totalVentas - totalGastos) + "\n");
                    writer.write("Ana 25%,," + ana + "\n");
                    writer.write("Clau 25%,," + clau + "\n");
                    writer.write("Emprendimiento 50%,," + emprendimiento + "\n");
                    writer.write("Fondo wishlist 10%,," + wish + "\n");
                }

                compartirArchivo(file, "text/csv", "Compartir reporte CSV");
                runOnUiThread(() -> Toast.makeText(this, "CSV generado", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al generar CSV: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void exportarPdf() {
        final Periodo periodo = periodoActual;
        Executors.newSingleThreadExecutor().execute(() -> {
            PdfDocument documento = new PdfDocument();
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                List<Venta> ventas = periodo.todo
                        ? db.ventaDao().getAll()
                        : db.ventaDao().getByFechaRango(periodo.inicio, periodo.fin);
                List<Gasto> gastos = periodo.todo
                        ? db.gastoDao().getAll()
                        : db.gastoDao().getByFechaRango(periodo.inicio, periodo.fin);

                int ancho = 595;
                int alto = 842;
                int margen = 40;
                int y = margen + 10;

                Paint titulo = new Paint();
                titulo.setAntiAlias(true);
                titulo.setTextSize(20f);
                titulo.setFakeBoldText(true);

                Paint subtitulo = new Paint();
                subtitulo.setAntiAlias(true);
                subtitulo.setTextSize(13f);
                subtitulo.setFakeBoldText(true);

                Paint texto = new Paint();
                texto.setAntiAlias(true);
                texto.setTextSize(11f);

                PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(ancho, alto, 1).create();
                PdfDocument.Page pagina = documento.startPage(info);
                Canvas canvas = pagina.getCanvas();

                canvas.drawText("OasisCheck - Reporte", margen, y, titulo);
                y += 24;
                canvas.drawText("Período: " + descripcionPeriodo(periodo), margen, y, texto);
                y += 16;
                canvas.drawText("Generado: " + dateFormat.format(new Date()), margen, y, texto);
                y += 28;

                canvas.drawText("Resumen", margen, y, subtitulo);
                y += 18;
                y = dibujarLinea(canvas, margen, y, "Total ventas: " + currencyFormat.format(totalVentas), texto);
                y = dibujarLinea(canvas, margen, y, "Total gastos: " + currencyFormat.format(totalGastos), texto);
                y = dibujarLinea(canvas, margen, y, "Ganancia neta: " + currencyFormat.format(totalVentas - totalGastos), texto);
                y += 12;

                canvas.drawText("Division de ganancias", margen, y, subtitulo);
                y += 18;
                y = dibujarLinea(canvas, margen, y, "Ana (25%): " + currencyFormat.format(ana), texto);
                y = dibujarLinea(canvas, margen, y, "Clau (25%): " + currencyFormat.format(clau), texto);
                y = dibujarLinea(canvas, margen, y, "Emprendimiento (50%): " + currencyFormat.format(emprendimiento), texto);
                y = dibujarLinea(canvas, margen, y, "Fondo wishlist (10%): " + currencyFormat.format(wish), texto);
                y += 12;

                canvas.drawText("Estado del dinero", margen, y, subtitulo);
                y += 18;
                y = dibujarLinea(canvas, margen, y, "Emprendimiento: " + currencyFormat.format(estadoEmprendimiento), texto);
                y = dibujarLinea(canvas, margen, y, "Retirado: " + currencyFormat.format(estadoRetirado), texto);
                y = dibujarLinea(canvas, margen, y, "Dejado en Oasis: " + currencyFormat.format(estadoOasis), texto);
                y += 12;

                canvas.drawText("Top plantas mas vendidas", margen, y, subtitulo);
                y += 18;
                int limite = Math.min(topPlantas.size(), 10);
                for (int i = 0; i < limite && y < alto - margen; i++) {
                    VentaDao.PlantaStats p = topPlantas.get(i);
                    y = dibujarLinea(canvas, margen, y,
                            (i + 1) + ". " + safe(p.nombrePlanta) + " - " + p.cant + " u. - " + currencyFormat.format(p.tot), texto);
                }

                y += 16;
                if (y < alto - margen - 20) {
                    canvas.drawText("Movimientos: " + ventas.size() + " ventas / " + gastos.size() + " gastos",
                            margen, y, texto);
                }

                documento.finishPage(pagina);

                File file = new File(getExternalFilesDir(null), "reporte_oasischeck.pdf");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    documento.writeTo(out);
                }

                compartirArchivo(file, "application/pdf", "Compartir reporte PDF");
                runOnUiThread(() -> Toast.makeText(this, "PDF generado", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                documento.close();
            }
        });
    }

    private String descripcionPeriodo(Periodo periodo) {
        if (periodo.todo) return "Histórico completo";
        return periodo.etiqueta + " (" + dateFormat.format(new Date(periodo.inicio)) + " - " +
                dateFormat.format(new Date(periodo.fin)) + ")";
    }

    private int dibujarLinea(Canvas canvas, int x, int y, String texto, Paint paint) {
        canvas.drawText(texto, x + 8, y, paint);
        return y + 16;
    }

    private void compartirArchivo(File file, String mime, String titulo) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        runOnUiThread(() -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mime);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, titulo));
        });
    }

    private static String csv(String valor) {
        String v = valor != null ? valor : "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    private static String safe(String valor) {
        return valor != null ? valor : "";
    }
}
