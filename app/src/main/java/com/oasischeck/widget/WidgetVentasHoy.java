package com.oasischeck.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.oasischeck.R;
import com.oasischeck.ui.MainActivity;
import com.oasischeck.ui.agregar.AgregarVentaActivity;

public class WidgetVentasHoy extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_ventas_hoy);

            // Botón para agregar venta rápida desde el widget
            Intent agregarIntent = new Intent(context, AgregarVentaActivity.class);
            agregarIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            android.app.PendingIntent pendingAgregar = android.app.PendingIntent.getActivity(
                    context, 0, agregarIntent,
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                            ? android.app.PendingIntent.FLAG_IMMUTABLE
                            : android.app.PendingIntent.FLAG_UPDATE_CURRENT
            );
            views.setOnClickPendingIntent(R.id.btn_agregar_venta, pendingAgregar);

            // Tocar el widget abre MainActivity
            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            android.app.PendingIntent pendingMain = android.app.PendingIntent.getActivity(
                    context, 0, mainIntent,
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                            ? android.app.PendingIntent.FLAG_IMMUTABLE
                            : android.app.PendingIntent.FLAG_UPDATE_CURRENT
            );
            views.setOnClickPendingIntent(R.id.widget_root, pendingMain);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}