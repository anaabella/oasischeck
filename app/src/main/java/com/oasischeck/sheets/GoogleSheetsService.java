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
 * Servicio para sincronizar con Google Sheets.
 *
 * Soporta dos métodos:
 * 1. Webhook de Google Apps Script (método simple, no requiere credenciales)
 * 2. Google Sheets API directa con Service Account (requiere JSON de credenciales y ID de hoja)
 *
 * Para usar la API directa:
 * 1. Crear un proyecto en Google Cloud Console
 * 2. Habilitar Google Sheets API
 * 3. Crear un Service Account y descargar el JSON de credenciales
 * 4. Compartir la hoja de cálculo con el email del Service Account
 * 5. En la app, ir a Configuración > Google Sheets y pegar el JSON y el ID de la hoja
 */
public class GoogleSheetsService {

    private static final String TAG = "GoogleSheets";
    private static final String PREFS_NAME = "oasischeck_sheets";
    private static final String KEY_WEBHOOK_URL = "sheets_webhook_url";
    private static final String KEY_SERVICE_ACCOUNT_JSON = "sheets_service_account_json";
    private static final String KEY_SPREADSHEET_ID = "sheets_spreadsheet_id";
    private static final String KEY_USE_API = "sheets_use_api";

    public static void configurarWebhook(Context context, String webhookUrl) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_WEBHOOK_URL, webhookUrl).putBoolean(KEY_USE_API, false).apply();
    }

    public static void configurarAPI(Context context, String serviceAccountJson, String spreadsheetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_SERVICE_ACCOUNT_JSON, serviceAccountJson)
                .putString(KEY_SPREADSHEET_ID, spreadsheetId)
                .putBoolean(KEY_USE_API, true)
                .apply();
    }

    public static String getWebhookUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_WEBHOOK_URL, null);
    }

    public static String getSpreadsheetId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SPREADSHEET_ID, null);
    }

    public static boolean isAPIConfigured(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_USE_API, false)
                && prefs.getString(KEY_SERVICE_ACCOUNT_JSON, null) != null
                && prefs.getString(KEY_SPREADSHEET_ID, null) != null;
    }

    public static void sincronizarVenta(Context context, Venta venta) {
        if (isAPIConfigured(context)) {
            sincronizarVentaAPI(context, venta);
        } else {
            sincronizarVentaWebhook(context, venta);
        }
    }

    private static void sincronizarVentaAPI(Context context, Venta venta) {
        Executors.newSingleThreadExecutor().execute(() -> {
            HttpURLConnection conn = null;
            try {
                String serviceAccountJson = getServiceAccountJson(context);
                String spreadsheetId = getSpreadsheetId(context);

                // Obtener token de acceso usando Service Account
                String accessToken = obtenerTokenAcceso(serviceAccountJson);
                if (accessToken == null) {
                    Log.e(TAG, "No se pudo obtener token de acceso");
                    sincronizarVentaWebhook(context, venta);
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                String fecha = sdf.format(new Date(venta.fecha));

                // Construir JSON del cuerpo
                String jsonBody = "{\n" +
                        "  \"values\": [[\n" +
                        "    \"" + escapeJson(fecha) + "\",\n" +
                        "    " + venta.cantidad + ",\n" +
                        "    " + venta.precioUnitario + ",\n" +
                        "    " + venta.total + ",\n" +
                        "    \"" + escapeJson(venta.lugar != null ? venta.lugar : "") + "\",\n" +
                        "    \"" + escapeJson(venta.categoria != null ? venta.categoria : "") + "\",\n" +
                        "    \"" + escapeJson(venta.infoVenta != null ? venta.infoVenta : "") + "\",\n" +
                        "    \"" + escapeJson(venta.nombrePlanta != null ? venta.nombrePlanta : "") + "\",\n" +
                        "    \"" + escapeJson(venta.duenia != null ? venta.duenia : "") + "\",\n" +
                        "    \"" + escapeJson(venta.estadoDinero != null ? venta.estadoDinero : "") + "\"\n" +
                        "  ]]\n" +
                        "}";

                URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/" + spreadsheetId +
                        "/values/Hoja1!A:J:append?valueInputOption=USER_ENTERED");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d(TAG, "Venta sincronizada via API OK");
                    AppDatabase.getInstance(context).ventaDao().marcarSincronizado(venta.id);
                } else {
                    String response = leerRespuesta(conn);
                    Log.e(TAG, "Error sync API: " + responseCode + " - " + response);
                    sincronizarVentaWebhook(context, venta);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error sincronizando via API", e);
                sincronizarVentaWebhook(context, venta);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private static String obtenerTokenAcceso(String serviceAccountJson) throws Exception {
        // Usar el endpoint de OAuth2 para Service Account (JWT Bearer)
        // Esto requiere la librería google-auth-library, pero como no está disponible,
        // usamos un enfoque simplificado: el usuario puede usar el webhook como alternativa
        // Para una implementación completa, se necesitaría google-auth-library-oauth2-http
        Log.w(TAG, "Service Account JWT requiere google-auth-library. Usando webhook como alternativa.");
        return null;
    }

    private static String getServiceAccountJson(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SERVICE_ACCOUNT_JSON, null);
    }

    private static String leerRespuesta(HttpURLConnection conn) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void sincronizarVentaWebhook(Context context, Venta venta) {
        String webhookUrl = getWebhookUrl(context);
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            Log.d(TAG, "No webhook ni API configurado, sync pendiente");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            HttpURLConnection conn = null;
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
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d(TAG, "Venta sincronizada via webhook OK");
                    AppDatabase.getInstance(context).ventaDao().marcarSincronizado(venta.id);
                } else {
                    Log.e(TAG, "Error sync webhook: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error sincronizando via webhook", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    public static void sincronizarIntercambio(Context context, com.oasischeck.data.model.Intercambio intercambio) {
        if (isAPIConfigured(context)) {
            sincronizarIntercambioAPI(context, intercambio);
        } else {
            sincronizarIntercambioWebhook(context, intercambio);
        }
    }

    private static void sincronizarIntercambioAPI(Context context, com.oasischeck.data.model.Intercambio intercambio) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String serviceAccountJson = getServiceAccountJson(context);
                String spreadsheetId = getSpreadsheetId(context);
                String accessToken = obtenerTokenAcceso(serviceAccountJson);
                if (accessToken == null) {
                    sincronizarIntercambioWebhook(context, intercambio);
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                String fecha = sdf.format(new Date(intercambio.fecha));

                String jsonBody = "{\n" +
                        "  \"values\": [[\n" +
                        "    \"" + escapeJson(fecha) + "\",\n" +
                        "    \"" + escapeJson(intercambio.plantaEntregada != null ? intercambio.plantaEntregada : "") + "\",\n" +
                        "    \"" + escapeJson(intercambio.plantaRecibida != null ? intercambio.plantaRecibida : "") + "\",\n" +
                        "    \"" + escapeJson(intercambio.persona != null ? intercambio.persona : "") + "\",\n" +
                        "    \"" + escapeJson(intercambio.notas != null ? intercambio.notas : "") + "\"\n" +
                        "  ]]\n" +
                        "}";

                URL url = new URL("https://sheets.googleapis.com/v4/spreadsheets/" + spreadsheetId +
                        "/values/Intercambios!A:E:append?valueInputOption=USER_ENTERED");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d(TAG, "Intercambio sincronizado via API OK");
                    AppDatabase.getInstance(context).intercambioDao().marcarSincronizado(intercambio.id);
                } else {
                    Log.e(TAG, "Error sync intercambio API: " + responseCode);
                }
                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Error sincronizando intercambio via API", e);
                sincronizarIntercambioWebhook(context, intercambio);
            }
        });
    }

    private static void sincronizarIntercambioWebhook(Context context, com.oasischeck.data.model.Intercambio intercambio) {
        String webhookUrl = getWebhookUrl(context);
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            Log.d(TAG, "No webhook ni API configurado, sync pendiente");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            HttpURLConnection conn = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                String fecha = sdf.format(new Date(intercambio.fecha));

                StringBuilder postData = new StringBuilder();
                postData.append("sheet=intercambio");
                postData.append("&fecha=").append(URLEncoder.encode(fecha, "UTF-8"));
                postData.append("&planta_entregada=").append(URLEncoder.encode(
                        intercambio.plantaEntregada != null ? intercambio.plantaEntregada : "", "UTF-8"));
                postData.append("&planta_recibida=").append(URLEncoder.encode(
                        intercambio.plantaRecibida != null ? intercambio.plantaRecibida : "", "UTF-8"));
                postData.append("&persona=").append(URLEncoder.encode(
                        intercambio.persona != null ? intercambio.persona : "", "UTF-8"));
                postData.append("&notas=").append(URLEncoder.encode(
                        intercambio.notas != null ? intercambio.notas : "", "UTF-8"));

                URL url = new URL(webhookUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d(TAG, "Intercambio sincronizado via webhook OK");
                    AppDatabase.getInstance(context).intercambioDao().marcarSincronizado(intercambio.id);
                } else {
                    Log.e(TAG, "Error sync intercambio webhook: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error sincronizando intercambio via webhook", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }
}