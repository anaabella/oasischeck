package com.oasischeck.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.oasischeck.R;
import com.oasischeck.ui.MainActivity;

public final class NotificationHelper {

    public static final String CHANNEL_ID = "oasischeck_recordatorios";
    private static final int NOTIFICATION_ID = 1001;

    private NotificationHelper() {}

    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        NotificationChannel canal = new NotificationChannel(
                CHANNEL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT);
        canal.setDescription("Pedidos pendientes, ventas planificadas y cuentas por vencer");
        manager.createNotificationChannel(canal);
    }

    public static void mostrarRecordatorio(Context context, int pedidos, int planificadas, int cuentas) {
        if (pedidos <= 0 && planificadas <= 0 && cuentas <= 0) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        crearCanal(context);

        StringBuilder detalle = new StringBuilder();
        if (pedidos > 0) detalle.append("• ").append(pedidos).append(" pedido(s) sin entregar\n");
        if (planificadas > 0) detalle.append("• ").append(planificadas).append(" venta(s) planificada(s) próxima(s)\n");
        if (cuentas > 0) detalle.append("• ").append(cuentas).append(" cuenta(s) por vencer o vencida(s)");

        Intent abrir = new Intent(context, MainActivity.class);
        abrir.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, abrir,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("OasisCheck - Recordatorios")
                .setContentText("Tenés tareas pendientes")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(detalle.toString().trim()))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }
}
