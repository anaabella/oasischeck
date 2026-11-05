package com.oasischeck.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Pedido;
import com.oasischeck.data.model.Venta;
import com.oasischeck.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class WidgetCalendario extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    static void updateWidget(Context context, AppWidgetManager mgr, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_calendario);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);

        Calendar mes = Calendar.getInstance();
        mes.set(Calendar.DAY_OF_MONTH, 1);
        mes.set(Calendar.HOUR_OF_DAY, 0);
        mes.set(Calendar.MINUTE, 0);
        mes.set(Calendar.SECOND, 0);
        mes.set(Calendar.MILLISECOND, 0);
        long inicio = mes.getTimeInMillis();
        mes.add(Calendar.MONTH, 1);
        long fin = mes.getTimeInMillis() - 1;

        String tituloMes = new SimpleDateFormat("MMMM yyyy", new Locale("es", "AR"))
                .format(new java.util.Date(inicio));
        views.setTextViewText(R.id.tv_mes, capitalizar(tituloMes));

        Calendar hoy = Calendar.getInstance();
        String[][] calendario = construirCalendario(hoy);
        views.setTextViewText(R.id.tv_calendario, calendario[0][0]);
        views.setTextViewText(R.id.tv_eventos, calendario[0][1]);

        final String mesTitulo = capitalizar(tituloMes);
        Executors.newSingleThreadExecutor().execute(() -> {
            int ventas = 0;
            int pedidos = 0;
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                List<Venta> listaVentas = db.ventaDao().getByFechaRango(inicio, fin);
                for (Venta v : listaVentas) {
                    if (v.categoria == null || !"Inversion".equalsIgnoreCase(v.categoria)) {
                        ventas++;
                    }
                }
                List<Pedido> listaPedidos = db.pedidoDao().getByFechaRango(inicio, fin);
                pedidos = listaPedidos.size();
            } catch (Exception ignored) {
            }

            String eventos = "Hoy " + hoy.get(Calendar.DAY_OF_MONTH) + "/" + (hoy.get(Calendar.MONTH) + 1)
                    + " · " + ventas + " venta" + (ventas == 1 ? "" : "s")
                    + " · " + pedidos + " pedido" + (pedidos == 1 ? "" : "s");
            views.setTextViewText(R.id.tv_eventos, eventos);
            mgr.updateAppWidget(id, views);
        });
    }

    private static String[][] construirCalendario(Calendar hoy) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int diaSemana = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (diaSemana + 5) % 7; // lunes = 0

        StringBuilder sb = new StringBuilder();
        int celda = 0;
        for (int i = 0; i < offset; i++) {
            sb.append("   ");
            celda++;
        }
        for (int dia = 1; dia <= diasEnMes; dia++) {
            sb.append(String.format(Locale.getDefault(), "%2d ", dia));
            celda++;
            if (celda % 7 == 0) {
                sb.append("\n");
            }
        }

        String resumen = "Hoy " + hoy.get(Calendar.DAY_OF_MONTH) + "/" + (hoy.get(Calendar.MONTH) + 1);
        return new String[][]{{sb.toString(), resumen}};
    }

    private static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
