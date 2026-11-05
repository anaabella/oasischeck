package com.oasischeck.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.oasischeck.R;
import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.ui.balance.BalanceActivity;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

public class WidgetBalance extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    static void updateWidget(Context context, AppWidgetManager mgr, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_balance);

        Intent intent = new Intent(context, BalanceActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);

        final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Double ventas = db.ventaDao().getTotalVentas();
            Double inversiones = db.ventaDao().getTotalInversiones();
            Double gastos = db.gastoDao().getTotalGeneral();

            double v = ventas != null ? ventas : 0;
            double i = inversiones != null ? inversiones : 0;
            double g = gastos != null ? gastos : 0;

            views.setTextViewText(R.id.tv_ventas, "Ventas: " + nf.format(v));
            views.setTextViewText(R.id.tv_gastos, "Gastos: " + nf.format(g));
            views.setTextViewText(R.id.tv_neto, nf.format(v - i - g));
            mgr.updateAppWidget(id, views);
        });
    }
}
