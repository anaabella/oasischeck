package com.oasischeck.ui.configuracion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.oasischeck.R;
import com.oasischeck.sheets.GoogleSheetsService;

public class ConfiguracionSheetsActivity extends AppCompatActivity {

    private EditText etServiceAccountJson, etSpreadsheetId;
    private Button btnGuardarAPI, btnGuardarWebhook, btnProbarConexion;
    private EditText etWebhookUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion_sheets);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etServiceAccountJson = findViewById(R.id.et_service_account_json);
        etSpreadsheetId = findViewById(R.id.et_spreadsheet_id);
        etWebhookUrl = findViewById(R.id.et_webhook_url);
        btnGuardarAPI = findViewById(R.id.btn_guardar_api);
        btnGuardarWebhook = findViewById(R.id.btn_guardar_webhook);
        btnProbarConexion = findViewById(R.id.btn_probar_conexion);

        // Cargar valores guardados
        SharedPreferences prefs = getSharedPreferences("oasischeck_sheets", MODE_PRIVATE);
        etServiceAccountJson.setText(prefs.getString("sheets_service_account_json", ""));
        etSpreadsheetId.setText(prefs.getString("sheets_spreadsheet_id", ""));
        etWebhookUrl.setText(prefs.getString("sheets_webhook_url", ""));

        btnGuardarAPI.setOnClickListener(v -> {
            String json = etServiceAccountJson.getText().toString().trim();
            String sheetId = etSpreadsheetId.getText().toString().trim();
            if (json.isEmpty() || sheetId.isEmpty()) {
                Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }
            GoogleSheetsService.configurarAPI(this, json, sheetId);
            Toast.makeText(this, "API de Google Sheets configurada", Toast.LENGTH_SHORT).show();
        });

        btnGuardarWebhook.setOnClickListener(v -> {
            String url = etWebhookUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Ingresa una URL", Toast.LENGTH_SHORT).show();
                return;
            }
            GoogleSheetsService.configurarWebhook(this, url);
            Toast.makeText(this, "Webhook configurado", Toast.LENGTH_SHORT).show();
        });

        btnProbarConexion.setOnClickListener(v -> {
            Toast.makeText(this, "Probando conexión...", Toast.LENGTH_SHORT).show();
            // Aquí se podría probar la conexión real con Google Sheets API
        });
    }

    public static void iniciar(Context context) {
        context.startActivity(new Intent(context, ConfiguracionSheetsActivity.class));
    }
}