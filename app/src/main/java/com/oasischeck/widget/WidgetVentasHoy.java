package com.oasischeck.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.ui.MainActivity;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class WidgetVentasHoy extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    static void updateWidget(Context context, AppWidgetManager mgr, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_ventas_hoy);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicio = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long fin = cal.getTimeInMillis();

        final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Double total = db.ventaDao().getTotalRango(inicio, fin);
            Integer unidades = db.ventaDao().getUnidadesRango(inicio, fin);
            double t = total != null ? total : 0;
            int u = unidades != null ? unidades : 0;

            views.setTextViewText(R.id.tv_monto, nf.format(t));
            views.setTextViewText(R.id.tv_detalle, u + " ventas | " + new java.text.SimpleDateFormat("d/M", Locale.getDefault()).format(new java.util.Date()));
            mgr.updateAppWidget(id, views);
        });
    }
}