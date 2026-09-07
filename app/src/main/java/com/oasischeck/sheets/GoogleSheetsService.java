package com.oasischeck.sheets;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.oasischeck.data.db.AppDatabase;
import com.oasischeck.data.model.Venta;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Servicio para sincronizar ventas con Google Sheets.
 *
 * Para configurar:
 * 1. Crear un Google Sheet con columnas: Fecha, Cantidad, Precio Unitario, Total, Lugar, Categoria, Info
 * 2. Usar Google Apps Script como webhook (ver instrucciones abajo)
 * 3. Guardar la URL del webhook en SharedPreferences con key "sheets_webhook_url"
 *
 * Alternativa: Usar Google Sheets API con Service Account (más complejo pero más robusto)
 */
public class GoogleSheetsService {

    private static final String TAG = "GoogleSheets";
    private static final String PREFS_NAME = "oasischeck_sheets";
    private static final String KEY_WEBHOOK_URL = "sheets_webhook_url";

    public static void configurarWebhook(Context context, String webhookUrl) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_WEBHOOK_URL, webhookUrl).apply();
    }

    public static String getWebhookUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_WEBHOOK_URL, null);
    }

    public static void sincronizarVenta(Context context, Venta venta) {
        String webhookUrl = getWebhookUrl(context);
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            Log.d(TAG, "No webhook configurado, sync pendiente");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                String fecha = sdf.format(new Date(venta.fecha));

                StringBuilder postData = new StringBuilder();
                postData.append("fecha=").append(URLEncoder.encode(fecha, "UTF-8"));
                postData.append("&cantidad=").append(venta.cantidad);
                postData.append("&precio=").append(venta.precioUnitario);
                postData.append("&total=").append(venta.total);
                postData.append("&lugar=").append(URLEncoder.encode(venta.lugar, "UTF-8"));
                postData.append("&categoria=").append(URLEncoder.encode(venta.categoria, "UTF-8"));
                postData.append("&info=").append(URLEncoder.encode(
                        venta.infoVenta != null ? venta.infoVenta : "", "UTF-8"));

                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d(TAG, "Venta sincronizada OK");
                    AppDatabase.getInstance(context).ventaDao().marcarSincronizado(venta.id);
                } else {
                    Log.e(TAG, "Error sync: " + responseCode);
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error sincronizando", e);
            }
        });
    }

    /**
     * INSTRUCCIONES PARA CONFIGURAR GOOGLE SHEETS:
     *
     * 1. Crear un Google Sheet nuevo
     * 2. Ir a Extensions > Apps Script
     * 3. Pegar este código:
     *
     * function doPost(e) {
     *   var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
     *   var data = [
     *     e.parameter.fecha,
     *     e.parameter.cantidad,
     *     e.parameter.precio,
     *     e.parameter.total,
     *     e.parameter.lugar,
     *     e.parameter.categoria,
     *     e.parameter.info
     *   ];
     *   sheet.appendRow(data);
     *   return ContentService.createTextOutput("OK");
     * }
     *
     * 4. Desplegar > Nuevo despliegue > Web App
     *    - Ejecutar como: Yo
     *   - Quién tiene acceso: Cualquier persona
     * 5. Copiar la URL del despliegue
     * 6. En la app, ir a Configuración > Pegar la URL
     */
}
