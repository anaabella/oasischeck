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

import java.util.List;
import java.util.concurrent.Executors;

public class WidgetWishlist extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    static void updateWidget(Context context, AppWidgetManager mgr, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_wishlist);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<com.oasischeck.data.model.WishPlanta> pendientes = db.wishPlantaDao().getPendientes();

            StringBuilder sb = new StringBuilder();
            int count = Math.min(pendientes.size(), 3);
            for (int i = 0; i < count; i++) {
                sb.append("• ").append(pendientes.get(i).nombre);
                if (i < count - 1) sb.append("\n");
            }
            if (pendientes.size() > 3) sb.append("\n...+" + (pendientes.size() - 3) + " más");

            views.setTextViewText(R.id.tv_count, "Wishlist (" + pendientes.size() + " pend.)");
            views.setTextViewText(R.id.tv_lista, pendientes.isEmpty() ? "Vacía" : sb.toString());
            mgr.updateAppWidget(id, views);
        });
    }
}