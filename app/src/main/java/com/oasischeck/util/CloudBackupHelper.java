package com.oasischeck.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

/**
 * Guarda la URI persistente del archivo de backup automático en la nube
 * (Google Drive, OneDrive, almacenamiento local, etc.).
 */
public final class CloudBackupHelper {

    private static final String PREFS = "oasischeck_prefs";
    private static final String KEY_URI = "cloud_backup_uri";
    private static final String KEY_NOMBRE = "cloud_backup_nombre";
    private static final String KEY_ULTIMO = "cloud_backup_ultimo";
    private static final String KEY_ERROR = "cloud_backup_error";

    private CloudBackupHelper() {
    }

    public static boolean estaActivo(Context context) {
        return getUri(context) != null;
    }

    public static Uri getUri(Context context) {
        String valor = prefs(context).getString(KEY_URI, "");
        return valor.isEmpty() ? null : Uri.parse(valor);
    }

    public static String getNombre(Context context) {
        return prefs(context).getString(KEY_NOMBRE, "");
    }

    public static long getUltimoExito(Context context) {
        return prefs(context).getLong(KEY_ULTIMO, 0);
    }

    public static String getError(Context context) {
        return prefs(context).getString(KEY_ERROR, "");
    }

    public static void activar(Context context, Uri uri) {
        try {
            context.getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (SecurityException e) {
            context.getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        prefs(context).edit()
                .putString(KEY_URI, uri.toString())
                .putString(KEY_NOMBRE, consultarNombre(context, uri))
                .remove(KEY_ERROR)
                .apply();
    }

    public static void desactivar(Context context) {
        Uri uri = getUri(context);
        if (uri != null) {
            try {
                context.getContentResolver().releasePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (Exception ignored) {
            }
        }
        prefs(context).edit()
                .remove(KEY_URI)
                .remove(KEY_NOMBRE)
                .remove(KEY_ERROR)
                .apply();
    }

    public static void marcarExito(Context context) {
        prefs(context).edit()
                .putLong(KEY_ULTIMO, System.currentTimeMillis())
                .remove(KEY_ERROR)
                .apply();
    }

    public static void marcarError(Context context, String mensaje) {
        prefs(context).edit()
                .putString(KEY_ERROR, mensaje != null ? mensaje : "Error desconocido")
                .apply();
    }

    private static String consultarNombre(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String nombre = cursor.getString(0);
                if (nombre != null && !nombre.isEmpty()) return nombre;
            }
        } catch (Exception ignored) {
        }
        String ultimo = uri.getLastPathSegment();
        return ultimo != null ? ultimo : uri.toString();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
