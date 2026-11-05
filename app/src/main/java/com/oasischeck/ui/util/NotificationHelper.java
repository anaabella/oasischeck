package com.oasischeck.ui.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.oasischeck.R;
import com.oasischeck.ui.MainActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "oasischeck_notifications";
    private static final int NOTIF_ID_VENTAS_PENDIENTES = 1001;
    private static final int NOTIF_ID_CUENTAS_VENCIDAS = 1002;
    private static final int NOTIF_ID_CIERRE_CAJA = 1003;

    public static void inicializarCanales(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "OasisCheck Notificaciones",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            canal.setDescription("Notificaciones de ventas, cuentas y cierre de caja");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    public static void notificarVentasPendientes(Context context, int cantidad) {
        inicializarCanales(context);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("Ventas pendientes de sincronizar")
                .setContentText(cantidad + " venta(s) aún no se han sincronizado con Google Sheets")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(NOTIF_ID_VENTAS_PENDIENTES, builder.build());
    }

    public static void notificarCuentasVencidas(Context context, int cantidad) {
        inicializarCanales(context);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("Cuentas vencidas")
                .setContentText(cantidad + " cuenta(s) por cobrar/pagar han vencido")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(NOTIF_ID_CUENTAS_VENCIDAS, builder.build());
    }

    public static void notificarCierreCajaPendiente(Context context) {
        inicializarCanales(context);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("Cierre de caja pendiente")
                .setContentText("No has cerrado la caja de hoy. Revisa tus ventas y gastos.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(NOTIF_ID_CIERRE_CAJA, builder.build());
    }

    public static void cancelarNotificaciones(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }

    public static void crearCanal(Context context) {
        inicializarCanales(context);
    }
}