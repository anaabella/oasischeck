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

public class WidgetGastosMes extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    static void updateWidget(Context context, AppWidgetManager mgr, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_gastos_mes);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicio = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long fin = cal.getTimeInMillis();

        final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Double viaje = db.gastoDao().getTotalTipoRango("Viaje", inicio, fin);
            Double fijo = db.gastoDao().getTotalTipoRango("Fijo", inicio, fin);
            Double total = db.gastoDao().getTotalRango(inicio, fin);

            double v = viaje != null ? viaje : 0;
            double f = fijo != null ? fijo : 0;
            double t = total != null ? total : 0;
            String mes = new java.text.SimpleDateFormat("MMMM", Locale.getDefault()).format(new java.util.Date());

            views.setTextViewText(R.id.tv_total, nf.format(t));
            views.setTextViewText(R.id.tv_viaje, "Viaje: " + nf.format(v));
            views.setTextViewText(R.id.tv_fijos, "Fijos: " + nf.format(f));
            views.setTextViewText(R.id.tv_mes, mes.substring(0, 1).toUpperCase() + mes.substring(1));
            mgr.updateAppWidget(id, views);
        });
    }
}